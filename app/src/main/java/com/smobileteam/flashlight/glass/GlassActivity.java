package com.smobileteam.flashlight.glass;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.smobileteam.flashlight.FlashlightApplication;
import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.dialog.ProgressDialog;
import com.smobileteam.flashlight.dialog.StandardDialog;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.Logger;
import com.smobileteam.flashlight.utils.OsUtils;
import com.smobileteam.flashlight.utils.PrefsUtils;
import com.smobileteam.flashlight.view.AutofocusCrosshair;
import com.smobileteam.flashlight.view.MyCameraSurfaceView;
import com.smobileteam.flashlight.view.RoundedImageView;

/**
 * Created by Duong Anh Son on 10/13/2016.
 * Flashlight_v3
 */

public class GlassActivity extends Activity implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        GlassInteractor.IFlashlightControl,
        GlassInteractor.IGlassView,
        CompassAssistant.CompassAssistantListener{

    private Context mContext;
    private Camera myCamera;
    private Camera.Parameters params;
    private MyCameraSurfaceView myCameraSurfaceView;
    private FrameLayout myCameraPreview;
    private AppCompatImageView mBtnFlash;

    private AutofocusCrosshair mAutofocusCrosshair;

    private RoundedImageView mGallery;

    public boolean isFlashButtonSelected = false;
    private int mRotation;

    public boolean isFlashButtonSelected() {
        return isFlashButtonSelected;
    }

    public void setFlashButtonSelected(boolean flashButtonSelected) {
        isFlashButtonSelected = flashButtonSelected;
    }

    private GlassActivity mActivity;
    private TakePictureCallback mTakePictureCallback;

    private CompassAssistant mCompassPresenter;
    private float mCurrentDegree;
    private ImageView mCompassImage;
    private TextView mTrueNorthText;
    private AppCompatImageView mCompassBtn;
    private AppCompatSeekBar mSbZoomCamera;
    private boolean isCompassStopped = false;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mActivity = this;
        mCompassPresenter = new CompassAssistant(this);

        if(OsUtils.hasCameraPermissions(this)){
            new LoadingCameraTask().execute();
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    OsUtils.sCameraPermissions,
                    FlashlightConstant.PermissionRequestCode.RQ_GLASSACTIVITY_CAMERA_PERMISSION);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_KEEP_SCREEN_ON)){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void turnOnFlash() {
        if (!FlashlightApplication.isFlashOn()) {
            if (myCamera == null) {
                return;
            }
            params = myCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            myCamera.setParameters(params);
            FlashlightApplication.setIsFlashOn(true);
        }
    }

    @Override
    public void turnOffFlash() {
        if (FlashlightApplication.isFlashOn()) {
            if (myCamera == null || params == null) {
                return;
            }
            params = myCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            myCamera.setParameters(params);
            FlashlightApplication.setIsFlashOn(false);

        }
    }

    @Override
    public void updateImageForGalleryView(Bitmap bitmap) {
        mGallery.setImageBitmap(bitmap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCompassPresenter != null){
            if(mCompassPresenter.isSupported()){
                mCompassPresenter.start();
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCompassPresenter != null){
            mCompassPresenter.stop();
        }
    }

    @Override
    public void onNewDegreesToNorth(float degrees) {
        // this is not used here because we want to have a smooth moving mCompassImage.
    }

    @Override
    public void onNewSmoothedDegreesToNorth(float degrees) {
        final RotateAnimation ra = new RotateAnimation(
                mCurrentDegree,
                degrees,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(210);
        ra.setFillAfter(true);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!isCompassStopped){
                    mCompassImage.startAnimation(ra);
                }
            }
        });
        mCurrentDegree = degrees;

    }

    @Override
    public void onCompassStopped() {
        Logger.d("onCompassStopped");
        isCompassStopped = true;
        mCompassImage.clearAnimation();
        mCompassImage.setVisibility(View.GONE);
        mTrueNorthText.setVisibility(View.GONE);
    }

    @Override
    public void onCompassStarted() {
        Logger.d("onCompassStarted");
        isCompassStopped = false;
        mCompassImage.setVisibility(View.VISIBLE);
        mTrueNorthText.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateBearingText(String bearing) {
        if(mTrueNorthText != null){
            mTrueNorthText.setText(bearing);
        }
    }


    private class LoadingCameraTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog pd = null;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(mContext);
            pd.setTopColorRes(R.color.darkBlueGrey);
            pd.setIcon(R.drawable.ic_progress);
            pd.setTitle(mContext.getString(R.string.msg_loading));
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Pass the result data back to the main activity
            if (this.pd != null) {
                pd.dismiss();
            }
            setContentView(R.layout.glass_activity);

            initView();
            myCameraSurfaceView = new MyCameraSurfaceView(mActivity, mContext, myCamera);
            myCameraSurfaceView.setDrawingView(mAutofocusCrosshair);
            myCameraPreview.addView(myCameraSurfaceView);
            mRotation = myCameraSurfaceView.getCameraDisplayOrientation();
            mTakePictureCallback = new TakePictureCallback(mActivity,myCamera,GlassActivity.this);
            mTakePictureCallback.setGlassViewInterface(GlassActivity.this);
            mTakePictureCallback.setView(mBtnFlash);
            mTakePictureCallback.setRotation(mRotation);
            if(mCompassPresenter.isSupported()){
                mCompassPresenter.addListener(GlassActivity.this);
            } else {
                mCompassImage.setVisibility(View.GONE);
                mTrueNorthText.setVisibility(View.GONE);
                mCompassBtn.setVisibility(View.GONE);
            }

        }

        @Override
        protected Boolean doInBackground(String... params) {
            //Do all your slow tasks here but dont set anything on UI
            //ALL ui activities on the main thread
            myCamera = getCameraInstance();
            return true;

        }

    }

    public void initView() {
        myCameraPreview = (FrameLayout) findViewById(R.id.flPreview);
        AppCompatImageView mBtnBack = (AppCompatImageView) findViewById(R.id.btnBack);
        mBtnFlash = (AppCompatImageView) findViewById(R.id.btnFlash);
        mSbZoomCamera = (AppCompatSeekBar) findViewById(R.id.sbZoomCamera);
        mSbZoomCamera.setMax(getMaxZoomCamera());
        AppCompatImageView mBtnCapture = (AppCompatImageView) findViewById(R.id.btnCaptureImage);
        mGallery = (RoundedImageView) findViewById(R.id.thumbnail);
        mAutofocusCrosshair = (AutofocusCrosshair) findViewById(R.id.af_crosshair);
        mCompassImage = (ImageView) findViewById(R.id.imageViewCompass);
        mTrueNorthText = (TextView) findViewById(R.id.glass_activity_txt_true_north);
        mCompassBtn = (AppCompatImageView) findViewById(R.id.btn_compass);
        AppCompatImageView rotateCameraBtn = (AppCompatImageView) findViewById(R.id.btn_rotate_camera);

        mBtnBack.setOnClickListener(this);
        mBtnFlash.setOnClickListener(this);
        mBtnCapture.setOnClickListener(this);
        mSbZoomCamera.setOnSeekBarChangeListener(this);
        mGallery.setOnClickListener(this);
        mCompassBtn.setOnClickListener(this);
        rotateCameraBtn.setOnClickListener(this);
        showCompass();
    }

    private void showCompass(){
        mCompassPresenter.start();
        mCompassBtn.setSelected(true);
    }
    private void hideCompass(){
        mCompassPresenter.stop();
        mCompassBtn.setSelected(false);
    }

    public int getMaxZoomCamera() {
        if(myCamera != null){
            Camera.Parameters parameters = myCamera.getParameters();
            return parameters.getMaxZoom();
        } else {
            return 10;
        }


    }

    public void zoom(int zoomSize) {
        if(myCamera != null){
            Camera.Parameters parameters = myCamera.getParameters();
            int size = parameters.getMaxZoom();
            if (zoomSize < size) {
                parameters.setZoom(zoomSize);
                myCamera.setParameters(parameters);
            }
        }

    }


    private Camera getCameraInstance() {
        // TODO Auto-generated method stub
        Camera c = null;
        try {
            c = Camera.open(currentCameraId); // attempt to get a Camera instance

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Logger.e("Camera is not available (in use or does not exist)");
        }
        return c; // returns null if camera is unavailable
    }

    private void releaseCamera() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (myCamera != null) {
                    myCamera.stopPreview();
                    myCamera.setPreviewCallback(null);
                    myCamera.release();
                    myCameraSurfaceView.setPreview(false);
                    FlashlightApplication.setIsFlashOn(false);
                    myCamera = null;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        releaseCamera();
        super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_SOUND)) {
            SoundClickController.clickSoundEffect();
        }
        switch (v.getId()) {
            case R.id.btnBack:
                releaseCamera();
                finish();
                break;
            case R.id.btnFlash:
                if (isFlashButtonSelected) {
                    isFlashButtonSelected = false;
                    turnOffFlash();
                    mBtnFlash.setSelected(false);
                } else {
                    isFlashButtonSelected = true;
                    turnOnFlash();
                    mBtnFlash.setSelected(true);
                }

                break;
            case R.id.btnCaptureImage:
                if(!OsUtils.hasStoragePermissions(this)){
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            OsUtils.sStoragePermissions,
                            FlashlightConstant.PermissionRequestCode.RQ_GLASSACTIVITY_STORATE_PERMISSION);

                } else {
                    myCamera.takePicture(null,null,mTakePictureCallback);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.thumbnail:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_VIEW );
                startActivity(intent);
                break;
            case R.id.btn_compass:
                if(mCompassBtn.isSelected()){
                    hideCompass();
                } else {
                    showCompass();
                }
                break;
            case R.id.btn_rotate_camera:
                if (myCameraSurfaceView.isPreview()) {
                    myCameraSurfaceView.surfaceDestroyed(myCameraSurfaceView.getHolder());
                    myCameraSurfaceView.getHolder().removeCallback(myCameraSurfaceView);
                    myCameraSurfaceView.destroyDrawingCache();
                    myCameraPreview.removeView(myCameraSurfaceView);
                    myCamera.stopPreview();
                    myCamera.setPreviewCallback(null);
                    myCamera.release();
                    myCameraSurfaceView.setPreview(false);
                }
                //swap the id of the camera to be used
                if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    mBtnFlash.setVisibility(View.INVISIBLE);
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    mBtnFlash.setVisibility(View.VISIBLE);
                }

                myCamera = getCameraInstance();
                myCameraSurfaceView = new MyCameraSurfaceView(this, mContext, myCamera);
                myCameraSurfaceView.setDrawingView(mAutofocusCrosshair);
                myCameraSurfaceView.setCurrentCameraId(currentCameraId);
                myCameraPreview.addView(myCameraSurfaceView);
                mRotation = myCameraSurfaceView.getCameraDisplayOrientation();
                mTakePictureCallback.setMyCamera(myCamera);
                mTakePictureCallback.setCameraId(currentCameraId);
                mTakePictureCallback.setRotation(mRotation);
