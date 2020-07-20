package com.example.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.HashMap;
import java.util.Hashtable;

public class GenerateQRcodeFragment extends Fragment implements View.OnClickListener {
    public View mview;
    private Button btn;
    private EditText name,organization,address,phone,email,detail;
    private ImageView qrcode;
    private HashMap<String,String> information;

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
        btn.setOnClickListener(this);
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
//                getFragmentManager().popBackStack(); //返回
                try {
                    Gson gson = new Gson();
                    setData();
                    qrcode.setImageBitmap(createQRCode(gson.toJson(information),300));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public static Bitmap createQRCode(String str,int widthAndHeight) throws WriterException {
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str,
                BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
