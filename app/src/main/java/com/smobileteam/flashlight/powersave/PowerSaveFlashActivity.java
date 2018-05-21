package com.smobileteam.flashlight.powersave;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.smobileteam.flashlight.FlashlightActivity;
import com.smobileteam.flashlight.FlashlightApplication;
import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.controller.CameraController;
import com.smobileteam.flashlight.controller.IStrobeLight;
import com.smobileteam.flashlight.controller.NotificationController;
import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.dialog.StandardDialog;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.PrefsUtils;

import java.io.IOException;

/**
 * Created by Duong Anh Son on 5/1/2017.
 * FlashlightBitbucket
 */

public class PowerSaveFlashActivity extends AppCompatActivity implements View.OnClickListener,
        IStrobeLight, SurfaceHolder.Callback,PowerSaveInteractor {
    public static final int DEFAULT_BATTERY_LEVEL_ENABLE_UPSM = 20;

    private Button mPowerBtn;
    private TextView mBatteryPercentTxt;
    private TextView mUsageTimeLeftTxt;
    private CameraController mCameraController;

    private SurfaceHolder mSurfaceHolder;

    private PowerSavePresenter mPowerSavePresenter;

    private int mBatteryStatus1 = 0;
    private int mBatteryStatus2 = 0;
    private long time1 = 0;
    private long time2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.power_save_flashlight_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.upsm_toolbar_title));
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.white));
        setSupportActionBar(toolbar);
        initUI();

        mPowerSavePresenter = new PowerSavePresenter(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PrefsUtils.getInstance(this).getBooleanParams(FlashlightConstant.FlashSetting.KEY_KEEP_SCREEN_ON)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCameraController = CameraController.getCameraControllerInstance();
        mCameraController.setCameraParams();
        if(FlashlightApplication.isFlashOn()
                || NotificationController.ACTION_TURN_ON_FLASHLIHT.equals(getIntent().getAction())){
            mCameraController.turnOnFlash(this);
            mPowerBtn.setSelected(true);
        }
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(PowerConnectionReceiver, iFilter);
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(PowerConnectionReceiver);
        if(FlashlightApplication.isFlashOn() && !PrefsUtils.getInstance(this).
                getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_AUTOMATIC_TURN_ON_LED)){
            if(mCameraController != null){
                mCameraController.turnOffFlash(this);
                mPowerBtn.setSelected(false);
            }
        }
        super.onStop();
    }

    private void initUI() {
        SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
        mSurfaceHolder = preview.getHolder();
        mSurfaceHolder.addCallback(this);

        mPowerBtn = (Button) findViewById(R.id.upsm_btn_power);
        mBatteryPercentTxt = (TextView) findViewById(R.id.upsm_txt_battery_percent);
        mUsageTimeLeftTxt = (TextView) findViewById(R.id.upsm_txt_usage_time_percent);

        mPowerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.upsm_btn_power:
                if (!mPowerBtn.isSelected()) {
                    mPowerBtn.setSelected(true);
                    mCameraController.turnOnFlash(this);
                } else {
                    mPowerBtn.setSelected(false);
                    mCameraController.turnOffFlash(this);
                }
                break;
        }
    }

    @Override
    public void setIndicatorOn() {
        //do nothing

    }

    @Override
    public void setIndicatorOff() {
        // do nothing
    }

    @Override
    public void turnOnFlashFailed() {
        StandardDialog dialog = new StandardDialog(this);
        dialog.setTopColorRes(R.color.darkBlueGrey);
        dialog.setIcon(R.drawable.ic_warning);
        dialog.setTitle(getString(R.string.warning_title));
        dialog.setMessage(R.string.msg_warning_device_not_support_flashlight);
        dialog.setPositiveButton(getString(R.string.string_ok), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ultra_power_saving_mode, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.disable_mode_menu:
                // Do whatever you want to do on logout click.
                PrefsUtils.getInstance(this).putBooleanParams(FlashlightConstant.FlashlightFeatures.ULTRA_POWER_SAVING_MODE,false);
                if(FlashlightApplication.isFlashOn() && !PrefsUtils.getInstance(this).
                        getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_AUTOMATIC_TURN_ON_LED)){
                    mCameraController.turnOffFlash(this);
                }
                PrefsUtils.getInstance(this).putBooleanParams(FlashlightConstant.FlashSetting.KEY_ALREADY_ENABLED_UPSM,false);
                finish();
                startActivity(new Intent(this, FlashlightActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        if (mCameraController != null && mCameraController.getCamera() != null) {
            try {
                mCameraController.getCamera().setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();


            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCameraController != null && mCameraController.getCamera() != null) {
            mCameraController.getCamera().stopPreview();
            mSurfaceHolder = null;
        }

    }

    @Override
    public void onBackPressed() {
        if (mCameraController != null){
            mCameraController.resetCamera();
        }
        super.onBackPressed();
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
            mBatteryStatus1 = level;
            time1 = System.currentTimeMillis();
            mPowerSavePresenter.caculateTimeRemaining(mBatteryStatus1,mBatteryStatus2,time1, time2);
            mBatteryStatus2 = mBatteryStatus1;
            time2 = System.currentTimeMillis();
            if (level == 5 || level == 4 || level == 3 || level == 2) {
                SoundClickController.warningSoundEffect();
            }
        }
    };

    @Override
    public void updateTimeRemaining(long time) {
        mUsageTimeLeftTxt.setText(CommonUtils.getDurationBreakdown(time));
    }
}
