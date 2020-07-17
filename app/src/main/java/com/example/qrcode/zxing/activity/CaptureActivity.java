package com.example.qrcode.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.qrcode.GenerateQRcodeFragment;
import com.example.qrcode.zxing.camera.CameraManager;
import com.example.qrcode.zxing.decoding.CaptureActivityHandler;
import com.example.qrcode.zxing.decoding.InactivityTimer;
import com.example.qrcode.zxing.view.ViewfinderView;
import com.example.qrcode.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class CaptureActivity extends AppCompatActivity implements Callback ,OnClickListener{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private Button btn_light;
	private boolean isOpen = true;
	private ImageView back,add;
	protected FragmentManager mFragmentManager = null;
	protected FragmentTransaction mFragmentTransaction = null;
	private GenerateQRcodeFragment generateQRcodeFragment;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);
		CameraManager.init(getApplication());
		generateQRcodeFragment = GenerateQRcodeFragment.newInstance();
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		btn_light = (Button) this.findViewById(R.id.btn_light);
		back = (ImageView) findViewById(R.id.scanner_toolbar_back);
		add = (ImageView) findViewById(R.id.scanner_toolbar_add);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		btn_light.setOnClickListener(this);
		back.setOnClickListener(this);
		add.setOnClickListener(this);
	}

	@SuppressWarnings("deprecation")
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * Handler scan result
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		String resultString = result.getText();
		// FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "failed!", Toast.LENGTH_SHORT).show();
		} else {
			// System.out.println("Result:"+resultString);
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		CaptureActivity.this.finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.btn_light:
				android.hardware.Camera camera = CameraManager.getCamera();
				android.hardware.Camera.Parameters parameter = camera.getParameters();
				// TODO FlashLight
				if (isOpen) {
					if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
					{
					}else {
						btn_light.setText(getString(R.string.closelight));
						parameter.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
						camera.setParameters(parameter);
						isOpen = false;
					}
				} else {
					btn_light.setText(getString(R.string.openlight));
					parameter.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameter);
					isOpen = true;
				}
				break;
			case R.id.scanner_toolbar_back:
				finish();
				break;
			case R.id.scanner_toolbar_add:
				showFragment(generateQRcodeFragment);
				break;
		}
	}

	public void showFragment(Fragment fragment){
		if (mFragmentManager == null) {
			mFragmentManager = getSupportFragmentManager();
		}
		mFragmentTransaction = mFragmentManager.beginTransaction();
		if (null == mFragmentManager.findFragmentByTag("generateQRcode")) {
			mFragmentTransaction.add(R.id.contentFragment, fragment, "generateQRcode");
		}
		mFragmentTransaction.show(fragment);
		mFragmentTransaction.addToBackStack(null);
		mFragmentTransaction.commitAllowingStateLoss();
	}
}