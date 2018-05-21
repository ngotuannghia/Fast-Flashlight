package com.smobileteam.flashlight.screenlight;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.controller.NotificationController;
import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.PrefsUtils;
import com.smobileteam.flashlight.view.Tooltip;

/**
 * Created by Duong Anh Son on 10/13/2016.
 * Flashlight_v3
 */

public class ScreenLightActivity extends FragmentActivity implements View.OnClickListener,
        ScreenLightInteractor.ScreenLightView, SeekBar.OnSeekBarChangeListener {

    private View mScreenColor;
    private PrefsUtils mPrefs;
    private ScreenLightPresenter mScreenlightPresenter;
    private Button mSosBtn;
    private Button mColorBtn;
    private boolean isFromWidget;
    private boolean isFlashNotSupport;
    private ModeScreenLight mModeScreenlight = ModeScreenLight.LIGHT;

    public ModeScreenLight getmModeScreenlight() {
        return mModeScreenlight;
    }

    public void setmModeScreenlight(ModeScreenLight mModeScreenlight) {
        this.mModeScreenlight = mModeScreenlight;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean b) {
        if(progresValue <= 10){
            progresValue = 10;
        }
        float backLightValue = (float) progresValue / 100;
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = backLightValue;
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
        setContentView(R.layout.screen_light_activity);
        mPrefs = PrefsUtils.getInstance(this);
        mScreenlightPresenter = new ScreenLightPresenter(this);
        initView();


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isFromWidget = bundle.getBoolean("widget_full", false);
            isFlashNotSupport = bundle.getBoolean("isFlashNotSupport",false);
            activeLightModeScreen();
        }
        if(NotificationController.ACTION_TURN_ON_SCREENLIGHT.equals(getIntent().getAction())){
            activeLightModeScreen();
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

    private void initView() {
        mScreenColor = findViewById(R.id.screenlight);
        AppCompatImageView backBtn = (AppCompatImageView) findViewById(R.id.screenlight_img_back);
        ImageView powerBtn = (ImageView) findViewById(R.id.screenlight_img_power);
        mSosBtn = (Button) findViewById(R.id.screenlight_btn_sos);
        mColorBtn = (Button) findViewById(R.id.screenlight_btn_color);
        AppCompatSeekBar brightestLevel = (AppCompatSeekBar) findViewById(R.id.screenlight_sb_brightestlevel);
        brightestLevel.setOnSeekBarChangeListener(this);
        brightestLevel.setProgress(100);

        powerBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        mSosBtn.setOnClickListener(this);
        mColorBtn.setOnClickListener(this);
        mScreenColor.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onSingleClick(View v) {

            }

            @Override
            public void onDoubleClick(View v) {
                if (mModeScreenlight == ModeScreenLight.SOS) {
                    mScreenlightPresenter.requestStop = true;
                    mScreenlightPresenter.stopSosModeScreenlight();
                }
                mColorBtn.setVisibility(View.VISIBLE);
                mSosBtn.setVisibility(View.VISIBLE);
                mScreenColor.setVisibility(View.GONE);
            }
        });
    }

    // This could be moved into an abstract BaseActivity
    // class for being re-used by several instances
    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(fragment.getTag());
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.screenlight_img_back:
                if (mPrefs.getBooleanParams(FlashlightConstant.FlashSetting.ENABLE_SOUND)) {
                    SoundClickController.clickSoundEffect();
                }
                finish();
                if (!isFromWidget) {
                    overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                }
                break;
            case R.id.screenlight_img_power:
                showTooltips(findViewById(R.id.screenlight_guide),getString(R.string.tips_how_to_exit));
                if (mScreenColor.getVisibility() == View.GONE) {
                    switch (mModeScreenlight) {
                        case SOS:
                            mScreenlightPresenter.startSosModeScreenlight();
                            break;
                        case COLOR:
                            setFragment(new ColorScreenFragment());
                            break;
                        default:
                            activeLightModeScreen();
                            break;
                    }
                    mScreenColor.setVisibility(View.VISIBLE);
                    mSosBtn.setVisibility(View.GONE);
                    mColorBtn.setVisibility(View.GONE);
                } else {
                    mScreenColor.setVisibility(View.GONE);
                    mSosBtn.setVisibility(View.VISIBLE);
                    mColorBtn.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.screenlight_btn_sos:
                if (!mSosBtn.isSelected()) {
                    mSosBtn.setSelected(true);
                    mColorBtn.setSelected(false);
                    mModeScreenlight = ModeScreenLight.SOS;
                } else {
                    mSosBtn.setSelected(false);
                    mModeScreenlight = ModeScreenLight.LIGHT;
                }

                break;
            case R.id.screenlight_btn_color:
                if (!mColorBtn.isSelected()) {
                    mColorBtn.setSelected(true);
                    mSosBtn.setSelected(false);
                    mModeScreenlight = ModeScreenLight.COLOR;
                } else {
                    mColorBtn.setSelected(false);
                    mModeScreenlight = ModeScreenLight.LIGHT;
                }
                break;
            default:
                break;


        }
    }

    private void showTooltips(View v,String content){
        Tooltip.make(
                this,
                new Tooltip.Builder(102)
                        .anchor(v, Tooltip.Gravity.BOTTOM)
                        .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME, 3000)
                        .text(content)
                        .fadeDuration(200)
                        .fitToScreen(true)
                        .maxWidth(CommonUtils.dpToPixels(310))
                        .showDelay(200)
                        .withArrow(false)
                        .withOverlay(false)
                        .build()
        ).show();
    }

    @Override
    public void onBackPressed() {
        switch (mModeScreenlight) {
            case COLOR:
                getSupportFragmentManager().popBackStack();
                mScreenColor.setVisibility(View.GONE);
                mSosBtn.setVisibility(View.VISIBLE);
                mColorBtn.setVisibility(View.VISIBLE);
                break;
            case SOS:
                mScreenColor.setVisibility(View.GONE);
                mSosBtn.setVisibility(View.VISIBLE);
                mColorBtn.setVisibility(View.VISIBLE);
                mScreenlightPresenter.requestStop = true;
                mScreenlightPresenter.stopSosModeScreenlight();
                break;
            default:
                if (mScreenColor.getVisibility() == View.VISIBLE) {
                    mScreenColor.setVisibility(View.GONE);
                    mSosBtn.setVisibility(View.VISIBLE);
                    mColorBtn.setVisibility(View.VISIBLE);
                } else {
                    if (!isFromWidget) {
                        overridePendingTransition(R.anim.activity_fadein, R.anim.activity_fadeout);
                    }
                    super.onBackPressed();
                }

                break;
        }

    }

    @Override
    public void updateScreenColor(final int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mScreenColor.setBackgroundColor(color);
                mSosBtn.setVisibility(View.GONE);
                mColorBtn.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void activeLightModeScreen() {
        showTooltips(findViewById(R.id.screenlight_guide),getString(R.string.tips_how_to_exit));
        mScreenColor.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        mScreenColor.setVisibility(View.VISIBLE);
        mSosBtn.setVisibility(View.GONE);
        mColorBtn.setVisibility(View.GONE);
        mModeScreenlight = ModeScreenLight.LIGHT;


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
