package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class PluginController {

    private String type;
    private int requestCode;

    public PluginController(String type, int requestCode) {
        this.type = type;
        this.requestCode = requestCode;
    }

    abstract public Intent getPluginIntent();

    abstract public void onActivityResult(int requestCode, int resultCode, Intent data, PluginResultHandler handler);

    public String getType() { return type; }

    public int getRequestCode() { return requestCode; }
}

