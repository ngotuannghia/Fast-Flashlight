package com.smobileteam.flashlight;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.utils.FlashlightConstant;

import java.io.File;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight v3
 */

public class FlashlightApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SoundClickController.initSoundManager(this);

        File filePreference = new File("/data/data/"
                + this.getPackageName() + "/shared_prefs/" + FlashlightConstant.PREFS_NAME);
        if ((filePreference != null && !filePreference.exists())) {
            PreferenceManager.setDefaultValues(this,
                    FlashlightConstant.PREFS_NAME, Context.MODE_PRIVATE, R.xml.settings, false);
        }
    }

    // This is a singleton to keep track of whether the flashlight is on or off.

    static Boolean _isFlashOn = false;

    public static Boolean isFlashOn() {
        return _isFlashOn;
    }

    public static void setIsFlashOn(Boolean isFlashOn) {
        _isFlashOn = isFlashOn;
    }
}
