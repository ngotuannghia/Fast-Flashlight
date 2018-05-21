package com.smobileteam.flashlight;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.smobileteam.flashlight.controller.CameraController;
import com.smobileteam.flashlight.controller.IStrobeLight;
import com.smobileteam.flashlight.controller.NotificationController;
import com.smobileteam.flashlight.dialog.RatingDialog;
import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.controller.StrobeLightController;
import com.smobileteam.flashlight.dialog.StandardDialog;
import com.smobileteam.flashlight.glass.GlassActivity;
import com.smobileteam.flashlight.powersave.PowerSaveFlashActivity;
import com.smobileteam.flashlight.screenlight.ScreenLightActivity;
import com.smobileteam.flashlight.setting.SettingsActivity;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.Logger;
import com.smobileteam.flashlight.utils.OsUtils;
import com.smobileteam.flashlight.utils.PrefsUtils;
import com.smobileteam.flashlight.view.StrobeView;

import java.io.IOException;


/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight v3
 */

public class FlashlightActivity extends Activity implements View.OnClickListener,
        StrobeView.OnWheelItemSelectedListener, IStrobeLight, SurfaceHolder.Callback {
    private Context mContext;
    private AppCompatImageView mPowerButton;
    private StrobeView mNumberStrobeView;
    private TextSwitcher mNumberStrobeTextView;
    private AppCompatImageView mIndicatorButton;
    private AppCompatImageView mBatteryView;
    private TextView mBatteryPercentTxt;
    private AppCompatImageView mLockButton;
    private FrameLayout mDisableClickFrame;
    private AppCompatImageView mQuickControllerBtn;

    private int mNumberStrobe = 0;

    private CameraController mCameraController;
    private StrobeLightController mStrobeController;

    private Thread mThread;
    private SurfaceHolder mSurfaceHolder;

    private boolean isHomePress;
    private boolean isFlashOnWhenHomePress;
    private IStrobeLight iStrobeLight;
    private NotificationController mQuickController;

    private boolean isStartUPSM = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        if(PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_ALREADY_ENABLED_UPSM)){
            isStartUPSM = true;
            startActivity(new Intent(this, PowerSaveFlashActivity.class));
            finish();
            return;
        }
        iStrobeLight = this;
        setContentView(R.layout.flashlight_activity);
        mQuickController = new NotificationController(this);
        initView();
        initData();
        if (!CommonUtils.isFlashSupported(this)) {
            //beacause dialog use animation when show, so add sleep to avoid hang animation
            CommonUtils.sleep(FlashlightConstant.DELAY.DELAY_300);
            showNoFlashAlert();

        } else {
            if (!OsUtils.hasCameraPermissions(this)) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        OsUtils.sCameraPermissions,
                        FlashlightConstant.PermissionRequestCode.RQ_FLASHACTIVITY_CAMERA_PERMISSION);
            } else {
                mCameraController = CameraController.getCameraControllerInstance();
                mCameraController.setCameraParams();
                mStrobeController = StrobeLightController.getInstance(mCameraController);
                mStrobeController.setIndicatorView(this);
                checkAutoTurnonFlash();
            }

        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean isWidgetTurnon = bundle.getBoolean("widget_turn_on", false);
            if (isWidgetTurnon) {
               activeFlashlight();
            }
        }
        if(NotificationController.ACTION_TURN_ON_FLASHLIHT.equals(getIntent().getAction())){
            activeFlashlight();
        }
        //Default Quick controller is ENABLE
        boolean enable_quick_controller = PrefsUtils.getInstance(this)
                .getBooleanParams(FlashlightConstant.FlashlightFeatures.ENABLE_QUICK_CONTROLLER, true);

        mQuickControllerBtn.setSelected(enable_quick_controller);
        if (enable_quick_controller) {
            mQuickController.setView(findViewById(R.id.flashlight_btn_quick_controller));
            mQuickController.setShowOverlayTooltips(true);
            mQuickController.buildNotification();
        }

        new AppUpdater(this)
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .start();
    }


    private void activeFlashlight(){
        try {
            mPowerButton.setSelected(true);
            if (mNumberStrobe > 0) {
                startStrobeMode(mNumberStrobe);
            } else {
                mCameraController.turnOnFlash(iStrobeLight);
            }
            mStrobeController.isRequestTurnOffLight = false;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FlashlightConstant.PermissionRequestCode.RQ_FLASHACTIVITY_CAMERA_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do it
                    mCameraController = CameraController.getCameraControllerInstance();
                    mCameraController.setCameraParams();
                    mStrobeController = StrobeLightController.getInstance(mCameraController);
                    mStrobeController.setIndicatorView(this);
                    SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
                    mSurfaceHolder = preview.getHolder();
                    mSurfaceHolder.addCallback(this);
                    surfaceCreated(mSurfaceHolder);
                    checkAutoTurnonFlash();
                    return;
                }
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA);
                if (!showRationale) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    showExplainPermission();
                } else {
                    new StandardDialog(this)
                            .setTopColorRes(R.color.darkBlueGrey)
                            .setIcon(R.drawable.ic_warning)
                            .setTitle(getString(R.string.warning_title))
                            .setMessage(getString(R.string.msg_explain_permission_camera))
                            .setCancelable(false)
                            .setPositiveButton(R.string.string_ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(mContext, ScreenLightActivity.class));
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(FlashlightActivity.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            FlashlightConstant.PermissionRequestCode.RQ_FLASHACTIVITY_CAMERA_PERMISSION);
                                }
                            })
                            .show();
                }
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FlashlightConstant.PermissionRequestCode.RQ_PERMISSION_SETTING_APP) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                showExplainPermission();
            } else {
                mCameraController = CameraController.getCameraControllerInstance();
                mCameraController.setCameraParams();
                mStrobeController = StrobeLightController.getInstance(mCameraController);
                mStrobeController.setIndicatorView(this);
                SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
                mSurfaceHolder = preview.getHolder();
                mSurfaceHolder.addCallback(this);
                surfaceCreated(mSurfaceHolder);
            }
        }
    }

    private void checkAutoTurnonFlash() {
        if (PrefsUtils.getInstance(this).
                getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_AUTOMATIC_TURN_ON_LED)) {
            mCameraController.turnOnFlash(this);
            mPowerButton.setSelected(true);
        }
    }


    private void initView() {
        SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
        mSurfaceHolder = preview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPowerButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_power);
        mNumberStrobeView = (StrobeView) findViewById(R.id.flashlight_strobeview);
        mNumberStrobeTextView = (TextSwitcher) findViewById(R.id.flashlight_txt_numberstrobe);
        mBatteryView = (AppCompatImageView) findViewById(R.id.battery_img_icon);
        mPowerButton.setOnClickListener(this);
        mNumberStrobeView.setOnWheelItemSelectedListener(this);
        mNumberStrobeTextView.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                TextView myText = new TextView(getApplicationContext());
                myText.setGravity(Gravity.CENTER);

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER);
                myText.setLayoutParams(params);

                myText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                myText.setTextColor(Color.WHITE);
                return myText;
            }
        });
        mNumberStrobeTextView.setInAnimation(AnimationUtils.loadAnimation(this,
                R.anim.push_up_in));
        mNumberStrobeTextView.setOutAnimation(this, R.anim.push_up_out);

        AppCompatImageView screenlightButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_screenlight);
        AppCompatImageView glassButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_glass);
        AppCompatImageView settingButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_setting);
        mIndicatorButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_indicator);
        mDisableClickFrame = (FrameLayout) findViewById(R.id.flashlight_fl_disableclick);
        mBatteryPercentTxt = (TextView) findViewById(R.id.battery_txt_percent);

        mLockButton = (AppCompatImageView) findViewById(R.id.flashlight_btn_lock);
        mQuickControllerBtn = (AppCompatImageView) findViewById(R.id.flashlight_btn_quick_controller);
        screenlightButton.setOnClickListener(this);
        glassButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        mLockButton.setOnClickListener(this);
        mQuickControllerBtn.setOnClickListener(this);
    }

    private void initData() {
        mNumberStrobeTextView.setText("" + mNumberStrobe);
    }

    @Override
    public void onClick(View v) {
        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_SOUND)) {
            SoundClickController.clickSoundEffect();
        }
        switch (v.getId()) {
            case R.id.flashlight_btn_power:
                if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_VIBRATION)) {
                    CommonUtils.vibrate(this);
                }
                if (!mPowerButton.isSelected()) {
                    mPowerButton.setSelected(true);
                    if (mNumberStrobe > 0) {
                        startStrobeMode(mNumberStrobe);
                    } else {
                        mCameraController.turnOnFlash(iStrobeLight);
                    }
                    mStrobeController.isRequestTurnOffLight = false;
                } else {
                    mPowerButton.setSelected(false);
                    mPowerButton.setPressed(true);
                    mStrobeController.requestStop = true;
                    mStrobeController.isRequestTurnOffLight = true;
                    releaseStrobeMode();
                    mCameraController.turnOffFlash(iStrobeLight);
                    // Show rate dialog
                    showRateDialog();
                }
                break;
            case R.id.flashlight_btn_screenlight:
                startActivity(new Intent(this, ScreenLightActivity.class));
                break;
            case R.id.flashlight_btn_glass:
                mStrobeController.requestStop = true;
                mStrobeController.isRequestTurnOffLight = true;
                releaseStrobeMode();
                mCameraController.turnOffFlash(this);
                mCameraController.resetCamera();
                mPowerButton.setSelected(false);
                mIndicatorButton.setSelected(false);
                startActivity(new Intent(this, GlassActivity.class));
                overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                break;
            case R.id.flashlight_btn_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.flashlight_btn_lock:
                mLockButton.setSelected(!mLockButton.isSelected());
                lockApp(mLockButton.isSelected());
                break;
            case R.id.flashlight_btn_quick_controller:
                if (mQuickControllerBtn.isSelected()) {
                    PrefsUtils.getInstance(this).putBooleanParams(
                            FlashlightConstant.FlashlightFeatures.ENABLE_QUICK_CONTROLLER, false);
                    mQuickControllerBtn.setSelected(false);
                    mQuickController.setView(v);
                    mQuickController.setShowOverlayTooltips(false);
                    mQuickController.hideNotification(NotificationController.NOTIFICATION_REMOTE_FLASHLIGHT);
                } else {
                    PrefsUtils.getInstance(this).putBooleanParams(
                            FlashlightConstant.FlashlightFeatures.ENABLE_QUICK_CONTROLLER, true);
                    mQuickControllerBtn.setSelected(true);
                    mQuickController.setView(v);
                    mQuickController.setShowOverlayTooltips(false);
                    mQuickController.buildNotification();

                }

                break;
            default:
                break;
        }
    }

    private void showRateDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .session(6) // Sau khi nhan 6 lan se show dialog rate
                .threshold(4) // Nguoi cho 4 sao tro len moi nhay sang Google Play con khong thi hien feedback
                .ratingBarColor(R.color.colorAccent)
                .positiveButtonTextColor(R.color.black)
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", "anhson.duong@gmail.com", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                mContext.getString(R.string.app_name) + " ("
                                        + CommonUtils.getVersion(mContext) + "|"
                                        + CommonUtils.getDeviceName() + "): "
                                        + mContext.getString(R.string.about_app_feedback_title));
                        emailIntent.putExtra(Intent.EXTRA_TEXT, feedback);
                        try {
                            mContext.startActivity(Intent.createChooser(emailIntent, mContext.getString(R.string.feedback) + "..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            CommonUtils.showToast(mContext, mContext.getString(R.string.app_feedback_exception_no_app_handle));
                        }
                    }
                })
                .build();


        ratingDialog.show();
    }

    private boolean startStrobeMode(int numberStrobe) {
        mStrobeController.numberStrobePerSecond = numberStrobe;
        if (mThread == null) {
            mThread = new Thread(mStrobeController);
            mThread.start();
        }
        return true;
    }

    private void releaseStrobeMode() {
        if (mThread != null) mThread.interrupt();
        mThread = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d("onStart");
        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_KEEP_SCREEN_ON)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d("onResume");

        if (mCameraController != null && mCameraController.getCamera() == null) {
            mCameraController = CameraController.getCameraControllerInstance();
            mCameraController.setCameraParams();
            mStrobeController = StrobeLightController.getInstance(mCameraController);
            mStrobeController.setIndicatorView(this);
        }

        if (isHomePress && isFlashOnWhenHomePress) {
            isHomePress = false;
            isFlashOnWhenHomePress = false;

            mCameraController = CameraController.getCameraControllerInstance();
            mCameraController.setCameraParams();
            mStrobeController = StrobeLightController.getInstance(mCameraController);
            mStrobeController.setIndicatorView(this);

            if (mNumberStrobe > 0) {
                startStrobeMode(mNumberStrobe);
            } else {
                mCameraController.turnOnFlash(this);
            }
            mPowerButton.setSelected(true);
            mStrobeController.isRequestTurnOffLight = false;
        }

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(PowerConnectionReceiver, iFilter);
    }


    @Override
    protected void onStop() {
        Logger.d("onStop");
        if (FlashlightApplication.isFlashOn()
                || (mNumberStrobe > 0 && mPowerButton.isSelected())) {
            isHomePress = true;
            isFlashOnWhenHomePress = true;

            if (mStrobeController != null) {
                mStrobeController.requestStop = true;
                mStrobeController.isRequestTurnOffLight = true;
            }

            releaseStrobeMode();
            if(!PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashlightFeatures.ULTRA_POWER_SAVING_MODE)){
                if (mCameraController != null) {
                    mCameraController.resetCamera();
                }
            }

        }
        this.unregisterReceiver(PowerConnectionReceiver);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        if (mStrobeController != null) {
            mStrobeController.requestStop = true;
            mStrobeController.isRequestTurnOffLight = true;
        }

        releaseStrobeMode();
        if (mCameraController != null && !isStartUPSM) {
            mCameraController.resetCamera();
            isStartUPSM = false;
        }
        isHomePress = false;
        super.onDestroy();
    }

    public void showNoFlashAlert() {
        StandardDialog dialog = new StandardDialog(this);
        dialog.setTopColorRes(R.color.darkBlueGrey);
        dialog.setIcon(R.drawable.ic_warning);
        dialog.setTitle(getString(R.string.warning_title));
        dialog.setMessage(R.string.msg_warning_device_not_support_flashlight);
        dialog.setPositiveButton(getString(R.string.string_ok), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(mContext, ScreenLightActivity.class));
            }
        });
        dialog.setCancelable(false);
        dialog.show();

    }

    private void showExplainPermission() {
        new StandardDialog(this)
                .setTopColorRes(R.color.darkBlueGrey)
                .setIcon(R.drawable.ic_warning)
                .setTitle(getString(R.string.warning_title))
                .setMessage(getString(R.string.msg_explain_permission_camera))
                .setCancelable(false)
                .setPositiveButton(R.string.string_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(mContext, ScreenLightActivity.class));
                        finish();
                    }
                })
                .setNegativeButton(R.string.button_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, FlashlightConstant.PermissionRequestCode.RQ_PERMISSION_SETTING_APP);
                    }
                })
                .show();
    }

    @Override
    public void onWheelItemChanged(StrobeView wheelView, int position) {
        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_SOUND)) {
            SoundClickController.moveSoundEffect();
        }
        mNumberStrobeTextView.setText("" + wheelView.getItems().get(position));
    }

    @Override
    public void onWheelItemSelected(StrobeView wheelView, int position) {
        mNumberStrobe = Integer.parseInt(wheelView.getItems().get(position));

        if (mPowerButton.isSelected()) {
            if (mNumberStrobe != 0) {
                startStrobeMode(mNumberStrobe);
            } else {
                mStrobeController.requestStop = true;
                releaseStrobeMode();
                CommonUtils.sleep(FlashlightConstant.DELAY.DELAY_200);
                mCameraController.turnOnFlash(this);
            }
        }

    }

    @Override
    public void setIndicatorOn() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (FlashlightApplication.isFlashOn()) mIndicatorButton.setSelected(true);
                Logger.d("setIndicatorOn");
            }
        });

    }

    @Override
    public void setIndicatorOff() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!FlashlightApplication.isFlashOn()) mIndicatorButton.setSelected(false);
                Logger.d("setIndicatorOff");
            }
        });

    }

    @Override
    public void turnOnFlashFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStrobeController.requestStop = true;
                mStrobeController.isRequestTurnOffLight = true;
                // reset strobe
                mNumberStrobe = 0;
                mNumberStrobeView.selectIndex(0);
                mNumberStrobeTextView.setText("0");
                mPowerButton.setSelected(false);
                CommonUtils.showToast(mContext, getString(R.string.error_flashlight_busy));
                Logger.d("turnOnFlashFailed");
            }
        });
        Intent intent = new Intent(this, ScreenLightActivity.class);
        intent.putExtra("isFlashNotSupport", true);
        startActivity(intent);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d("SurfaceCreated");
        mSurfaceHolder = holder;
        if (mCameraController != null && mCameraController.getCamera() != null) {
            try {
                mCameraController.getCamera().setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.d("surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCameraController != null && mCameraController.getCamera() != null) {
            mCameraController.getCamera().stopPreview();
            mSurfaceHolder = null;
        }

    }

    private void lockApp(boolean isLock) {
        mDisableClickFrame.setVisibility(isLock ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {

        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_ASK_ON_QUIT)) {
            new StandardDialog(this)
                    .setTopColorRes(R.color.darkBlueGrey)
                    .setIcon(R.drawable.ic_warning)
                    .setTitle(getString(R.string.quit))
                    .setMessage(getString(R.string.quit_confirm_message))
                    .setPositiveButton(getString(R.string.string_exit), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.string_cancel),null)
                    .show();

        } else {
            super.onBackPressed();
        }
    }

    // Initialize a new BroadcastReceiver instance
    private BroadcastReceiver PowerConnectionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the battery scale
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            // get the battery level
            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            // Calculate the battery charged percentage
            float percentage = rawlevel / (float) scale;
            int level = (int) ((percentage) * 100);
            mBatteryPercentTxt.setText(level + "%");
