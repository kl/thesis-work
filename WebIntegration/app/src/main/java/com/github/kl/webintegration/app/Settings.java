package com.github.kl.webintegration.app;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

public class Settings {

    @Inject @ForApplication Context context;
    @Inject @ForApplication SharedPreferences sharedPreferences;

    public String getHttpServerIP() {
        return readValue(R.string.pref_key_http_server_ip);
    }

    public int getHttpServerPort() {
        String port = readValue(R.string.pref_key_http_server_port);
        return Integer.parseInt(port);
    }

    public String getHttpsServerIP() {
        return readValue(R.string.pref_key_https_server_ip);
    }

    public int getHttpsServerPort() {
        String port = readValue(R.string.pref_key_https_server_port);
        return Integer.parseInt(port);
    }

    public int getLocalServerPort() {
        String port = readValue(R.string.pref_key_local_server_port);
        return Integer.parseInt(port);
    }

    private String readValue(int prefKeyId) {
        String key = context.getString(prefKeyId);
        String value = sharedPreferences.getString(key, null);
        if (value == null) throw new RuntimeException("Error reading key from SharedPreferences");
        return value;
    }
}

