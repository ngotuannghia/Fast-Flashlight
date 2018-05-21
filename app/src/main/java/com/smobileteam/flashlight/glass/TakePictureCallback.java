package com.smobileteam.flashlight.glass;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.AppCompatImageView;

import com.smobileteam.flashlight.R;
import com.smobileteam.flashlight.dialog.ProgressDialog;
import com.smobileteam.flashlight.utils.CommonUtils;
import com.smobileteam.flashlight.utils.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Duong Anh Son on 2/4/2017.
 * FlashlightBitbucket
 */

public final class TakePictureCallback implements Camera.PictureCallback {

    private GlassActivity mGlassActivity;
    private Camera myCamera;
    private GlassInteractor.IFlashlightControl mFlashControl;
    private GlassInteractor.IGlassView mGlassView;
    private AppCompatImageView mBtnFlash;
    private GlassPresenter mGlassPresenter;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int mRotation = 0;

    public void setRotation(int mRotation) {
        this.mRotation = mRotation;
    }

    public TakePictureCallback() {

    }

    public void setMyCamera(Camera myCamera) {
        this.myCamera = myCamera;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }

    public TakePictureCallback(GlassActivity glassActivity,
                               Camera camera,
                               GlassInteractor.IFlashlightControl flashControl) {
        this.mGlassActivity = glassActivity;
        this.myCamera = camera;
        this.mFlashControl = flashControl;
        mGlassPresenter = new GlassPresenter();
    }

    public void setGlassViewInterface(GlassInteractor.IGlassView glassView){
        this.mGlassView = glassView;
    }

    public void setView(AppCompatImageView btnFlash){
        this.mBtnFlash = btnFlash;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        new SaveImageTask().execute(data);
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                mGlassActivity.getString(R.string.folder));
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Logger.d("failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private class SaveImageTask extends AsyncTask<byte[], Void,Bitmap>{
        ProgressDialog pd = null;
        File pictureFile;

        @Override
        protected Bitmap doInBackground(byte[]... data) {

            pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap realImage = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
                if(mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    if(mRotation == 90){
                        realImage= mGlassPresenter.rotate(realImage, 90);
                    } else if(mRotation == 270){
                        realImage= mGlassPresenter.rotate(realImage, 270);
                    }

                } else {
                    realImage= mGlassPresenter.rotate(realImage, 270);
                }
                realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                return realImage;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(mGlassActivity);
            pd.setTopColorRes(R.color.darkBlueGrey);
            pd.setIcon(R.drawable.ic_progress);
            pd.setTitle(mGlassActivity.getString(R.string.msg_loading));
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (this.pd != null) {
                pd.dismiss();
            }
            try {
                myCamera.startPreview();
            } catch (Exception e){
                e.printStackTrace();
            }
            mFlashControl.turnOffFlash();
            mBtnFlash.setSelected(false);
            mGlassActivity.setFlashButtonSelected(false);
            if(bitmap != null){
                mGlassPresenter.addImageToGallery(pictureFile.getPath(),mGlassActivity);
                mGlassView.updateImageForGalleryView(CommonUtils.scaleDownBitmapImage(bitmap,200,200));
            }
        }
    }
}