//                myCamera.setDisplayOrientation(mRotation);
                mSbZoomCamera.setProgress(0);// reset Seekbar

                break;
            default:
                break;
        }
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FlashlightConstant.PermissionRequestCode.RQ_GLASSACTIVITY_STORATE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    myCamera.takePicture(null,null,mTakePictureCallback);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (!showRationale) {
                    new StandardDialog(this)
                            .setTopColorRes(R.color.darkBlueGrey)
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setTitle(R.string.warning_title)
                            .setMessage(R.string.msg_explain_permission_storage)
                            .setCancelable(false)
                            .setPositiveButton(R.string.string_ok,null)
                            .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            }).show();


                } else {
                    new StandardDialog(this)
                            .setTopColorRes(R.color.darkBlueGrey)
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setTitle(R.string.warning_title)
                            .setMessage(R.string.msg_explain_permission_storage)
                            .setCancelable(false)
                            .setPositiveButton(R.string.string_ok,null)
                            .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(GlassActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            FlashlightConstant.PermissionRequestCode.RQ_FLASHACTIVITY_CAMERA_PERMISSION);
                                }
                            }).show();
                }
                break;
            case FlashlightConstant.PermissionRequestCode.RQ_GLASSACTIVITY_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new LoadingCameraTask().execute();
                    return;
                }

                boolean showCameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA);
                if (!showCameraRationale) {
                    showExplainCameraPermission();
                } else {
                    new StandardDialog(this)
                            .setTopColorRes(R.color.darkBlueGrey)
                            .setIcon(android.R.drawable.stat_sys_warning)
                            .setTitle(R.string.warning_title)
                            .setMessage(R.string.msg_explain_permission_camera_for_glass)
                            .setCancelable(false)
                            .setPositiveButton(R.string.string_ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(GlassActivity.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            FlashlightConstant.PermissionRequestCode.RQ_GLASSACTIVITY_CAMERA_PERMISSION);
                                }
                            }).show();
                }

                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FlashlightConstant.PermissionRequestCode.RQ_PERMISSION_SETTING_APP){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                showExplainCameraPermission();

            } else {
                new LoadingCameraTask().execute();
            }
        }
    }

    private void showExplainCameraPermission(){
        new StandardDialog(this)
                .setTopColorRes(R.color.darkBlueGrey)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setTitle(R.string.warning_title)
                .setMessage(R.string.msg_explain_permission_camera_for_glass)
                .setCancelable(false)
                .setPositiveButton(R.string.string_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent,
                                FlashlightConstant.PermissionRequestCode.RQ_PERMISSION_SETTING_APP);
                    }
                }).show();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        zoom(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            releaseCamera();
            finish();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        releaseCamera();
        super.onDestroy();
    }
}

