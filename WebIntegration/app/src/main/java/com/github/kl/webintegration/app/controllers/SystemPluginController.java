package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.github.kl.webintegration.app.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * This is a special controller that is used to launch plugins that are implemented as separate
 * applications. Before getPluginIntent is called, setPluginIntent must be called in order to
 * set the type of the plugin that controller should start.
 */
public class SystemPluginController extends PluginController {

    private static final String TYPE = "SYSTEM_PLUGIN_CONTROLLER";
    private static final int REQUEST_CODE = 0xDEAD;

    @Inject Settings settings;

    private Intent pluginIntent;

    public SystemPluginController() {
        super(TYPE, REQUEST_CODE);
    }

    public void setPluginIntent(String pluginName) {
        pluginIntent = new Intent();
        pluginIntent.setAction(pluginName);
        pluginIntent.addCategory(settings.getPluginIntentCategory());
    }

    @Override
    public Intent getPluginIntent() {
        if (pluginIntent == null) {
            throw new IllegalStateException("You must call setPluginIntent before you call this method");
        }
        return pluginIntent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getRequestCode()) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    handleResultOk(data);
                    break;
                case Activity.RESULT_CANCELED:
                    notifyPluginCancel(this);
                    break;
            }
        }
    }

    private void handleResultOk(Intent data) {
        try {
            JSONObject json = bundleToJson(data.getExtras());
            notifyPluginResult(json, this);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject bundleToJson(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();
        for (String key : bundle.keySet()) { json.put(key, bundle.get(key)); }
        return json;
    }
}



