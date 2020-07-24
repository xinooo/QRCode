package com.example.qrcode.Setting;

import android.content.Context;
import android.util.Log;

import com.example.qrcode.MainActivity;
import com.example.qrcode.R;
import com.example.qrcode.zxing.activity.CaptureActivity;
import com.example.qrcode.zxing.camera.AutoFocusCallback;
import com.example.qrcode.zxing.camera.CameraManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SettingTools {
    public static String getAssetsData(Context context) {
        InputStream mAssets = null;
        String result = "";
        try {
            mAssets = context.getAssets().open("setting.json");
            int length = mAssets.available();
            byte[] buffer = new byte[length];
            mAssets.read(buffer);
            result = new String(buffer);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        } finally {
            try {
                if (mAssets != null) {
                    mAssets.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String fileName, String mCachePath){
        File mFile = new File(mCachePath + "/" + fileName);
//        Log.e("AA","readFile::" + mFile.getPath());
        if(!mFile.exists()) {
            return "";
        }
        FileInputStream fis = null;
        InputStreamReader inputStreamReader = null;
        try {
            fis = new FileInputStream(mFile);
            inputStreamReader = new InputStreamReader(fis, "utf-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuffer sb = new StringBuffer("");
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }  catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

    private static void writeFile(String fileName, String data, String mCachePath){
        File fileDir = new File(mCachePath);
        File mFile = new File(mCachePath + "/" + fileName);
//        Log.e("AA","writeFile::" + mFile.getPath());
        FileOutputStream fos = null;
        try {
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }
            if(!mFile.exists()) {
//                Logger.e("创建文件！！！");
                mFile.createNewFile();
            }
            if(data.length() > 1){
//                Log.e("AA","保存文件到sd卡指定路径！！！" + mFile.getPath() +"--------"+ mFile.exists());
                fos = new FileOutputStream(mFile);
                fos.write(data.getBytes());
                fos.flush();// 刷新缓冲区
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<SettingBean> parseJson(String json) {
        List<SettingBean> dataList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            JSONObject jsonObject;
            SettingBean settingBean;
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.optJSONObject(i);
                settingBean = new SettingBean();
                settingBean.setid(jsonObject.optString("id"));
                settingBean.setnote(jsonObject.optString("note"));
                settingBean.setisCheck(jsonObject.optBoolean("isCheck"));
                dataList.add(settingBean);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static void saveJsonData(List<SettingBean> mData, String mCachePath) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        SettingBean settingBean;
        for (int i = 0; i < mData.size(); i++) {
            try {
                settingBean = mData.get(i);
                jsonObject = new JSONObject();
                jsonObject.put("id", settingBean.getid());
                jsonObject.put("note", settingBean.getnote());
                jsonObject.put("isCheck", settingBean.getisCheck());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        writeFile("setting.json", jsonArray.toString(),mCachePath);
    }

    public static void settingChange(String id,boolean isChecked){
        AutoFocusCallback autoFocusCallback  = new AutoFocusCallback();
        switch (id){
            case "播放提示音":
                Log.e("AA",id+isChecked);
                MainActivity.sound = isChecked;
                break;
            case "复制到剪贴板":
                Log.e("AA",id+isChecked);
                MainActivity.isClipData = isChecked;
                break;
            case "自动对焦":
                Log.e("AA",id+isChecked);
                CaptureActivity.handler.isFocus = isChecked;
                if(isChecked){
                    CameraManager.get().requestAutoFocus(CaptureActivity.handler, R.id.auto_focus);
                }else {
                    autoFocusCallback.setHandler(null, 0);
                }
                break;
            case "确定焦点":
                Log.e("AA",id+isChecked);
                break;
            case "自动打开网页":
                Log.e("AA",id+isChecked);
                break;
            case "反色":
                Log.e("AA",id+isChecked);
                break;
        }

    }
}
