package com.example.qrcode.Image;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ImageTools {
    public static String setPhotoPath() {
        //儲存路徑
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Share/";
        //檔名(以時間命名)
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = simpleDate.format(now.getTime());
        return dir + "/" + fileName + ".png";
    }

    //儲存圖片
    public static Uri SaveImage(Context context, Bitmap mBitmap){
        //獲取內部儲存狀態
        String state = Environment.getExternalStorageState();
        //如果狀態不是mounted，無法讀寫
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        //儲存圖片
        try {
            File file = new File(ImageTools.setPhotoPath());
            FileOutputStream out = null;
            out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            /*
            不使用Uri.fromFile(file)
            使用FileProvider解决file:// URI引起的FileUriExposedException
            1.在AndroidManifest.xml中添加provider
            2.創建res/xml/provider_paths.xml
            */
            Uri photoUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file);
            return photoUri;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //儲存圖片
    public static void SaveImage(Bitmap bitmap, String path) {
        //獲取內部儲存狀態
        String state = Environment.getExternalStorageState();
        //如果狀態不是mounted，無法讀寫
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return;
        }
        FileOutputStream out = null;
        File file = new File(path);
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (FileNotFoundException ignore) {
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
