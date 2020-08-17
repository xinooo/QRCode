package com.example.qrcode;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class WifiAutoConnectManager {
    private static final String TAG = WifiAutoConnectManager.class
            .getSimpleName();

    WifiManager wifiManager;

    // 定義幾種加密方式，一種是WEP，一種是WPA，還有沒有密碼的情況
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    // 建構函式
    public WifiAutoConnectManager(Context mContext) {
        this.wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    // 提供一個外部介面，傳入要連線的無線網
    public void connect(String ssid, String password, WifiCipherType type) {
        Thread thread = new Thread(new ConnectRunnable(ssid, password, type));
        thread.start();
    }

    // 檢視以前是否也配置過這個網路
    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // config.SSID = SSID;
        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            // config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            // config.wepTxKeyIndex = 0;
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此處需要修改否則不能自動重聯
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

        }
        return config;
    }

    // 開啟wifi功能
    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    // 關閉WIFI
    private void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    private class ConnectRunnable implements Runnable {
        private String ssid;

        private String password;

        private WifiCipherType type;

        public ConnectRunnable(String ssid, String password, WifiCipherType type) {
            this.ssid = ssid;
            this.password = password;
            this.type = type;
        }

        @Override
        public void run() {
            // 開啟wifi
            openWifi();
            // 開啟wifi功能需要一段時間(一般需要1-3秒左右)，所以要等到wifi
            // 狀態變成WIFI_STATE_ENABLED的時候才能執行下面的語句

            while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                try {
                    // 為了避免程式一直while迴圈，讓它睡個200毫秒檢測……
                    Thread.sleep(200);

                } catch (InterruptedException ie) {
                    Log.e(TAG, ie.toString());
                }
            }

            WifiConfiguration tempConfig = isExsits(ssid);

            if (tempConfig != null) {
                // wifiManager.removeNetwork(tempConfig.networkId);此方法只能刪除自己應用建立的wifi

                wifiManager.enableNetwork(tempConfig.networkId,
                        true);
            } else {
                WifiConfiguration wifiConfig = createWifiInfo(ssid, password,
                        type);
                //
                if (wifiConfig == null) {
                    Log.d(TAG, "wifiConfig is null!");
                    return;
                }

                int netID = wifiManager.addNetwork(wifiConfig);
                boolean enabled = wifiManager.enableNetwork(netID, true);
                Log.d(TAG, "enableNetwork status enable=" + enabled);
                boolean connected = wifiManager.reconnect();
                Log.d(TAG, "enableNetwork connected=" + connected);
            }

        }
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f')) {
                return false;
            }
        }

        return true;
    }

    // 獲取ssid的加密方式

    public static WifiCipherType getCipherType(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> list = wifiManager.getScanResults();

        for (ScanResult scResult : list) {

            if (!TextUtils.isEmpty(scResult.SSID) && scResult.SSID.equals(ssid)) {
                String capabilities = scResult.capabilities;
                // Log.i("hefeng","capabilities=" + capabilities);

                if (!TextUtils.isEmpty(capabilities)) {
                    Log.e("AAAAA","AAAAAAAA"+capabilities);

                    if (capabilities.contains("WPA")
                            || capabilities.contains("wpa")) {
                        return WifiCipherType.WIFICIPHER_WPA;
                    } else if (capabilities.contains("WEP")
                            || capabilities.contains("wep")) {
                        return WifiCipherType.WIFICIPHER_WEP;
                    } else {
                        return WifiCipherType.WIFICIPHER_NOPASS;
                    }
                }
            }
        }
        return WifiCipherType.WIFICIPHER_INVALID;
    }

    public static WifiCipherType getNetWorkType(Context context, String Type) {
        if (Type.equals("WPA")
                || Type.equals("wpa")) {
            return WifiCipherType.WIFICIPHER_WPA;
        } else if (Type.equals("WEP")
                || Type.equals("wep")) {
            return WifiCipherType.WIFICIPHER_WEP;
        } else {
            return WifiCipherType.WIFICIPHER_NOPASS;
        }
    }
}
