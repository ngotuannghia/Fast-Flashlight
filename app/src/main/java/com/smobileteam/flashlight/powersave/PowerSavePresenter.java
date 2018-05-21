package com.smobileteam.flashlight.powersave;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.smobileteam.flashlight.utils.Logger;

/**
 * Created by Duong Anh Son on 5/6/2017.
 * FlashlightBitbucket
 */

public class PowerSavePresenter {
    private PowerSaveInteractor mpPowerSaveInteractor;
    private long timeForTotalBattery = 216000000;// Gia su pin day 100% thi dung duoc 2 ngay

    PowerSavePresenter(PowerSaveInteractor mpPowerSaveInteractor) {
        this.mpPowerSaveInteractor = mpPowerSaveInteractor;
    }

    void caculateTimeRemaining(int battery1, int battery2, long time1, long time2 ){
        long timeEstimate = 0;
        if(battery2 == 0){
            Logger.d("battery2 == 0");
            // For the first start app
            timeEstimate = (timeForTotalBattery * battery1)/100;
            mpPowerSaveInteractor.updateTimeRemaining(timeEstimate);
        } else {
            int speedBattery = battery1 - battery2;
            long speedTime = Math.abs(time1 - time2);
            if(speedBattery < 0){
                // case drain battery
                timeEstimate = (battery1 * speedTime)/Math.abs(speedBattery);
                mpPowerSaveInteractor.updateTimeRemaining(timeEstimate);
            } else if(speedBattery == 0) {
                // do nothing
                Logger.d("speedBattery = "+speedBattery +"speedTime= "+speedTime);
            } else {
                // case charging
                mpPowerSaveInteractor.updateTimeRemaining(timeForTotalBattery);
            }
        }
    }
    /**
     * Tra ve dung luong cua pin : mAh. Neu co loi xay ra thi tra ve dung luong bang 0
     * @return : mAh
     */
    private double getBatteryCapacity() {
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            Object mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
            return (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
