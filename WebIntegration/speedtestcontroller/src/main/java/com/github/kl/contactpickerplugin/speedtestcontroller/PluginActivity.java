package com.github.kl.contactpickerplugin.speedtestcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PluginActivity extends Activity {

    private static final int REQUEST_CODE = 0xFEED;
    private static final String EMAIL_KEY = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(getSpeedTestIntent(), REQUEST_CODE);
    }

    private Intent getSpeedTestIntent() {
        return new Intent("com.github.kl.webintegration.speedtest.LAUNCH");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            setResult(resultCode, data);
            finish();
        }
    }
}
