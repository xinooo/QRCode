package com.example.qrcode.zxing.activity;

import android.content.ClipData;
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
import com.example.qrcode.MainActivity;
import com.example.qrcode.Setting.SettingBean;
import com.example.qrcode.Setting.SettingFragment;
import com.example.qrcode.Setting.SettingTools;
import com.example.qrcode.ToastUtil;
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
	private ClipboardManager myClipboard;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.capture);
		// ViewUtil.addTopView(getApplicationContext(), this,
		// R.string.scan_card);
		CameraManager.init(getApplication());
		mCachePath = getFilesDir().getPath() + "/cache";
		generateQRcodeFragment = GenerateQRcodeFragment.newInstance();
		settingFragment = SettingFragment.newInstance();
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		menu = (ImageView) findViewById(R.id.scanner_toolbar_menu);
		flashlight = (ImageView) findViewById(R.id.scanner_toolbar_flashlight);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		menu.setOnClickListener(this);
		flashlight.setOnClickListener(this);
        surfaceView.setOnClickListener(this);
		navigationView.setNavigationItemSelectedListener(this);
		myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

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
		CameraSetting();
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
			case R.id.scanner_toolbar_menu:
				drawerLayout.openDrawer(GravityCompat.START);
				break;
			case R.id.scanner_toolbar_flashlight:
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
                if(!handler.isFocus){
                    CameraManager.get().requestAutoFocus(handler, R.id.auto_focus);
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
						ToastUtil.showMessageOnCenter(result.getText());
						//複製結果
						if(MainActivity.isClipData){
							ClipData myClip;
							String text = result.getText();
							myClip = ClipData.newPlainText("text", text);
							myClipboard.setPrimaryClip(myClip);
						}
					}catch (Exception e){
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
			switch (s){
				case "播放提示音":
					if(data.getisCheck()){

					}
					break;
				case "复制到剪贴板":
					MainActivity.isClipData = data.getisCheck();
					break;
				case "自动对焦":
					handler.isFocus = data.getisCheck();
					if(data.getisCheck()){
						CameraManager.get().requestAutoFocus(handler, R.id.auto_focus);
					}else {
						autoFocusCallback.setHandler(null, 0);
					}
					break;
				case "确定焦点":
					if(data.getisCheck()){

					}
					break;
				case "自动打开网页":
					if(data.getisCheck()){

					}
					break;
				case "反色":
					if(data.getisCheck()){

					}
					break;
			}
		}
	}
}