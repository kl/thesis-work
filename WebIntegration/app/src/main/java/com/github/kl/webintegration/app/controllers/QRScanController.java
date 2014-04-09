package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import javax.inject.Inject;

public class QRScanController extends ScanController {

    private static final String TYPE = "SCANNER_QR";
    private static final int REQUEST_CODE = 0xC0DE;

    @Inject
    public QRScanController() {
        super(TYPE, REQUEST_CODE);
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");   // Scan all formats
        intent.putExtra("PROMPT_MESSAGE", "Läs in en QR-kod");
        //intent.putExtra("SCAN_WIDTH", 500);
        //intent.putExtra("SCAN_HEIGHT", 50);
        intent.putExtra("SAVE_HISTORY", false); // Don't save barcode in Barcode Scanner's history
        return intent;
    }
}
