package com.example.qrcode.AppUpdater;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.example.qrcode.AppUpdater.Local.ErrorMessage.APP_PAGE_ERROR;
import static com.example.qrcode.AppUpdater.Local.ErrorMessage.CONNECT_ERROR;
import static com.example.qrcode.AppUpdater.Local.ErrorMessage.NETWORK_NOT_AVAILABLE;
import static com.example.qrcode.AppUpdater.Local.ErrorMessage.PARSE_ERROR;


public class AppUpdater {
    private static final String TAG = "AppUpdater";
    private static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=%s";
    private static final String PLAY_STORE_TAG_RELEASE = "data-node-index=\"0;0\" jsmodel=\"hc6Ubd\"><div class=\"W4P4ne \"><div class=\"wSaTQd\">";
    private static final String PLAY_STORE_TAG_CHANGES = "data-node-index=\"7;0\" jsmodel=\"hc6Ubd\"><div class=\"W4P4ne \"><div class=\"wSaTQd\">";

    public interface OnFinishListener {
        void onSuccess(Update update);
        void onFailed(String errorMessage);
    }

    public static void getLatestAppVersion(Context context, final OnFinishListener listener) {
        if (!isNetworkAvailable(context)) {
            listener.onFailed(NETWORK_NOT_AVAILABLE);
        }else {
            OkHttpClient client = new OkHttpClient();
            final String storeUrl = getStoreUrl(context);
            Request request = new Request.Builder().url(storeUrl).build();
            Log.d(TAG, "storeUrl = " + storeUrl);
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onFailed(CONNECT_ERROR);
                }
                @Override
                public void onResponse(Call call, Response response){
                    ResponseBody body = response.body();
                    try {
                        if(body != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), "UTF-8"));
                            String versionString = "";
                            String changesString = "";
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.contains(PLAY_STORE_TAG_RELEASE)) {
                                    versionString = line;
                                } else if (line.contains(PLAY_STORE_TAG_CHANGES)) {
                                    changesString = line;
                                }
                            }
                            reader.close();
                            body.close();
                            listener.onSuccess(new Update(getVersion(versionString), getRecentChanges(changesString), storeUrl));
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        listener.onFailed(APP_PAGE_ERROR);
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.onFailed(PARSE_ERROR);
                    }
                }
            });
        }
    }

    private static String getStoreUrl(Context context) {
        return String.format(PLAY_STORE_URL, context.getPackageName());
    }
    private static String getVersionPattern() {
        StringBuilder versionPattern = new StringBuilder();
        for(String version: Local.versions){
            versionPattern.append("[").append(version).append("]*");
        }
        return versionPattern.toString();
    }
    private static String getVersion(String source) {
        String pattern = "<div class=\"BgcNfc\">"+getVersionPattern()+"</div><span class=\"htlgb\"><div class=\"IQ1z0d\"><span class=\"htlgb\">([^<]+)</span></div></span></div>";
        Matcher matcher = Pattern.compile(pattern).matcher(source);
        if (matcher.find()) return matcher.group(1);
        else return "";
    }

    private static String getRecentChanges(String source) {
        String pattern = "<content>([^\"]*)</content>";
        Matcher matcher = Pattern.compile(pattern).matcher(source);
        if (matcher.find()) return matcher.group(1);
        else return "";
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return  (networkInfo != null && networkInfo.isConnected());
        }
        return false;
    }
}