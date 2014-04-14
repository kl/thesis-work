package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class ScanController extends PluginController {

    public ScanController(String type, int requestCode) {
        super(type, requestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, PluginResultHandler handler) {
        if (requestCode == getRequestCode()) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    handleResultOk(data, handler);
                    break;
                case Activity.RESULT_CANCELED:
                    handler.onPluginCancel(this);
                    break;
            }
        }
    }

    private void handleResultOk(Intent data, PluginResultHandler handler) {
        JSONObject jso = new JSONObject();
        try {
            jso.put("message", data.getStringExtra("SCAN_RESULT"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        handler.onPluginResult(jso, this);
    }
}
