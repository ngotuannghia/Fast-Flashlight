package com.smobileteam.flashlight.dialog;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Button;

import com.smobileteam.flashlight.R;

import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

@SuppressWarnings("WeakerAccess")
public class StandardDialog extends AbsBaseDialog<StandardDialog> {

    public static final int POSITIVE_BUTTON = R.id.ld_btn_yes;
    public static final int NEGATIVE_BUTTON = R.id.ld_btn_no;
    public static final int NEUTRAL_BUTTON = R.id.ld_btn_neutral;

    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;

    public StandardDialog(Context context) {
        super(context);
    }

    public StandardDialog(Context context, int theme) {
        super(context, theme);
    }

    {
        positiveButton = findView(R.id.ld_btn_yes);
        negativeButton = findView(R.id.ld_btn_no);
        neutralButton = findView(R.id.ld_btn_neutral);
    }

    public StandardDialog setPositiveButton(@StringRes int text, OnClickListener listener) {
        return setPositiveButton(string(text), listener);
    }

    public StandardDialog setPositiveButton(String text, @Nullable OnClickListener listener) {
        positiveButton.setVisibility(VISIBLE);
        positiveButton.setText(text);
        positiveButton.setOnClickListener(new ClickListenerDecorator(listener, true));
        return this;
    }

    public StandardDialog setNegativeButtonText(@StringRes int text) {
        return setNegativeButton(string(text), null);
    }

    public StandardDialog setNegativeButtonText(String text) {
        return setNegativeButton(text, null);
    }

    public StandardDialog setNegativeButton(@StringRes int text, OnClickListener listener) {
        return setNegativeButton(string(text), listener);
    }

    public StandardDialog setNegativeButton(String text, @Nullable OnClickListener listener) {
        negativeButton.setVisibility(VISIBLE);
        negativeButton.setText(text);
        negativeButton.setOnClickListener(new ClickListenerDecorator(listener, true));
        return this;
    }

    public StandardDialog setNeutralButtonText(@StringRes int text) {
        return setNeutralButton(string(text), null);
    }

    public StandardDialog setNeutralButtonText(String text) {
        return setNeutralButton(text, null);
    }

    public StandardDialog setNeutralButton(@StringRes int text, @Nullable OnClickListener listener) {
        return setNeutralButton(string(text), listener);
    }

    public StandardDialog setNeutralButton(String text, @Nullable OnClickListener listener) {
        neutralButton.setVisibility(VISIBLE);
        neutralButton.setText(text);
        neutralButton.setOnClickListener(new ClickListenerDecorator(listener, true));
        return this;
    }

    public StandardDialog setButtonsColor(@ColorInt int color) {
        positiveButton.setTextColor(color);
        negativeButton.setTextColor(color);
        neutralButton.setTextColor(color);
        return this;
    }

    public StandardDialog setButtonsColorRes(@ColorRes int colorRes) {
        return setButtonsColor(color(colorRes));
    }

    public StandardDialog setOnButtonClickListener(OnClickListener listener) {
        return setOnButtonClickListener(true, listener);
    }

    public StandardDialog setOnButtonClickListener(boolean closeOnClick, OnClickListener listener) {
        OnClickListener clickHandler = new ClickListenerDecorator(listener, closeOnClick);
        positiveButton.setOnClickListener(clickHandler);
        neutralButton.setOnClickListener(clickHandler);
        negativeButton.setOnClickListener(clickHandler);
        return this;
    }

    public StandardDialog setPositiveButtonText(@StringRes int text) {
        return setPositiveButton(string(text), null);
    }

    public StandardDialog setPositiveButtonText(String text) {
        return setPositiveButton(text, null);
    }

    public StandardDialog setPositiveButtonColor(@ColorInt int color) {
        positiveButton.setTextColor(color);
        return this;
    }

    public StandardDialog setNegativeButtonColor(@ColorInt int color) {
        negativeButton.setTextColor(color);
        return this;
    }

    public StandardDialog setNeutralButtonColor(@ColorInt int color) {
        neutralButton.setTextColor(color);
        return this;
    }

    public StandardDialog setPositiveButtonColorRes(@ColorRes int colorRes) {
        return setPositiveButtonColor(color(colorRes));
    }

    public StandardDialog setNegativeButtonColorRes(@ColorRes int colorRes) {
        return setNegativeButtonColor(color(colorRes));
    }

    public StandardDialog setNeutralButtonColorRes(@ColorRes int colorRes) {
        return setNeutralButtonColor(color(colorRes));
    }

    @Override
    protected int getLayout() {
        return R.layout.dialog_standard;
    }
}
