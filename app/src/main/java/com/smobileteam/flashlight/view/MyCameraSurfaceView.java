package com.smobileteam.flashlight.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.smobileteam.flashlight.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Duong Anh Son on 04/12/2016.
 * FlashlightBitbucket
 */

public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    private Camera mCamera;

    private AutofocusCrosshair drawingView;

    private boolean isAutoFocusing;
    private boolean isSurfaceCreated;

    private boolean isPreview;

    private Activity mActivity;
    private int mCurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public void setCurrentCameraId(int currentCameraId) {
        this.mCurrentCameraId = currentCameraId;
    }

    public boolean isPreview() {
        return isPreview;
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    private ScheduledExecutorService myScheduledExecutorService;
    private int mRotation = 0;

    public MyCameraSurfaceView(Activity activity, Context context, Camera camera) {
        super(context);
        mActivity = activity;
        mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mRotation = getCameraDisplayOrientation();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
        // If your preview can change or rotate, take care of those events
        // here.
        // Make sure to stop the preview before resizing or reformatting it.
        // mCamera.setDisplayOrientation(90);
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            isPreview = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Camera.Parameters param;
        param = mCamera.getParameters();
        try {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(mRotation);
            Camera.Size bestSize = findBestCameraSize();
            List<Integer> supportedPreviewFormats = param.getSupportedPreviewFormats();
            Iterator<Integer> supportedPreviewFormatsIterator = supportedPreviewFormats.iterator();
            while(supportedPreviewFormatsIterator.hasNext()){
                Integer previewFormat =supportedPreviewFormatsIterator.next();
                if (previewFormat == ImageFormat.YV12) {
                    param.setPreviewFormat(previewFormat);
                }
            }
            param.setPreviewSize(bestSize.width, bestSize.height);
            param.setPictureSize(bestSize.width, bestSize.height);
            mCamera.setParameters(param);

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            isPreview = true;
            isSurfaceCreated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        isSurfaceCreated = false;

    }

    public int getCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(mCurrentCameraId, info);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // do something for phones running an SDK before lollipop
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.Size findBestCameraSize(){
        Camera.Parameters param = mCamera.getParameters();

        Camera.Size bestSize;
        List<Camera.Size> sizeList = param.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) > (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    private final int FOCUS_AREA_SIZE = 300;
    private Rect calculateFocusArea(float x, float y, int width, int height) {
        int left = clamp(Float.valueOf((x / width) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / height) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper) + focusAreaSize / 2 > 1000) {
            if (touchCoordinateInCameraReper > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = touchCoordinateInCameraReper - focusAreaSize / 2;
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void focusOnTouch(Rect rect, final Camera.AutoFocusCallback callback) {

        if (mCamera != null && !isAutoFocusing && isSurfaceCreated) {
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0) {
                isAutoFocusing = true;
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);
                try {
                    mCamera.setParameters(parameters);
                } catch (RuntimeException e) {
                    Logger.e("failed to set parameters");
                }
                //Delay call autoFocus(myAutoFocusCallback)
                myScheduledExecutorService = Executors.newScheduledThreadPool(1);
                myScheduledExecutorService.schedule(new Runnable() {
                    public void run() {
                        mCamera.autoFocus(callback);
                    }
                }, 500, TimeUnit.MILLISECONDS);
                drawingView.invalidate();
            }

        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getX();
            float y = event.getY();
            Rect touchRect = new Rect(
                    (int)(x-70),
                    (int)(y-70),
                    (int)(x + 70),
                    (int)(y + 70));

            Rect targetFocusRect = calculateFocusArea(x,y,getWidth(),getHeight());

            drawingView.setHaveTouch(true, touchRect);
            focusOnTouch(targetFocusRect,mAutoFocusTakePictureCallback);
            // Remove the square after some time

        }

        return false;
    }
    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Logger.i("tap_to_focus: success!");
                isAutoFocusing = false;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                        drawingView.invalidate();
                    }
                }, 0);
            } else {
                // do something...
                drawingView.setHaveTouch(false, new Rect(0, 0, 0, 0));
                drawingView.invalidate();
                Logger.i("tap_to_focus: fail!");
            }
        }

    };

    /**
     * set DrawingView instance for touch focus indication.
     * @param dView - AutofocusCrosshair
     */
    public void setDrawingView(AutofocusCrosshair dView) {
        drawingView = dView;
    }
}
