package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import javax.inject.Inject;

public class AllScanController extends ScanControllerBase implements PluginController {

    private static final String TYPE = "SCANNER_ALL";
    private static final int REQUEST_CODE = 0xCAFE;

    @Inject
    public AllScanController() {
        super(TYPE, REQUEST_CODE);
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "SCAN_MODE");   // Scan all formats
        intent.putExtra("PROMPT_MESSAGE", "Läs in en kod");
        intent.putExtra("SAVE_HISTORY", false);       // Don't save barcode in Barcode Scanner's history
        return intent;
    }
}
