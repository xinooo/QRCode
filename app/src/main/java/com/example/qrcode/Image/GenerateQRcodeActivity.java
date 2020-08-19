package com.example.qrcode.Image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qrcode.Image.ClipImage.CropActivity;
import com.example.qrcode.R;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Hashtable;

public class GenerateQRcodeActivity extends AppCompatActivity implements View.OnClickListener {

    private final int GET_IMAGE = 0;
    private final int CLIP_IMAGE = 1;

    private Button btn,select_icon,clear_icon;
    private EditText name,organization,address,phone,email,detail;
    private CheckBox rect,circle;
    private ImageView qrcode,icon;
    private HashMap<String,String> information;
    private Bitmap QRCodeBitmap;
    private String iconPath = "";
    private int radius = 20;

    //Toolbar
    private ImageView leftbutton,rightbutton;
    private TextView title;
    private LinearLayout toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment);
        information = new HashMap<>();
        findViewById();

        //Toolbar
        leftbutton.setImageDrawable(getResources().getDrawable(R.mipmap.ic_menu_back));
        rightbutton.setImageDrawable(getResources().getDrawable(R.mipmap.ic_menu_share));
        rightbutton.setVisibility(View.GONE);
        title.setText(getString(R.string.menu3));
        title.setTextColor(getResources().getColor(R.color.white));
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        clear_icon.setOnClickListener(this);
        btn.setOnClickListener(this);
        select_icon.setOnClickListener(this);
        leftbutton.setOnClickListener(this);
        rightbutton.setOnClickListener(this);
    }

    private void findViewById(){
        btn = (Button)findViewById(R.id.btn);
        qrcode = (ImageView)findViewById(R.id.qrcode);
        select_icon = (Button) findViewById(R.id.select_icon);
        icon = (ImageView)findViewById(R.id.icon);
        name = (EditText)findViewById(R.id.name);
        organization = (EditText)findViewById(R.id.organization);
        address = (EditText)findViewById(R.id.address);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        detail = (EditText)findViewById(R.id.detail);
        clear_icon = (Button) findViewById(R.id.clear_icon);
        rect = (CheckBox)findViewById(R.id.rect);
        circle = (CheckBox)findViewById(R.id.circle);

        //Toolbar
        title = (TextView)findViewById(R.id.scanner_toolbar_title);
        leftbutton = (ImageView)findViewById(R.id.scanner_toolbar_leftbutton);
        rightbutton = (ImageView)findViewById(R.id.scanner_toolbar_rightbutton);
        toolbar = (LinearLayout)findViewById(R.id.include);

        rect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    circle.setChecked(false);
                    radius = 20;
                }
            }
        });
        circle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    rect.setChecked(false);
                    radius = 200;
                }
            }
        });
    }

    private void clearText(){
        name.setText(null);
        organization.setText(null);
        address.setText(null);
        phone.setText(null);
        email.setText(null);
        detail.setText(null);
    }

    private void setData(){
        information.put("name",name.getText().toString());
        information.put("organization",organization.getText().toString());
        information.put("address",address.getText().toString());
        information.put("phone",phone.getText().toString());
        information.put("email",email.getText().toString());
        information.put("detail",detail.getText().toString());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn:
                try {
                    Gson gson = new Gson();
                    setData();
                    QRCodeBitmap = createQRCode(gson.toJson(information),1000);
                    qrcode.setImageBitmap(QRCodeBitmap);
                    rightbutton.setVisibility(View.VISIBLE);
                    clearText();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.scanner_toolbar_leftbutton:
                clearText();
                ImageTools.DeleteIcon(this,iconPath);
                iconPath = "";
                finish();
                break;
            case R.id.scanner_toolbar_rightbutton:
                ShareImage(QRCodeBitmap);
                break;
            case R.id.select_icon:
                Intent i = new Intent(Intent.ACTION_PICK, null);
                i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(i, GET_IMAGE);
                break;
            case R.id.clear_icon:
                ImageTools.DeleteIcon(this,iconPath);
                iconPath = "";
                icon.setImageResource(R.drawable.select_icon_bg);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            switch (requestCode) {
                case GET_IMAGE:
                    //從相冊獲取圖片
                    try{
                        final Uri imageUri = data.getData();
                        Log.e("imageUri:",imageUri+"");
                        String selectPhoto = GetImageResult.getRealPathFromUri(this,imageUri);
                        Log.e("selectPhoto:",selectPhoto);
                        startActivityForResult(new Intent(GenerateQRcodeActivity.this, CropActivity.class).putExtra("path", selectPhoto), CLIP_IMAGE);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case CLIP_IMAGE:
                    String path = data.getStringExtra("path");
                    iconPath = path;
                    icon.setImageBitmap(GetImageResult.getBitmap(iconPath));
                    break;
                default:
                    break;
            }
        }
    }

    private Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN,1);
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight,hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
                else {
                    pixels[y * width + x] = 0xffffffff;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        if (GetImageResult.getBitmap(iconPath) == null){
            return bitmap;
        }else {
            return  addLogo(bitmap, GetImageResult.getBitmap(iconPath));
        }
    }

    //合成bitmap
    private Bitmap addLogo(Bitmap qrcode, Bitmap logo){
        int bgWidth = qrcode.getWidth();
        int bgHeigh = qrcode.getHeight();

        logo = ThumbnailUtils.extractThumbnail(logo,bgWidth/5,bgHeigh/5,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        Bitmap cvbitmap = Bitmap.createBitmap(bgWidth,bgHeigh, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(cvbitmap);

        int left = (bgWidth-logo.getWidth())/2;
        int top = (bgHeigh-logo.getHeight())/2;
        //畫icon
        final int color = 0xff007AFF;
        //設定座標點 Rect(左,上,右,下)
        Rect rect = new Rect(left+10,top+10,left+logo.getWidth()-10,top+logo.getHeight()-10);
        RectF rectF = new RectF(rect);
        //去鋸齒
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        //畫圓角矩形
        canvas.drawRoundRect(rectF, radius, radius, paint);
        //通過SRC_IN的模式取原图片和圆角矩形重叠部分
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //畫icon
        canvas.drawBitmap(logo,(bgWidth-logo.getWidth())/2,(bgHeigh-logo.getHeight())/2,paint);

        //通過DST_OVER的模式繪製在原图片下方
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        //畫圓角矩形
        rect = new Rect(left,top,left+logo.getWidth(),top+logo.getHeight());
        rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, radius, radius, paint);
        //在icon下方畫QRCode
        canvas.drawBitmap(qrcode,0,0,paint);
        canvas.save();
        canvas.restore();
        if (cvbitmap.isRecycled()){
            cvbitmap.recycle();
        }
        return cvbitmap;
    }

    //分享QRCode
    private void ShareImage(Bitmap bitmap){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, ImageTools.SaveImage(this,bitmap)/*分享前要先存入本地*/);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
}
