package com.smobileteam.flashlight.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.smobileteam.flashlight.controller.NotificationController;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.Logger;
import com.smobileteam.flashlight.utils.PrefsUtils;

/**
 * Created by Duong Anh Son on 4/3/2017.
 * FlashlightBitbucket
 */

public class QuickControllerBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.e("QuickControllerBootReceiver");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Enable Quick controller afte boot completed if this mode was enabled
            boolean enable_quick_controller = PrefsUtils.getInstance(context)
                    .getBooleanParams(FlashlightConstant.FlashlightFeatures.ENABLE_QUICK_CONTROLLER, true);
            if (enable_quick_controller) {
                NotificationController quickController = new NotificationController(context);
                quickController.buildNotification();
            }
        }
    }
}
