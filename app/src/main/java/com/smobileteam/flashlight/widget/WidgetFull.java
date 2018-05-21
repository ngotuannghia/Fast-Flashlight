package com.smobileteam.flashlight.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

import com.smobileteam.flashlight.FlashlightActivity;
import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.setting.SettingsActivity;
import com.smobileteam.flashlight.glass.GlassActivity;
import com.smobileteam.flashlight.screenlight.ScreenLightActivity;

/**
 * Created by Duong Anh Son on 12/30/2016.
 * FlashlightBitbucket
 */

public class WidgetFull extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_full_layout);
        // Flashlight
        Intent flashIntent = new Intent(context, FlashlightActivity.class);
        flashIntent.putExtra("widget_turn_on", true);
        PendingIntent flashPendingIntent = PendingIntent.getActivity(context, 11001, flashIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ll_power, flashPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        // Setting
        Intent settingIntent = new Intent(context, SettingsActivity.class);
        PendingIntent settingPendingIntent = PendingIntent.getActivity(context, 11002, settingIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ll_setting, settingPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        // glass
        Intent glassIntent = new Intent(context, GlassActivity.class);
        PendingIntent glassPendingIntent = PendingIntent.getActivity(context, 11003, glassIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ll_glass, glassPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        // screenlight
        Intent screenlightIntent = new Intent(context, ScreenLightActivity.class);
        screenlightIntent.putExtra("widget_full",true);
        PendingIntent screenlightPendingIntent = PendingIntent.getActivity(context, 11004, screenlightIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ll_screenlight, screenlightPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        // Wifi setting
        Intent wifiSettingIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        PendingIntent wifiSettingPendingIntent = PendingIntent.getActivity(context, 11005, wifiSettingIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_ll_wifi_setting, wifiSettingPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

}
