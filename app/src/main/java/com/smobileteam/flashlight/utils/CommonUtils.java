package com.smobileteam.flashlight.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight_v3
 */

public class CommonUtils {

    public static boolean isFlashSupported(Context context) {
//      if(isEmulator()) return true;
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

   /*public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }*/

    private static Toast mToast = null;

    /**
     * Show Toast
     */
    public static void showToast(Context context, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    public static Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get version of application from Manifest file
     *
     * @return
     */
    public static String getVersion(Context context) {
        String versionName = "1.0";
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
        }
        return versionName;
    }

    public static void setBrightness(Activity activity, int mode, int value) {

        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                mode);

        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, value);

        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = 0.2f;// 100 / 100.0f;
        activity.getWindow().setAttributes(lp);
    }

    public static void vibrate(Context context) {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 400 milliseconds
        v.vibrate(20);
    }

    public static int calculateTimeDelayStrobe(int numberStrobePerSecond) {
        if (numberStrobePerSecond != 0) {
            return Math.round(1000 / numberStrobePerSecond);
        } else {
            return 0;
        }

    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
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

    /**
     * Converts dps to pixels.
     * @param dp Value in dp.
     * @return Value in pixels.
     */
    public static int dpToPixels(int dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp + 0.5f);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px) {
        return (px / (Resources.getSystem().getDisplayMetrics().densityDpi / 160f));

    }
    /**
     * Convert a millisecond duration to a string format
     *
     * @param milliseconds A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long milliseconds){
        final int SECOND = 1000;
        final int MINUTE = 60 * SECOND;
        final int HOUR = 60 * MINUTE;
        final int DAY = 24 * HOUR;
        StringBuffer text = new StringBuffer("");
        if (milliseconds > DAY) {
            text.append(milliseconds / DAY).append("d ");
            milliseconds %= DAY;
        }
        if (milliseconds > HOUR) {
            text.append(milliseconds / HOUR).append("h ");
            milliseconds %= HOUR;
        }
        if (milliseconds > MINUTE) {
            text.append(milliseconds / MINUTE).append("m ");
            milliseconds %= MINUTE;
        }
        if (milliseconds > SECOND) {
            text.append(milliseconds / SECOND).append("s ");
//            milliseconds %= SECOND;
        }
        return text.toString();
    }
}
