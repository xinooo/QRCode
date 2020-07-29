package com.example.qrcode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;

public class GenerateQRcodeFragment extends Fragment implements View.OnClickListener {
    public View mview;
    private Button btn;
    private EditText name,organization,address,phone,email,detail;
    private ImageView qrcode;
    private HashMap<String,String> information;
    private Bitmap QRCodeBitmap;

    //Toolbar
    private ImageView leftbutton,rightbutton;
    private TextView title;
    private LinearLayout toolbar;

    public static GenerateQRcodeFragment newInstance() {
        GenerateQRcodeFragment fragment = new GenerateQRcodeFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        information = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mview = inflater.inflate(R.layout.fragment, container, false);
        findViewById();

        //Toolbar
        leftbutton.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.ic_menu_back));
        rightbutton.setImageDrawable(getActivity().getResources().getDrawable(R.mipmap.ic_menu_share));
        rightbutton.setVisibility(View.GONE);
        title.setText(getString(R.string.menu3));
        title.setTextColor(getActivity().getResources().getColor(R.color.white));
        toolbar.setBackgroundColor(getActivity().getResources().getColor(R.color.colorPrimary));

        btn.setOnClickListener(this);
        leftbutton.setOnClickListener(this);
        rightbutton.setOnClickListener(this);
        return mview;
    }

    private void findViewById(){
        btn = (Button)mview.findViewById(R.id.btn);
        qrcode = (ImageView)mview.findViewById(R.id.qrcode);
        name = (EditText)mview.findViewById(R.id.name);
        organization = (EditText)mview.findViewById(R.id.organization);
        address = (EditText)mview.findViewById(R.id.address);
        phone = (EditText)mview.findViewById(R.id.phone);
        email = (EditText)mview.findViewById(R.id.email);
        detail = (EditText)mview.findViewById(R.id.detail);

        //Toolbar
        title = (TextView)mview.findViewById(R.id.scanner_toolbar_title);
        leftbutton = (ImageView) mview.findViewById(R.id.scanner_toolbar_leftbutton);
        rightbutton = (ImageView) mview.findViewById(R.id.scanner_toolbar_rightbutton);
        toolbar = (LinearLayout)mview.findViewById(R.id.include);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn:
                try {
                    Gson gson = new Gson();
                    setData();
                    QRCodeBitmap = createQRCode(gson.toJson(information),300,getContext());
                    qrcode.setImageBitmap(QRCodeBitmap);
                    rightbutton.setVisibility(View.VISIBLE);
                    clearText();
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.scanner_toolbar_leftbutton:
                clearText();
                getFragmentManager().popBackStack(); //返回
                break;
            case R.id.scanner_toolbar_rightbutton:
                ShareImage(getContext(),QRCodeBitmap);
                break;
        }
    }

    private Bitmap createQRCode(String str, int widthAndHeight, Context context) throws WriterException {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
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
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.face005, null);
        return addLogo(bitmap,logo);
    }

    //合成bitmap
    private Bitmap addLogo(Bitmap qrcode, Bitmap logo){
        int bgWidth = qrcode.getWidth();
        int bgHeigh = qrcode.getHeight();

        logo = ThumbnailUtils.extractThumbnail(logo,bgWidth/5,bgHeigh/5,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        Bitmap cvbitmap = Bitmap.createBitmap(bgWidth,bgHeigh, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(cvbitmap);
        canvas.drawBitmap(qrcode,0,0,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(logo,(bgWidth-logo.getWidth())/2,(bgHeigh-logo.getHeight())/2,paint);
        canvas.save();
        canvas.restore();
        if (cvbitmap.isRecycled()){
            cvbitmap.recycle();
        }
        return cvbitmap;
    }

    //分享QRCode
    private void ShareImage(Context context,Bitmap bitmap){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, SaveImage(context,bitmap)/*分享前要先存入本地*/);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
    //儲存QRCode
    private Uri SaveImage(Context context,Bitmap mBitmap){
        //獲取內部儲存狀態
        String state = Environment.getExternalStorageState();
        //如果狀態不是mounted，無法讀寫
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }
        //儲存路徑
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Share/";
        //檔名(以時間命名)
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = simpleDate.format(now.getTime());
        //儲存圖片
        try {
            File file = new File(dir + fileName + ".png");
            FileOutputStream out = null;
            out = new FileOutputStream(file);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
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
}
