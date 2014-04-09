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

import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class ControllerActivity extends Activity implements PluginResultHandler, PostCompletedListener {

    public static final String LOG_TAG = "WebIntegration";

    public static final String POST_CANCEL_KEY = "data";
    public static final String POST_CANCEL_VALUE = "USER_CANCEL";
    public static final String POST_CANCEL_TYPE_KEY = "type";

    public static final String POST_NOT_FOUND_KEY = "data";
    public static final String POST_NOT_FOUND_VALUE = "PLUGIN_NOT_FOUND";

    @Inject DataPoster poster;
    @Inject PluginControllerCollection controllers;
    @Inject PostProgressDialogHandler progressDialogHandler;

    private PluginController selectedPluginController;

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
        ((Injector)getApplication()).inject(this);
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
        progressDialogHandler.show(this);
        poster.post(result);
    }

    @Override
    public void onPluginCancel(PluginController controller) {
        progressDialogHandler.show(this);
        postCancel(controller.getType());
    }

    private void postCancel(String type) {
        Map<String, String> data = new HashMap<>();
        data.put(POST_CANCEL_KEY, POST_CANCEL_VALUE);
        data.put(POST_CANCEL_TYPE_KEY, type);
        poster.post(data);
    }

    private void postPluginNotFound() {
        Map<String, String> data = new HashMap<>();
        data.put(POST_NOT_FOUND_KEY, POST_NOT_FOUND_VALUE);
        poster.post(data);
    }

    @Override
    public void onPostCompleted(HttpResponse result) {
        progressDialogHandler.dismiss();
        finish();
    }
}


















