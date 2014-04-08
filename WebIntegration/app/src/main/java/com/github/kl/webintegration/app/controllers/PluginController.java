package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultHandler;

import java.util.List;
import java.util.Map;

public interface PluginController {
    String getType();

    void onActivityResult(int requestCode, int resultCode, Intent data, PluginResultHandler handler);

    Intent getPluginIntent();

    int getRequestCode();
}
