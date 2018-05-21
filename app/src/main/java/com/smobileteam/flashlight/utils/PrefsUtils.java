package com.smobileteam.flashlight.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight_v3
 */

public class PrefsUtils {

    private SharedPreferences pref;

    public static PrefsUtils getInstance(Context context) {
        return new PrefsUtils(context);
    }


    private PrefsUtils(Context context) {
        if (pref == null) {
            pref = context.getSharedPreferences(FlashlightConstant.PREFS_NAME, Context.MODE_PRIVATE);
        }
    }

    public void putStringParams(String name, String value) {
        pref.edit().putString(name, value).apply();
    }

    public String getStringParams(String name) {
        if (pref == null) {
            return "";
        }
        return pref.getString(name, "");
    }

    public String getStringParams(String name, String defaultvalue) {
        if (pref == null) {
            return defaultvalue;
        }
        return pref.getString(name, defaultvalue);
    }

    public void putIntParams(String name, int value) {
        pref.edit().putInt(name, value).apply();
    }

    public int getIntParams(String name) {
        if (pref == null) {
            return -1;
        }
        return pref.getInt(name, -1);
    }

    public int getIntParams(String name, int defaultvalue) {
        if (pref == null) {
            return defaultvalue;
        }
        return pref.getInt(name, defaultvalue);
    }

    public void putBooleanParams(String name, boolean value) {
        pref.edit().putBoolean(name, value).apply();
    }

    public boolean getBooleanParams(String name) {
        if (pref == null) {
            return false;
        }
        if (FlashlightConstant.FlashSetting.ENABLE_SOUND.equalsIgnoreCase(name)) {
            return pref.getBoolean(name, true);
        }
        return pref.getBoolean(name, false);
    }

    public boolean getBooleanParams(String name, boolean defaultValue) {
        if (pref == null) {
            return false;
        }
        return pref.getBoolean(name, defaultValue);
    }
}
