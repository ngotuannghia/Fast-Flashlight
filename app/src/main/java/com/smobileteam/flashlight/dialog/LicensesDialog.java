package com.smobileteam.flashlight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;

import com.smobileteam.flashlight.R;


public class LicensesDialog extends AbsBaseDialog<LicensesDialog> {
    private Button confirmButton;
    private WebView webView;

    private int infoDialogId;

    public LicensesDialog(Context context) {
        super(context);
    }

    public LicensesDialog(Context context, int theme) {
        super(context, theme);
    }

    {
        webView = findView(R.id.wv_license_content);
        confirmButton = findView(R.id.ld_btn_confirm);
        confirmButton.setOnClickListener(new ClickListenerDecorator(null, true));
        infoDialogId = -1;
        initContent();
    }

    public LicensesDialog setConfirmButtonText(@StringRes int text) {
        return setConfirmButtonText(string(text));
    }

    public LicensesDialog setConfirmButtonText(String text) {
        confirmButton.setText(text);
        return this;
    }

    public LicensesDialog setConfirmButtonColor(int color) {
        confirmButton.setTextColor(color);
        return this;
    }

    @Override
    public Dialog show() {
        if (infoDialogId == -1) {
            return super.show();
        }
        return super.show();
    }

    private void initContent() {
        webView.loadUrl("file:///android_asset/licenses.html");
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("http://") || url != null && url.startsWith("https://")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.dialog_licenses;
    }

}
