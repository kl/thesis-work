package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultHandler;

import java.util.HashMap;
import java.util.Map;

public class FilePickerPluginController implements PluginController {

    private static final int REQUEST_CODE = 1;

    @Override
    public String getType() {
        return "FILE_PICKER";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data, PluginResultHandler handler) {

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String filePath = data.getDataString();

            Map<String, String> result = new HashMap<>();
            result.put("data", filePath);
            handler.onPluginResult(result);
        }
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        return intent;
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }
}