/*            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;*/
            if(OsUtils.hasCameraPermissions(mContext)
                    && PrefsUtils.getInstance(mContext).
                    getBooleanParams(FlashlightConstant.FlashlightFeatures.ULTRA_POWER_SAVING_MODE, true)
                    && (level <= PrefsUtils.getInstance(mContext).
                    getIntParams(FlashlightConstant.FlashSetting.KEY_BATTERY_LEVEL,
                            PowerSaveFlashActivity.DEFAULT_BATTERY_LEVEL_ENABLE_UPSM))){
                Toast.makeText(mContext,mContext.getString(R.string.upsm_msg_enable_mode,
                        PrefsUtils.getInstance(mContext).getIntParams(FlashlightConstant.FlashSetting.KEY_BATTERY_LEVEL)),
                        Toast.LENGTH_LONG).show();

                boolean enable_quick_controller = PrefsUtils.getInstance(mContext)
                        .getBooleanParams(FlashlightConstant.FlashlightFeatures.ENABLE_QUICK_CONTROLLER, true);
                if (enable_quick_controller) {
                    mQuickController.buildNotification(true);
                }
                PrefsUtils.getInstance(mContext).putBooleanParams(FlashlightConstant.FlashSetting.KEY_ALREADY_ENABLED_UPSM,true);
                isStartUPSM = true;
                startActivity(new Intent(mContext, PowerSaveFlashActivity.class));
                finish();

                // Bug: Start 2 lan
            }
            if (level <= 5) {
                mBatteryPercentTxt.setTextColor(ContextCompat.getColor(context, R.color.red_btn_bg_color));
                final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                animation.setDuration(300); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
                animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
                mBatteryView.startAnimation(animation);
            } else {
                mBatteryPercentTxt.setTextColor(ContextCompat.getColor(context, R.color.white));
                mBatteryView.clearAnimation();
            }
            if (level == 5 || level == 4 || level == 3 || level == 2) {
                SoundClickController.warningSoundEffect();
            }
        }
    };
}
