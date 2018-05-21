package com.smobileteam.flashlight.setting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.dialog.LicensesDialog;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.Logger;
import com.smobileteam.flashlight.utils.PrefsUtils;
import com.smobileteam.flashlight.view.SeekBarPreference;

/**
 * Created by Duong Anh Son on 5/1/2017.
 * FlashlightBitbucket
 */

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
        ,SeekbarUpdateInterface{
    private final static String APP_PNAME = "com.smobileteam.flashlight";// Package Name

    private Context mContext;
    private Preference mAppVersion;
    private Preference mFeedback;
    private Preference mSmobileOnWeb;
    private Preference mSayThanks;
    private Preference mOpensourceLicense;
    private SeekBarPreference mBatteryLevelSb;
    private CheckBoxPreference mPowerSavingModeCb;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        getPreferenceManager()
                .setSharedPreferencesName(FlashlightConstant.PREFS_NAME);
        addPreferencesFromResource(R.xml.settings);
        initUI();
    }
    private void initUI(){
        mAppVersion = findPreference(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_VERSION);
        mFeedback = findPreference(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_FEEDBACK);
        mSmobileOnWeb = findPreference(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_ON_GOOGLEPLUS);
        mSayThanks = findPreference(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_RATE);
        mOpensourceLicense = findPreference(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_LICENSE);
        mBatteryLevelSb = (SeekBarPreference)findPreference(FlashlightConstant.FlashSetting.KEY_BATTERY_LEVEL);
        mPowerSavingModeCb = (CheckBoxPreference) findPreference(FlashlightConstant.FlashlightFeatures.ULTRA_POWER_SAVING_MODE);

        mAppVersion.setSummary(CommonUtils.getVersion(mContext));
        mFeedback.setOnPreferenceClickListener(this);
        mSmobileOnWeb.setOnPreferenceClickListener(this);
        mSayThanks.setOnPreferenceClickListener(this);
        mOpensourceLicense.setOnPreferenceClickListener(this);
        mBatteryLevelSb.setSeekbarUpdateInterface(this);
        mPowerSavingModeCb.setSummary(getString(R.string.st_about_app_power_saving_mode_description,
                PrefsUtils.getInstance(mContext).getIntParams(FlashlightConstant.FlashSetting.KEY_BATTERY_LEVEL)));

    }

    public void showOpensourceLicense() {
        new LicensesDialog(mContext).setTopColorRes(R.color.darkBlueGrey)
                .setIcon(R.drawable.ic_info_outline_white_36dp)
                .show();
    }
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (FlashlightConstant.FlashSetting.KEY_ABOUT_APP_ON_GOOGLEPLUS.equals(key)) {
            String url = "https://plus.google.com/b/115825835888801940774/115825835888801940774/about";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        } else if (FlashlightConstant.FlashSetting.KEY_ABOUT_APP_FEEDBACK.equals(key)) {
            Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "anhson.duong@gmail.com", null));
            feedbackIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.app_name) + " ("
                            + CommonUtils.getVersion(mContext) + "|"
                            + getDeviceName() + "): "
                            + getString(R.string.about_app_feedback_title));
            try {
                this.startActivity(Intent.createChooser(feedbackIntent, getString(R.string.feedback) + "..."));
            } catch (android.content.ActivityNotFoundException ex) {
                CommonUtils.showToast(mContext, getString(R.string.app_feedback_exception_no_app_handle));
            }

            return true;
        } else if(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_RATE.equals(key)){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
        } else if(FlashlightConstant.FlashSetting.KEY_ABOUT_APP_LICENSE.equals(key)){
            showOpensourceLicense();
        }
        return false;
    }

    @Override
    public void updateProcessChange(int value) {
        Logger.e("updateProcessChange, value = "+value);
        mPowerSavingModeCb.setSummary(getString(R.string.st_about_app_power_saving_mode_description,value));
    }
}
