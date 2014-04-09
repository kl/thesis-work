package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import javax.inject.Inject;

public class PostProgressDialogHandler {

    private ProgressDialog dialog;

    @Inject
    public PostProgressDialogHandler() { }

    public void show(Context context) {
        dismiss();
        dialog = ProgressDialog.show(context,
                                     context.getString(R.string.post_progress_dialog_title),
                                     context.getString(R.string.post_progress_dialog_message),
                                     true);
    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
