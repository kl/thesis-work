package com.github.kl.webintegration.app;

import android.content.Context;
import android.content.SharedPreferences;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

@ThreadSafe
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

    public String getHttpServerPostPath() {
        return readValue(R.string.pref_key_http_server_post_path);
    }

    public String getHttpsServerIP() {
        return readValue(R.string.pref_key_https_server_ip);
    }

    public int getHttpsServerPort() {
        String port = readValue(R.string.pref_key_https_server_port);
        return Integer.parseInt(port);
    }

    public String getHttpsServerPostPath() {
        return readValue(R.string.pref_key_https_server_post_path);
    }

    public int getLocalServerPort() {
        String port = readValue(R.string.pref_key_local_server_port);
        return Integer.parseInt(port);
    }

    public String getPluginTypeKey() {
        return readValue(R.string.pref_key_protocol_plugin_type_key);
    }

    public String getUserCancelKey() {
        return readValue(R.string.pref_key_protocol_cancel_key);
    }

    public String getUserCancelValue() {
        return readValue(R.string.pref_key_protocol_cancel_value);
    }

    public String getPluginNotFoundKey() {
        return readValue(R.string.pref_key_protocol_not_found_key);
    }

    public String getPluginNotFoundValue() {
        return readValue(R.string.pref_key_protocol_not_found_value);
    }

    private String readValue(int prefKeyId) {
        String key = context.getString(prefKeyId);
        String value = sharedPreferences.getString(key, null);
        if (value == null) throw new RuntimeException("Error reading key from SharedPreferences");
        return value;
    }

    public int getLocalServerTimeoutMs() {
        int timeoutSeconds = Integer.parseInt(readValue(R.string.pref_key_local_server_timeout));
        return timeoutSeconds * 1000;
    }

    public boolean getSystemPluginsEnabled() {
        String key = context.getString(R.string.pref_key_other_system_plugins_enabled);
        return sharedPreferences.getBoolean(key, false);
    }

    public String getPluginIntentAction() {
        return readValue(R.string.pref_key_other_intent_action);
    }
}



















