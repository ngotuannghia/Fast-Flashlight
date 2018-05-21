package com.smobileteam.flashlight.dialog;

import android.content.Context;

import com.smobileteam.flashlight.R;

public class ProgressDialog extends AbsBaseDialog<ProgressDialog> {

    public ProgressDialog(Context context) {
        super(context);
    }

    public ProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    {
        setCancelable(false);
    }

    @Override
    protected int getLayout() {
        return R.layout.dialog_progress;
    }
}
