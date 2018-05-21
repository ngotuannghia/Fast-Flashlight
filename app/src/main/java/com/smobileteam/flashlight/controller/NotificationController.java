package com.smobileteam.flashlight.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.smobileteam.flashlight.FlashlightActivity;
import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.glass.GlassActivity;
import com.smobileteam.flashlight.powersave.PowerSaveFlashActivity;
import com.smobileteam.flashlight.screenlight.ScreenLightActivity;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.PrefsUtils;
import com.smobileteam.flashlight.view.Tooltip;

/**
 * Created by Duong Anh Son on 3/31/2017.
 * FlashlightBitbucket
 */

public class NotificationController {
    private Context mContext;
    public final static int NOTIFICATION_REMOTE_FLASHLIGHT = 1987;
    public final static String ACTION_TURN_ON_FLASHLIHT = "turn_on_flashlight";
    public final static String ACTION_TURN_ON_SCREENLIGHT = "turn_on_screenlight";
    private final String KEY_CHECK_SHOW_TIPS = "is_the_first_enable_QC_notice";

    public NotificationController(Context context) {
        mContext = context;
    }
    private View view;
    private boolean isShowOverlayTooltips;

    public boolean isShowOverlayTooltips() {
        return isShowOverlayTooltips;
    }

    public void setShowOverlayTooltips(boolean showOverlayTooltips) {
        isShowOverlayTooltips = showOverlayTooltips;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void buildNotification(boolean... isUpsm) {
        //get the notification manager
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent();
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(
                mContext,
                NOTIFICATION_REMOTE_FLASHLIGHT,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                // Set Icon
                .setSmallIcon(R.drawable.ic_light_statusbar)
                // Dismiss Notification
                .setAutoCancel(true)
                // Set PendingIntent into Notification
                .setContentIntent(pIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // build a complex notification, with buttons and such
            //
            if (isUpsm.length > 0) {
                builder = builder.setContent(getComplexNotificationView(isUpsm[0]));
            } else {
                builder = builder.setContent(getComplexNotificationView(false));
            }
        } else {
            // Build a simpler notification, without buttons
            builder = builder.setSmallIcon(R.drawable.ic_light_statusbar);
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.priority = Notification.PRIORITY_MAX;
        mNotificationManager.notify(NOTIFICATION_REMOTE_FLASHLIGHT,notification);
        if(PrefsUtils.getInstance(mContext).getBooleanParams(KEY_CHECK_SHOW_TIPS,true)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if(null != getView()) showTooltips(getView(),mContext.getString(R.string.msg_enable_quick_controller));
            } else {
                Toast.makeText(mContext,
                        mContext.getString(R.string.msg_enable_quick_controller),
                        Toast.LENGTH_SHORT).show();
            }

            PrefsUtils.getInstance(mContext).putBooleanParams(KEY_CHECK_SHOW_TIPS,false);
        }

    }


    private void showTooltips(View v,String content){
        Tooltip.make(
                mContext,
                new Tooltip.Builder(101)
                        .anchor(v, Tooltip.Gravity.BOTTOM)
                        .closePolicy(Tooltip.ClosePolicy.TOUCH_ANYWHERE_NO_CONSUME, 3000)
                        .text(content)
                        .fadeDuration(200)
                        .fitToScreen(true)
                        .maxWidth(CommonUtils.dpToPixels(170))
                        .showDelay(200)
                        .withArrow(true)
                        .withOverlay(isShowOverlayTooltips())
                        .build()
        ).show();
    }
    private RemoteViews getComplexNotificationView(boolean isUpsm) {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews notificationView;
        if(isUpsm){
            notificationView = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.notification_upsm_layout
            );
            Intent turnOnFlashIntent = new Intent(mContext, PowerSaveFlashActivity.class);
            turnOnFlashIntent.setAction(ACTION_TURN_ON_FLASHLIHT);
            PendingIntent pturnOnFlashIntent = PendingIntent.getActivity(mContext, 1,
                    turnOnFlashIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_flashlight, pturnOnFlashIntent);
        } else {
            notificationView = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.notification_layout
            );
            Intent turnOnFlashIntent = new Intent(mContext, FlashlightActivity.class);
            turnOnFlashIntent.setAction(ACTION_TURN_ON_FLASHLIHT);
            PendingIntent pturnOnFlashIntent = PendingIntent.getActivity(mContext, 1,
                    turnOnFlashIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_flashlight, pturnOnFlashIntent);

            Intent screenLightIntent = new Intent(mContext, ScreenLightActivity.class);
            screenLightIntent.setAction(ACTION_TURN_ON_SCREENLIGHT);
            PendingIntent pscreenLightIntent = PendingIntent.getActivity(mContext, 2,
                    screenLightIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_screenlight, pscreenLightIntent);

            Intent glassIntent = new Intent(mContext, GlassActivity.class);
            PendingIntent pglassIntent = PendingIntent.getActivity(mContext, 3,
                    glassIntent, 0);
            notificationView.setOnClickPendingIntent(R.id.notification_glass, pglassIntent);
        }

        return notificationView;
    }
    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new �task complete� notification
     */
    public void hideNotification(int notificationId)    {
        //remove the notification from the status bar
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if(null != getView()) showTooltips(getView(),mContext.getString(R.string.msg_disable_quick_controller));
        } else {
            Toast.makeText(mContext,
                    mContext.getString(R.string.msg_disable_quick_controller),
                    Toast.LENGTH_SHORT).show();
        }
        PrefsUtils.getInstance(mContext).putBooleanParams(KEY_CHECK_SHOW_TIPS,true);
    }
}
