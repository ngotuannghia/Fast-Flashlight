package com.smobileteam.flashlight.screenlight;

/**
 * Created by Duong Anh Son on 13/12/2016.
 *
 */

public interface ScreenLightInteractor {
    interface ScreenLightView{
        void updateScreenColor(int color);
        void activeLightModeScreen();
    }
}
