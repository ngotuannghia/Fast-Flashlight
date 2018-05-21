package com.smobileteam.flashlight.controller;

import android.hardware.Camera;
import android.os.Handler;

import com.smobileteam.flashlight.FlashlightApplication;
import com.smobileteam.flashlight.utils.Logger;

import java.util.List;

/**
 * Created by Duong Anh Son on 10/2/2016.
 * Flashlight version3
 */

public class CameraController {
    private static Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isSupportFlashModeTourch;
    private String mManufactuerName = android.os.Build.MANUFACTURER.toLowerCase();
    private boolean isMotorolaModel;

    private static CameraController mCameraController = null;

    private Handler mHandler = new Handler();

    private boolean isAlreadyStartPreview;

    private CameraController() {}

    public static CameraController getCameraControllerInstance(){
        if(mCameraController == null){
            mCameraController = new CameraController();
        }
        getCameraInstance();



        return mCameraController;
    }

    private static Camera getCameraInstance(){
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            }catch (Exception e) {
                //Error
                e.printStackTrace();
            }

        }
        return mCamera;
    }
    public void setCameraParams(){
        try {
            mParams = mCamera.getParameters();
            List<String> flashModes = mParams.getSupportedFlashModes();
            if (flashModes == null) {
                return;
            } else {
                String flashMode = mParams.getFlashMode();
                if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                    isSupportFlashModeTourch = flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
                }
            }
        }catch (RuntimeException e) {
            //Error
        }

        if(mManufactuerName.contains("motorola")){
            isMotorolaModel = true;
        }
    }
    public void resetCamera(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    // first stop preview
                    mCamera.stopPreview();
                    // then cancel its preview callback
                    mCamera.setPreviewCallback(null);
                    // and finally release it
                    mCamera.release();
                    // sanitize you Camera object holder
                     mCamera = null;
                    FlashlightApplication.setIsFlashOn(false);
                    setAlreadyStartPreview(false);
                }
            }
        });

    }

    public synchronized boolean turnOnFlash(IStrobeLight indicator) {
        boolean isTurnOnSuccess = false;
        if (isMotorolaModel) {
            isTurnOnSuccess = turnMotorolaOn(indicator);
            Logger.d("turnMotorolaOn");
            return isTurnOnSuccess;
        }
        if (!FlashlightApplication.isFlashOn()) {
                if (mCamera == null || mParams == null) {
                    Logger.e("mCamera == null");
                    return false;
                }
                try {
                    FlashlightApplication.setIsFlashOn(true);
                    indicator.setIndicatorOn();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (isSupportFlashModeTourch) {
                                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                mCamera.setParameters(mParams);
                            } else {
                                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                                mCamera.setParameters(mParams);
                            }
                            mCamera.startPreview();
                        }
                    }).start();

                    Logger.d("turnOnFlash");
                    isTurnOnSuccess = true;
                }  catch (RuntimeException ex){
                    Logger.d("CameraController, turnOnlight- RuntimeException");
                    ex.printStackTrace();
                } catch (Exception e) {
                    Logger.d("CameraController, turnOnlight- getParameters failed (empty parameters)");
                    isTurnOnSuccess = false;
                    indicator.turnOnFlashFailed();
                }



        }
        return isTurnOnSuccess;
    }
    public boolean turnOnStrobeFlash(IStrobeLight indicator) {
        boolean isTurnOnSuccess = false;
        if (isMotorolaModel) {
            isTurnOnSuccess = turnMotorolaOn(indicator);
            Logger.d("turnMotorolaOn");
            return isTurnOnSuccess;
        }
        if (!FlashlightApplication.isFlashOn()) {
            if (mCamera == null || mParams == null) {
                Logger.e("mCamera != null");
                return false;
            }
            try {
                FlashlightApplication.setIsFlashOn(true);
                indicator.setIndicatorOn();
                if (isSupportFlashModeTourch) {
                    mParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParams);
                } else {
                    mParams.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(mParams);
                }
                if(!isAlreadyStartPreview()){
                    mCamera.startPreview();
                    setAlreadyStartPreview(true);
                    Logger.d("isAlreadyStartPreview = false");
                }
                Logger.d("turnOnFlash");
                isTurnOnSuccess = true;
            } catch (Exception e) {
                Logger.d("CameraController, turnOnlight- getParameters failed (empty parameters)");
                isTurnOnSuccess = false;
                indicator.turnOnFlashFailed();
            }

        }
        return isTurnOnSuccess;
    }
    private boolean turnMotorolaOn(IStrobeLight indicator) {
        boolean isTurnOnSuccess;
        MotorolaDroidLED led;
        try {
            led = new MotorolaDroidLED();
            led.enable(true);
            FlashlightApplication.setIsFlashOn(true);
            indicator.setIndicatorOn();
            isTurnOnSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isTurnOnSuccess = false;
            indicator.turnOnFlashFailed();
        }
        return isTurnOnSuccess;
    }
    private boolean turnMotorolaOff() {
        boolean isTurnOffSuccess;
        MotorolaDroidLED led;
        try {
            led = new MotorolaDroidLED();
            led.enable(false);
            FlashlightApplication.setIsFlashOn(false);
            isTurnOffSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            isTurnOffSuccess = false;
        }
        return isTurnOffSuccess;
    }


    public synchronized boolean turnOffFlash(IStrobeLight indicator) {
        boolean isTurnOffSuccess = false;
        if (isMotorolaModel) {
            if(FlashlightApplication.isFlashOn()){
                isTurnOffSuccess = turnMotorolaOff();
                indicator.setIndicatorOff();
            }
            Logger.d("turnMotorolaOff");
            return isTurnOffSuccess;
        }
        if (FlashlightApplication.isFlashOn()) {
            try {
                FlashlightApplication.setIsFlashOn(false);
                indicator.setIndicatorOff();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCamera != null) {
                            mParams = mCamera.getParameters();
                            mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            mCamera.setParameters(mParams);
                            mCamera.stopPreview();
                        }
                    }
                }).start();
                isTurnOffSuccess = true;
                Logger.d("turnOffFlash");
            } catch (Exception e) {
                Logger.d("CameraController, turnofflight- getParameters failed (empty parameters)");
                isTurnOffSuccess = false;
            }
        }
        setAlreadyStartPreview(false);
        return isTurnOffSuccess;
    }
    public boolean turnOffStrobeFlash(IStrobeLight indicator) {
        boolean isTurnOffSuccess = false;
        if (isMotorolaModel) {
            if(FlashlightApplication.isFlashOn()){
                isTurnOffSuccess = turnMotorolaOff();
                indicator.setIndicatorOff();
            }
            Logger.d("turnMotorolaOff");
            return isTurnOffSuccess;
        }
        if (FlashlightApplication.isFlashOn()) {
            if (mCamera == null || mParams == null) {
                return false;
            }
            try {
                FlashlightApplication.setIsFlashOn(false);
                indicator.setIndicatorOff();
                mParams = mCamera.getParameters();
                mParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParams);
                if(!isAlreadyStartPreview()){
                    mCamera.stopPreview();
                    setAlreadyStartPreview(false);
                }
                isTurnOffSuccess = true;
                Logger.d("turnOffFlash");
            }catch (Exception e){
                Logger.d("CameraController, turnofflight- getParameters failed (empty parameters)");
                isTurnOffSuccess = false;
            }


        }
        return isTurnOffSuccess;
    }

    public boolean isSupportFlashModeTourch() {
        return isSupportFlashModeTourch;
    }

    public void setIsSupportFlashModeTourch(boolean isSupportFlashModeTourch) {
        this.isSupportFlashModeTourch = isSupportFlashModeTourch;
    }

    public boolean isMotorolaModel() {
        return isMotorolaModel;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    public Camera.Parameters getParams() {
        return mParams;
    }

    public void setParams(Camera.Parameters mParams) {
        this.mParams = mParams;
    }

    public boolean isAlreadyStartPreview() {
        return isAlreadyStartPreview;
    }

    public void setAlreadyStartPreview(boolean alreadyStartPreview) {
        isAlreadyStartPreview = alreadyStartPreview;
    }
}
