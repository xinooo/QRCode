package com.example.qrcode;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcode.Setting.SettingTools;
import com.example.qrcode.zxing.activity.CaptureActivity;

public class ScanResultDialog implements View.OnClickListener {

    private final Activity mactivity;
    private Dialog dialog;
    private ViewGroup root;
    private String resultString;
    private Bitmap scanBitmap;
    private TextView tv_result, b1, b2, b3;
    private ImageView im_result;
    public static ClipboardManager myClipboard ;

    public ScanResultDialog(Activity activity, String string, Bitmap bitmap) {
        this.mactivity = activity;
        this.resultString = string;
        this.scanBitmap = bitmap;
        dialog = new Dialog(activity,R.style.dialog_no_title);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        ViewGroup.LayoutParams contentParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(initView(), contentParam);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = activity.getResources().getDisplayMetrics().widthPixels;
            window.setAttributes(params);
            window.setWindowAnimations(R.style.ActionSheetDialogAnimation);
        }
    }

    private View initView(){
        root = (ViewGroup) View.inflate(mactivity, R.layout.dialog_scan_result, null);
        tv_result = (TextView)root.findViewById(R.id.tv_result);
        b1 = (TextView)root.findViewById(R.id.b1);
        b2 = (TextView)root.findViewById(R.id.b2);
        b3 = (TextView)root.findViewById(R.id.b3);
        im_result = (ImageView) root.findViewById(R.id.im_result);

        myClipboard= (ClipboardManager)mactivity.getSystemService(Context.CLIPBOARD_SERVICE);
        im_result.setVisibility(scanBitmap == null? View.GONE : View.VISIBLE);
        tv_result.setText(resultString);
        im_result.setImageBitmap(scanBitmap);

        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);

        return root;
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (v == b1){
            ClipData myClip;
            myClip = ClipData.newPlainText("text", resultString);
            myClipboard.setPrimaryClip(myClip);
        }
        if (v == b2){
            if(SettingTools.isUrl(resultString)){
                Log.e("openWeb", "onActivityResult: 打開web "+ resultString);
                Uri uri = Uri.parse(resultString);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mactivity.startActivity(intent);
            }else {
                ToastUtil.showMessageOnCenter("無法開啟網頁");
                Log.e("openWeb", "onActivityResult: 不開web "+ resultString);
            }
        }
        if (v == b3){
            CaptureActivity.handler.restartPreviewAndDecode();
            dialog.dismiss();
        }
    }
}
