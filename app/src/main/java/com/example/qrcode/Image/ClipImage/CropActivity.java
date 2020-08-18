package com.example.qrcode.Image.ClipImage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcode.Image.ImageTools;
import com.example.qrcode.R;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CropActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.crop_ziv)
    ZoomImageView ziv;
    @BindView(R.id.crop_confirm)
    TextView tvConfirm;
    @BindView(R.id.crop_cancel)
    TextView tvCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        ButterKnife.bind(this);

        String path = getIntent().getStringExtra("path");
        if (TextUtils.isEmpty(path)) {
            forceClose();
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            forceClose();
            return;
        }
        int degree = getBitmapOritation(path);
        if (degree > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        ziv.setImageBitmap(bitmap);

        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    private int getBitmapOritation(String path) {
        int degree = 0;
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException ignore) {
        }
        return degree;
    }

    private void forceClose() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void Complete(String path) {
        Intent intent = new Intent().putExtra("path", path);
        setResult(RESULT_OK, intent);
        finish();
    }



    @Override
    public void onClick(View view) {
        if (view == tvConfirm){
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = ziv.getCropBitmap();
                    String newPhotoPath = ImageTools.setPhotoPath();
                    ImageTools.SaveImage(bitmap, newPhotoPath);
                    Complete(newPhotoPath);
                }
            });
        }
        if (view == tvCancel){
            forceClose();
        }
    }
}
