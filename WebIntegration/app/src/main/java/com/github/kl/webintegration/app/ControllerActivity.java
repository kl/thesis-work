package com.github.kl.webintegration.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.kl.webintegration.app.PluginControllerCollection.PluginControllerNotFoundException;
import com.github.kl.webintegration.app.controllers.PluginController;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class ControllerActivity extends Activity implements PluginResultHandler {

    public static String LOG_TAG = "WebIntegration";

    @Inject DataPoster poster;
    @Inject PluginControllerCollection controllers;

    private PluginController selectedPluginController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WebIntegrationApplication)getApplication()).inject(this);

        try {
            selectedPluginController = findControllerForIntent(getIntent());
            Intent pluginIntent = selectedPluginController.getPluginIntent();
            startActivityForResult(pluginIntent, selectedPluginController.getRequestCode());

        } catch (PluginControllerNotFoundException e) {
            Log.e(LOG_TAG, "Unknown plugin controller for type: " + e.getType());
            poster.post(getPluginNotFoundData());
            finish();
        }
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
    public void onPluginResult(Map<String, String> result) {
        poster.post(result);
    }

    private Map<String, String> getPluginNotFoundData() {
        Map<String, String> data = new HashMap<>();
        data.put("data", "PLUGIN_NOT_FOUND");
        return data;
    }
}


















