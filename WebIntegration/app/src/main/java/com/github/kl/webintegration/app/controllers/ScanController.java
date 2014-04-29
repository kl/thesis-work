package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultListener;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ScanController extends PluginController {

    public static final String PLUGIN_RESULT_JSON_KEY = "message";

    public ScanController(String type, int requestCode) {
        super(type, requestCode);
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
        JSONObject jso = new JSONObject();
        try {
            jso.put(PLUGIN_RESULT_JSON_KEY, data.getStringExtra("SCAN_RESULT"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        notifyPluginResult(jso, this);
    }
}
