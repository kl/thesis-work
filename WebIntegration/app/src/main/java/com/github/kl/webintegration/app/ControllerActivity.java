package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.kl.webintegration.app.PluginControllerCollection.PluginControllerNotFoundException;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.DataPoster.PostCompletedListener;
import com.google.common.base.Preconditions;

import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class ControllerActivity extends Activity implements PluginResultHandler, PostCompletedListener {

    public static String LOG_TAG = "WebIntegration";

    @Inject DataPoster poster;
    @Inject PluginControllerCollection controllers;

    private PluginController selectedPluginController;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrapInjection();
        poster.addOnPostCompletedListener(this);

        try {
            selectedPluginController = findControllerForIntent(getIntent());
            Intent pluginIntent = selectedPluginController.getPluginIntent();
            startActivityForResult(pluginIntent, selectedPluginController.getRequestCode());

        } catch (PluginControllerNotFoundException e) {
            Log.e(LOG_TAG, "Unknown plugin controller for type: " + e.getType());
            postPluginNotFound();
        }
    }

    private void bootstrapInjection() {
        ((WebIntegrationApplication)getApplication()).inject(this);
    }

    private PluginController findControllerForIntent(Intent intent) throws PluginControllerNotFoundException {
        Uri data = intent.getData();
        String pluginType = data.getAuthority();
        return controllers.getPluginController(pluginType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        selectedPluginController.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onPluginResult(Map<String, String> result, PluginController controller) {
        showPostProgressDialog();
        poster.post(result);
    }

    @Override
    public void onPluginCancel(PluginController controller) {
        showPostProgressDialog();
        postCancel(controller.getType());
    }

    private void postCancel(String type) {
        Map<String, String> data = new HashMap<>();
        data.put("data", "PLUGIN_USER_CANCEL");
        data.put("type", type);
        poster.post(data);
    }

    private void postPluginNotFound() {
        Map<String, String> data = new HashMap<>();
        data.put("data", "PLUGIN_NOT_FOUND");
        poster.post(data);
    }

    @Override
    public void onPostCompleted(HttpResponse result) {
        if (progressDialog != null) progressDialog.dismiss();
        finish();
    }

    private void showPostProgressDialog() {
        progressDialog = ProgressDialog.show(this,
                                             getString(R.string.post_progress_dialog_title),
                                             getString(R.string.post_progress_dialog_message),
                                             true);
    }
}


















