package com.github.kl.webintegration.app.handlers;

import android.content.SharedPreferences;

import com.github.kl.webintegration.app.R;
import com.github.kl.webintegration.app.Settings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class HttpPostHandler extends PostHandler {

    private static final String TYPE = "HTTP_POST";

    @Inject Settings settings;

    @Inject
    public HttpPostHandler() {
        super(TYPE);
    }

    @Override
    protected HttpURLConnection getConnection() throws IOException {
        URL url = new URL(getPostUrl());
        return (HttpURLConnection)url.openConnection();
    }

    private String getPostUrl() {
        String IP   = settings.getHttpServerIP();
        int port    = settings.getHttpServerPort();
        String post = "android";

        return "http://" + IP + ":" + port + "/" + post;
    }
}










