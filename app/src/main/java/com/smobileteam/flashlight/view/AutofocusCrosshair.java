package com.smobileteam.flashlight.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.smobileteam.flashlight.R;

/**
 * Created by ngotuannghia on 04/12/2016.
 */

public class AutofocusCrosshair extends View {

    private Context mContext;

    private boolean haveTouch = false;
    private Rect touchArea;
    private Paint paint;


    public AutofocusCrosshair(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context,R.color.material_deep_teal_50));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        haveTouch = false;
    }

    private void setDrawable(int resid) {
        this.setBackgroundResource(resid);
    }

    public void setHaveTouch(boolean val, Rect rect) {
        haveTouch = val;
        touchArea = rect;
    }

  /*  @Override
    public void onDraw(Canvas canvas) {
        if(haveTouch){
            //drawingPaint.setColor(Color.BLUE);
            canvas.drawRect(
                    touchArea.left, touchArea.top, touchArea.right, touchArea.bottom,
                    paint);
        }
    }*/

}
