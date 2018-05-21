package com.smobileteam.flashlight.controller;

import com.smobileteam.flashlight.FlashlightApplication;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.Logger;

/**
 * Created by Duong Anh Son on 10/7/2016.
 * Flashlight_v3
 */

public class StrobeLightController implements Runnable {
    private CameraController mCameraController;

    private static StrobeLightController instance;
    public volatile boolean requestStop = false;
    public volatile int numberStrobePerSecond = 0;

    public volatile boolean isRequestTurnOffLight = false;

    private volatile IStrobeLight mIndicator;


    private StrobeLightController(CameraController camera) {
        if(camera != null){
            this.mCameraController = camera;
        } else {
            this.mCameraController = CameraController.getCameraControllerInstance();
        }

    }

    public static StrobeLightController getInstance(CameraController camera) {
        return (instance == null ? instance = new StrobeLightController(camera) : instance);
    }

    public void setIndicatorView(IStrobeLight indicator){
        this.mIndicator = indicator;
    }


    @Override
    public void run() {
        requestStop = false;
        isRequestTurnOffLight = false;
        while (!requestStop) {
            try {
                int delay = CommonUtils.calculateTimeDelayStrobe(numberStrobePerSecond);
                mCameraController.turnOnStrobeFlash(mIndicator);
                Logger.d("run() turnOnFlash");
                Thread.sleep(delay);
                mCameraController.turnOffStrobeFlash(mIndicator);
                Logger.d("run() turnOffFlash");
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (RuntimeException ex) {
                requestStop = true;
                ex.printStackTrace();

            }
        }
        if(isRequestTurnOffLight && FlashlightApplication.isFlashOn()){
            mCameraController.turnOffFlash(mIndicator);
            Logger.d("run() isRequestTurnOffLight = true");
            isRequestTurnOffLight = false;
        }
        requestStop = false;
        isRequestTurnOffLight = false;
    }

}
