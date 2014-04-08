package com.github.kl.webintegration.app;

import com.github.kl.webintegration.app.controllers.BarcodeController;
import com.github.kl.webintegration.app.controllers.PluginController;

import java.util.Map;

public interface PluginResultHandler {
    public void onPluginResult(Map<String, String> result, PluginController pluginController);

    void onPluginCancel(PluginController pluginController);
}
