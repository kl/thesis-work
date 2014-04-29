package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import com.github.kl.webintegration.app.ControllerActivity;
import com.github.kl.webintegration.app.PluginResultListener;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public abstract class PluginController {

    protected Set<PluginResultListener> resultListeners;

    private String type;
    private int requestCode;

    public PluginController(String type, int requestCode) {
        this.type = type;
        this.requestCode = requestCode;
        resultListeners = new HashSet<>();
    }

    public void addPluginResultListener(PluginResultListener listener) {
        resultListeners.add(listener);
    }

    public void removePluginResultListener(PluginResultListener listener) {
        resultListeners.remove(listener);
    }

    protected void notifyPluginResult(JSONObject result, PluginController controller) {
        for (PluginResultListener l : resultListeners) l.onPluginResult(result, controller);
    }

    protected void notifyPluginCancel(PluginController controller) {
        for (PluginResultListener l : resultListeners) l.onPluginCancel(controller);
    }

    public String getType() { return type; }

    public int getRequestCode() { return requestCode; }

    abstract public Intent getPluginIntent();

    abstract public void onActivityResult(int requestCode, int resultCode, Intent data);
}



