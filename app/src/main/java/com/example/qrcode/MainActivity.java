package com.example.qrcode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.qrcode.Setting.SettingTools;
import com.example.qrcode.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button button;
    public static boolean TW = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openAppByUrl();
        button = (Button)findViewById(R.id.btn);
        ToastUtil.init(getApplicationContext());
        showPermission();
        if (this.getResources().getConfiguration().locale.getCountry().equals("TW")) {
            TW = true;
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);
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
            }
        }
    }

    private void openAppByUrl(){
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        String dataString = intent.getDataString();
        Log.e("openAppByUrl","scheme："+scheme+
                        ", dataString："+dataString);
        Uri uri = intent.getData();
        if (uri != null) {
            //完整的url信息
            String url = uri.toString();
            //scheme部分
            String schemes = uri.getScheme();
            //host部分
            String host = uri.getHost();
            //port部分
            int port = uri.getPort();
            //访问路径
            String path = uri.getPath();
            //编码路径
            String path1 = uri.getEncodedPath();
            //query部分
            String queryString = uri.getQuery();
            //获取参数值
            String Info_system = uri.getQueryParameter("system");
            String Info_id = uri.getQueryParameter("id");
            Log.e("openAppByUrl","url："+url+
                                ",\n schemes："+schemes+
                                ",\n host："+host+
                                ",\n port："+port+
                                ",\n path："+path+
                                ",\n path1："+path1+
                                ",\n queryString："+queryString+
                                ",\n Info_system："+Info_system+
                                ",\n Info_id："+Info_id);
            if (Info_id.equals("45464")){
                startActivityForResult(new Intent(MainActivity.this, CaptureActivity.class), 1);
            }
        }
    }
}
