package com.smobileteam.flashlight.utils;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight_v3
 */

public class FlashlightConstant {
    public static final boolean DEBUG = true;
    public final static String PREFS_NAME = "led_flashlight";

    public class FlashlightFeatures {
        public static final String ENABLE_SOS = "CheckSOS";
        public static final String ENABLE_QUICK_CONTROLLER = "key_enable_quick_controller";
        public static final String ULTRA_POWER_SAVING_MODE = "key_power_saving_mode";
    }

    public class FlashSetting {
        public static final String ENABLE_SOUND = "key_enable_touch_sound";
        public static final String ENABLE_AUTOMATIC_TURN_ON_LED = "key_automatic_turn_on_led";
        public static final String KEY_ABOUT_APP_VERSION           = "key_about_app_version";
        public static final String KEY_ABOUT_APP_FEEDBACK          = "key_about_app_feedback";
        public static final String KEY_ABOUT_APP_ON_GOOGLEPLUS     = "key_about_app_smobile_on_googleplus";
        public static final String KEY_ABOUT_APP_RATE = "key_about_app_rate_app";
        public static final String KEY_ABOUT_APP_LICENSE = "key_about_app_license";
        public static final String KEY_VIBRATION = "key_vabration";
        public static final String KEY_ASK_ON_QUIT = "key_ask_on_quit";
        public static final String KEY_KEEP_SCREEN_ON = "key_keep_screen_on";
        public static final String KEY_BATTERY_LEVEL = "key_power_save_mode_seekbar";
        public static final String KEY_ALREADY_ENABLED_UPSM = "key_already_enabled_upsm";

    }

    public class PermissionRequestCode {
        public static final int RQ_FLASHACTIVITY_CAMERA_PERMISSION = 101;
        public static final int RQ_GLASSACTIVITY_CAMERA_PERMISSION = 102;
        public static final int RQ_GLASSACTIVITY_STORATE_PERMISSION = 103;
        public static final int RQ_PERMISSION_SETTING_APP = 104;
    }

    public class DELAY{
        public static final int DELAY_200 = 200;
        public static final int DELAY_300 = 300;
        public static final int DELAY_400 = 400;

    }
}
