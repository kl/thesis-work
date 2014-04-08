package com.github.kl.webintegration.app.controllers;

import android.app.Activity;
import android.content.Intent;

import com.github.kl.webintegration.app.PluginResultHandler;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class BarcodeController implements PluginController {

    private static final String TYPE = "BARCODE_SCANNER";
    private static final int REQUEST_CODE = 0xBEEF;

    @Inject
    public BarcodeController() { }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("MODE", "SCAN_MODE");   // Scan all formats
        intent.putExtra("PROMPT_MESSAGE", "Läs in en barkod");
        intent.putExtra("SCAN_WIDTH", 500);
        intent.putExtra("SCAN_HEIGHT", 50);
        intent.putExtra("SAVE_HISTORY", false); // Don't save barcode in Barcode Scanner's history
        return intent;
    }

    @Override
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
        Map<String, String> result = new HashMap<>();
        result.put("data", data.getStringExtra("SCAN_RESULT"));
        handler.onPluginResult(result, this);
    }
}
