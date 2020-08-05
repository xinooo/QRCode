package com.example.qrcode.zxing.activity;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.qrcode.GenerateQRcodeFragment;
import com.example.qrcode.GetImageResult;
import com.example.qrcode.ScanResultDialog;
import com.example.qrcode.Setting.SettingBean;
import com.example.qrcode.Setting.SettingFragment;
import com.example.qrcode.Setting.SettingTools;
import com.example.qrcode.zxing.camera.AutoFocusCallback;
import com.example.qrcode.zxing.camera.CameraManager;
import com.example.qrcode.zxing.decoding.CaptureActivityHandler;
import com.example.qrcode.zxing.decoding.InactivityTimer;
import com.example.qrcode.zxing.view.ViewfinderView;
import com.example.qrcode.R;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class CaptureActivity extends AppCompatActivity implements Callback ,OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "CaptureActivity";
	public static CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private boolean isOpen = true;
	private ImageView menu,flashlight;
	protected FragmentManager mFragmentManager = null;
	protected FragmentTransaction mFragmentTransaction = null;
	private GenerateQRcodeFragment generateQRcodeFragment;
	private SettingFragment settingFragment;
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private AutoFocusCallback autoFocusCallback;
	private SurfaceView surfaceView;
	private List<SettingBean> mData = new ArrayList<SettingBean>();
	public static String mCachePath;
	public static TextView invert;
	private boolean isFirst = true;
	private ScanResultDialog scanResultDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
		setContentView(R.layout.capture);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);
		CameraManager.init(getApplication());
		mCachePath = getFilesDir().getPath() + "/cache";
		generateQRcodeFragment = GenerateQRcodeFragment.newInstance();
		settingFragment = SettingFragment.newInstance();
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		menu = (ImageView) findViewById(R.id.scanner_toolbar_leftbutton);
		flashlight = (ImageView) findViewById(R.id.scanner_toolbar_rightbutton);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        invert = (TextView)findViewById(R.id.invert);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		menu.setOnClickListener(this);
		flashlight.setOnClickListener(this);
        surfaceView.setOnClickListener(this);
		navigationView.setNavigationItemSelectedListener(this);

		autoFocusCallback = new AutoFocusCallback();
		String jsonData = SettingTools.readFile("setting.json",mCachePath);
		if (TextUtils.isEmpty(jsonData)) {
			jsonData = SettingTools.getAssetsData(this);
		}
		mData = SettingTools.parseJson(jsonData);
	}

	@SuppressWarnings("deprecation")
	protected void onResume() {
		super.onResume();
        Log.d(TAG, "onResume" );
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
        Log.d(TAG, "onPause" );
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
        Log.d(TAG, "onDestroy" );
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
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
			scanResultDialog = new ScanResultDialog(this,resultString,barcode);
			scanResultDialog.show();
			SettingTools.todo(this,resultString,true);
		}
//		CaptureActivity.this.finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException | RuntimeException ioe) {
			return;
		}
        if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
		CameraSetting();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated" );
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed" );
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		if(isFirst){
			viewfinderView.drawViewfinder();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.scanner_toolbar_leftbutton:
				drawerLayout.openDrawer(GravityCompat.START);
				navigationView.setCheckedItem(R.id.menu1);
				break;
			case R.id.scanner_toolbar_rightbutton:
				android.hardware.Camera camera = CameraManager.getCamera();
				android.hardware.Camera.Parameters parameter = camera.getParameters();
				// TODO FlashLight
				if (isOpen) {
					if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
					{
					}else {
						parameter.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
						camera.setParameters(parameter);
						isOpen = false;
					}
				} else {
					parameter.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameter);
					isOpen = true;
				}
				break;
            case R.id.preview_view:
                //若沒自動對焦，點擊螢幕對焦
                if(!SettingTools.isAutoFocus){
                    CameraManager.get().requestAutoFocus(handler, R.id.auto_focus);
                    //對焦後關閉自動對焦
                    autoFocusCallback.setHandler(null, 0);
                }
                break;
		}
	}

	public void showFragment(Fragment fragment,String tag){
		if (mFragmentManager == null) {
			mFragmentManager = getSupportFragmentManager();
		}
		mFragmentTransaction = mFragmentManager.beginTransaction();
		if (null == mFragmentManager.findFragmentByTag(tag)) {
			mFragmentTransaction.add(R.id.contentFragment, fragment, tag);
		}
		mFragmentTransaction.show(fragment);
		mFragmentTransaction.addToBackStack(null);
		mFragmentTransaction.commitAllowingStateLoss();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 0:
					//從相冊獲取圖片
					try{
						final Uri imageUri = data.getData();
						Log.e("imageUri:",imageUri+"");
						String selectPhoto = GetImageResult.getRealPathFromUri(this,imageUri);
						Log.e("selectPhoto:",selectPhoto);
						Bitmap photobitmap = GetImageResult.getBitmap(selectPhoto);
						Result result = GetImageResult.scanningImage(photobitmap);
						final String text = result.getText();
						scanResultDialog = new ScanResultDialog(this,text,photobitmap);
						scanResultDialog.show();
						SettingTools.todo(this,text,false);
					}catch (Exception e){
						scanResultDialog = new ScanResultDialog(this,"掃描失敗",null);
						scanResultDialog.show();
						e.printStackTrace();
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu1:
				drawerLayout.closeDrawer(GravityCompat.START);
				return true;
			case R.id.menu2:
				drawerLayout.closeDrawer(GravityCompat.START);
				Intent i = new Intent(Intent.ACTION_PICK, null);
				i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(i, 0);
				return true;
			case R.id.menu3:
				drawerLayout.closeDrawer(GravityCompat.START);
				showFragment(generateQRcodeFragment,"generateQRcode");
				return true;
			case R.id.menu4:
				drawerLayout.closeDrawer(GravityCompat.START);
				showFragment(settingFragment,"setting");
				return true;
		}
		return false;
	}

	private void CameraSetting(){
		for(int i =0;i<mData.size();i++){
			SettingBean data = mData.get(i);
			String s = data.getid();
			SettingTools.settingChange(s,data.getisCheck());
		}
	}
}