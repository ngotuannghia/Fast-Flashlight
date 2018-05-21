package com.smobileteam.flashlight.screenlight;

import android.graphics.Color;

import com.smobileteam.flashlight.utils.CommonUtils;

/**
 * Created by Duong Anh Son on 13/12/2016.
 *
 */

public class ScreenLightPresenter {

    private ScreenLightInteractor.ScreenLightView viewlisenter;
    private int index = 0;

    public ScreenLightPresenter(ScreenLightInteractor.ScreenLightView view) {
        this.viewlisenter = view;
    }

    public String getScreenLightColor(String[] colors) {

        String color;
        if (index < colors.length) {
            color = colors[index];
            index++;
        } else {
            index = 0;
            color = colors[index];
            index++;
        }
        return color;
    }

    public volatile boolean requestStop = false;
    private Thread mThread;

    public void startSosModeScreenlight() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                requestStop = false;
                while (!requestStop) {
                    try {
                        int delay = CommonUtils.calculateTimeDelayStrobe(7);
                        viewlisenter.updateScreenColor(Color.WHITE);
                        Thread.sleep(delay);
                        viewlisenter.updateScreenColor(Color.BLACK);
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (RuntimeException ex) {
                        requestStop = true;
                        ex.printStackTrace();

                    }
                }
               requestStop = false;
            }
        });
        mThread.start();
    }

    public void stopSosModeScreenlight() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }

    }

    public void colorModeScreenlight() {
        viewlisenter.updateScreenColor(Color.RED);
    }


}
