package com.smobileteam.flashlight.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight_v3
 */

public class OsUtils {
    private static boolean sIsAtLeastM;
    private static boolean sIsAtLeastL;

    static {
        final int v = getApiVersion();
        sIsAtLeastL = v >= 21;//android.os.Build.VERSION_CODES.LOLLIPOP;
        sIsAtLeastM = v >= 23;//android.os.Build.VERSION_CODES.M
    }

    /**
     * @return True if the version of Android that we're running on is at least M
     *  (API level 23).
     */
    public static boolean isAtLeastM() {
        return sIsAtLeastM;
    }
    /**
     * @return True if the version of Android that we're running on is at least L
     *  (API level 21).
     */
    public static boolean isAtLeastL() {
        return sIsAtLeastL;
    }
    /**
     * @return The Android API version of the OS that we're currently running on.
     */
    private static int getApiVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String[] sCameraPermissions = new String[] {
            // Required to record audio
            Manifest.permission.CAMERA,

    };

    public static String[] sStoragePermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    public static String[] sWriteSettingPermissions = new String[]{
            Manifest.permission.WRITE_SETTINGS,
    };

    private static boolean hasPermission(Context context, final String permission) {
        if (isAtLeastM()) {
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            return permissionCheck == PackageManager.PERMISSION_GRANTED;
        }else {
            return true;
        }
    }
    /** Does the app have all the specified permissions */
    private static boolean hasPermissions(Context context,final String[] permissions) {
        for (final String permission : permissions) {
            if (!hasPermission(context,permission)) {
                return false;
            }
        }
        return true;
    }

    /** Does the app have the minimum set of permissions required to operate. */
    public static boolean hasCameraPermissions(Context context) {
        return hasPermissions(context,sCameraPermissions);
    }

    public static boolean hasStoragePermissions(Context context){
        return hasPermissions(context, sStoragePermissions);
    }

    public static boolean hasWriteSettingPermissions(Context context){
        return hasPermissions(context, sWriteSettingPermissions);
    }

}
