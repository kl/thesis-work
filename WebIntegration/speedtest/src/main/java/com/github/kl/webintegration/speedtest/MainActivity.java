package com.github.kl.webintegration.speedtest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent result = new Intent();
        result.putExtra("result", "ok");
        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
