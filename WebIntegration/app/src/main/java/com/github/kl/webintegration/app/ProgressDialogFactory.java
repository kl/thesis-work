package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import javax.inject.Inject;

public class ProgressDialogFactory {

    @Inject
    public ProgressDialogFactory() { }

    public ProgressDialog newProgressDialog(Context context) {
        return new ProgressDialog(context);
    }

    public ProgressDialog newProgressDialog(Context context, int theme) {
        return new ProgressDialog(context, theme);
    }
}
