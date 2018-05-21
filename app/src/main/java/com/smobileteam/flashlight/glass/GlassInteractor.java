package com.smobileteam.flashlight.glass;

import android.graphics.Bitmap;

/**
 * Created by Duong Anh Son on 2/4/2017.
 * FlashlightBitbucket
 */

public interface GlassInteractor {
    interface IFlashlightControl {
        void turnOnFlash();

        void turnOffFlash();
    }

    interface IGlassView {
        void updateImageForGalleryView(Bitmap bitmap);
    }
}
