package com.smobileteam.flashlight.screenlight;

import android.os.Handler;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Duong Anh Son on 4/11/2017.
 * FlashlightBitbucket
 */

public abstract class DoubleClickListener implements View.OnClickListener {
    private Timer timer = null;  //at class level;
    private int DELAY = 400;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            processDoubleClickEvent(v);
        } else {
            processSingleClickEvent(v);
        }
        lastClickTime = clickTime;
    }


    public void processSingleClickEvent(final View v) {

        final Handler handler = new Handler();
        final Runnable mRunnable = new Runnable() {
            public void run() {
                onSingleClick(v); //Do what ever u want on single click

            }
        };

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(mRunnable);
            }
        };
        timer = new Timer();
        timer.schedule(timertask, DELAY);

    }


    public void processDoubleClickEvent(View v) {
        if (timer != null) {
            timer.cancel(); //Cancels Running Tasks or Waiting Tasks.
            timer.purge();  //Frees Memory by erasing cancelled Tasks.
        }
        onDoubleClick(v);//Do what ever u want on Double Click
    }

    public abstract void onSingleClick(View v);

    public abstract void onDoubleClick(View v);
}
