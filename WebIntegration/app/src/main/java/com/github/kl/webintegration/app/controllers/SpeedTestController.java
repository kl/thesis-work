package com.github.kl.webintegration.app.controllers;

import android.content.Context;
import android.content.Intent;

import com.github.kl.webintegration.app.ForApplication;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class SpeedTestController extends PluginController {

    private static final String TYPE = "SPEED_TEST";
    private static final int CODE = 0xFADE;

    @Inject
    public SpeedTestController() {
        super(TYPE, CODE);
    }

    @Override
    public Intent getPluginIntent() {
        return new Intent("com.github.kl.webintegration.speedtest.MainActivity");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            JSONObject result = new JSONObject();
            result.put("message", "ok");
            notifyPluginResult(result, this);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
