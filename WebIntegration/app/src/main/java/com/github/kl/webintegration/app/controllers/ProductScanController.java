package com.github.kl.webintegration.app.controllers;

import android.content.Intent;

import javax.inject.Inject;

public class ProductScanController extends ScanControllerBase implements PluginController {

    private static final String TYPE = "SCANNER_PRODUCT";
    private static final int REQUEST_CODE = 0xABBA;

    @Inject
    public ProductScanController() {
        super(TYPE, REQUEST_CODE);
    }

    @Override
    public Intent getPluginIntent() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");   // Scan product bar codes
        intent.putExtra("PROMPT_MESSAGE", "Läs in en produkt-kod");
        intent.putExtra("SCAN_WIDTH", 500);
        intent.putExtra("SCAN_HEIGHT", 50);
        intent.putExtra("SAVE_HISTORY", false); // Don't save barcode in Barcode Scanner's history
        return intent;
    }
}
