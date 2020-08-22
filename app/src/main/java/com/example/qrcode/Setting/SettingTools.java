package com.example.qrcode.Setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;

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
    public static boolean isClipData = false;   //複製結果
    public static boolean sound = false;        //開啟提示音
    public static boolean invert = false;       //反色
    public static boolean openWeb = false;      //開啟網頁
    public static boolean isAutoFocus = false;   //自動對焦
    public static int settingRoot = 0;   //setting.json版本
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

    public static List<SettingBean> getSettingData(Context context, String mCachePath){
        String jsonData = null;
        String sdData = null;
        int root = 0,sdroot = 0;

        try {
            //本地
            String sdresult = SettingTools.readFile("setting.json",mCachePath);
            JSONObject sdObject = new JSONObject(sdresult);
            sdroot = sdObject.optInt("root");
            sdData = sdObject.optString("data");

            //預設
            String result = SettingTools.getAssetsData(context);
            JSONObject rootObject = new JSONObject(result);
            root = rootObject.optInt("root");
            jsonData = rootObject.optString("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        settingRoot = Math.max(sdroot, root);
        return SettingTools.parseJson(sdroot >= root? sdData : jsonData);
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
                settingBean.setid_tw(jsonObject.optString("id_tw"));
                settingBean.setnote_tw(jsonObject.optString("note_tw"));
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
        JSONObject jsonObject1 = null;
        SettingBean settingBean;

            try {
                for (int i = 0; i < mData.size(); i++) {
                    settingBean = mData.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", settingBean.getid());
                    jsonObject.put("note", settingBean.getnote());
                    jsonObject.put("id_tw", settingBean.getid_tw());
                    jsonObject.put("note_tw", settingBean.getnote_tw());
                    jsonObject.put("isCheck", settingBean.getisCheck());
                    jsonObject1 = new JSONObject();
                    jsonObject1.put("root",settingRoot);
                    jsonArray.put(jsonObject);
                    jsonObject1.put("data",jsonArray);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        writeFile("setting.json", jsonObject1.toString(),mCachePath);
    }

    public static boolean isUrl(String value){
        String[] start = {"http:","https:","file:"};
        for (int i = 0; i < start.length; i++) {
            if (value.startsWith(start[i])) {
                return true;
            }
        }
        return false;
    }
    public static ClipboardManager myClipboard ;
    public static void todo(final Context context, final String result, boolean b){
        myClipboard= (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        //複製結果
        if(SettingTools.isClipData){
            ClipData myClip;
            myClip = ClipData.newPlainText("text", result);
            myClipboard.setPrimaryClip(myClip);
        }
        //提示音
        if(SettingTools.sound && b){
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone rt = RingtoneManager.getRingtone(context, uri);
            rt.play();
        }
        //自動開啟網頁
        if(SettingTools.openWeb){
            if(SettingTools.isUrl(result)){
                Log.e("openWeb", "onActivityResult: 打開web "+ result);
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        Uri uri = Uri.parse(result);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }}, 50);
            }else {
                Log.e("openWeb", "onActivityResult: 不開web "+ result);
            }
        }
    }


    public static void settingChange(String id, boolean isChecked){
        AutoFocusCallback autoFocusCallback  = new AutoFocusCallback();
        switch (id){
            case "播放提示音":
                Log.e("AA",id + isChecked);
                sound = isChecked;
                break;
            case "複製到剪貼板":
            case "复制到剪贴板":
                Log.e("AA",id + isChecked);
                isClipData = isChecked;
                break;
            case "自動對焦":
            case "自动对焦":
                Log.e("AA",id + isChecked);
                isAutoFocus = isChecked;
                if(isChecked){
                    CameraManager.get().requestAutoFocus(CaptureActivity.handler, R.id.auto_focus);
                }else {
                    autoFocusCallback.setHandler(null, 0);
                }
                break;
            case "確定焦點":
            case "确定焦点":
                Log.e("AA",id + isChecked);
                break;
            case "自動打開網頁":
            case "自动打开网页":
                Log.e("AA",id + isChecked);
                openWeb = isChecked;
                break;
            case "反色":
                Log.e("AA",id + isChecked);
                invert = isChecked;
                if(isChecked){
                    CaptureActivity.invert.setVisibility(View.VISIBLE);
                }else {
                    CaptureActivity.invert.setVisibility(View.GONE);
                }
                break;
        }
    }
}
