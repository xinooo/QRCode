package com.example.qrcode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.qrcode.Setting.SettingTools;
import com.example.qrcode.zxing.activity.CaptureActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button;
    public static boolean TW = false;

    private String takePhotoPath;
    public static final int REQUEST_CAMERS = 23;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button)findViewById(R.id.btn);
        iv = (ImageView)findViewById(R.id.iv);
        ToastUtil.init(getApplicationContext());
        showPermission();
        if (this.getResources().getConfiguration().locale.getCountry().equals("TW")) {
            TW = true;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);
                launchCamera();
            }
        });
    }
    @TargetApi(23)
    @SuppressLint("NewApi")
    private void showPermission() {
        // We don't have permission so prompt the user
        List<String> permissions = new ArrayList<String>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 許可授權
            } else {
                // 沒有權限
                ToastUtil.showMessage("未授權應用使用權限!");
                showPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    final String text = data.getStringExtra("result");
                    SettingTools.todo(this,text,true);
                    break;
                case REQUEST_CAMERS:
                    Glide.with(this).asBitmap().load(takePhotoPath).into(new BitmapImageViewTarget(iv) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCornerRadius(20);
                            iv.setImageDrawable(circularBitmapDrawable);
                        }
                    });
                    break;
            }
        }
    }

    private void launchCamera() {
        try {

            takePhotoPath = getNewPhotoPath();
            Intent mSourceIntent = takeBigPicture(this, takePhotoPath);
            startActivityForResult(mSourceIntent, REQUEST_CAMERS);
        } catch (Exception ignore) {
            ToastUtil.showMessage("未授權應用使用權限!");
        }
    }

    public static String getNewPhotoPath() {
        return getDirPath() + "/" + System.currentTimeMillis() + ".jpg";
    }
    public static String getDirPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/UploadImage";
    }

    public static Intent takeBigPicture(Context context, String path) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newPictureUri(context, path));
        return intent;
    }

    private static Uri newPictureUri(Context context, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider",
                    new File(path));
        } else {
            return Uri.fromFile(new File(path));
        }
    }
}
