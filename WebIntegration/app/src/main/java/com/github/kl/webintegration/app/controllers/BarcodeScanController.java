package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import javax.inject.Inject;

public class BarcodeScanController extends ScanControllerBase implements PluginController {

    private static final String TYPE = "SCANNER_BARCODE";
    private static final int REQUEST_CODE = 0xBEEF;

    @Inject
    public BarcodeScanController() {
        super(TYPE, REQUEST_CODE);
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_FORMATS", "UPC_A,UPC_E,EAN_8,EAN_13,CODE_39,CODE_93,CODE_128,ITF,RSS_14,RSS_EXPANDED");
        intent.putExtra("PROMPT_MESSAGE", "Läs in en barkod");
        intent.putExtra("SCAN_WIDTH", 500);
        intent.putExtra("SCAN_HEIGHT", 50);
        intent.putExtra("SAVE_HISTORY", false); // Don't save barcode in Barcode Scanner's history
        return intent;
    }
}
