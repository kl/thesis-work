
package com.github.kl.webintegration.app;

import com.github.kl.webintegration.app.controllers.PluginController;

import org.json.JSONObject;

import java.util.Map;

public interface PluginResultHandler {
    public void onPluginResult(JSONObject result, PluginController pluginController);

    void onPluginCancel(PluginController pluginController);
}

