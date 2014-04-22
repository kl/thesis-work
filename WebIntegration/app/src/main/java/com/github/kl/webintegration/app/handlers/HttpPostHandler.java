package com.github.kl.webintegration.app.handlers;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.util.Log;

import com.github.kl.webintegration.app.R;
import com.github.kl.webintegration.app.WebIntegrationApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Inject;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public class HttpPostHandler extends PostHandler {

    private static final String TYPE = "HTTP_POST";

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
        String IP   = "192.168.0.224";
        String post = "android";
        int port    = 9000;

        return "http://" + IP + ":" + port + "/" + post;
    }
}










