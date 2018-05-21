package com.smobileteam.flashlight.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.smobileteam.flashlight.FlashlightActivity;
import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.controller.SoundClickController;
import com.smobileteam.flashlight.utils.FlashlightConstant;
import com.smobileteam.flashlight.utils.PrefsUtils;

/**
 * Created by Duong Anh Son on 1/5/2017.
 * FlashlightBitbucket
 */

public class WidgetLite extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_lite_layout);
        // Flashlight
        Intent flashIntent = new Intent(context, FlashlightActivity.class);
        flashIntent.putExtra("widget_turn_on", true);
        PendingIntent flashPendingIntent = PendingIntent.getActivity(context, 11002, flashIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_lite_btn_power, flashPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
}
