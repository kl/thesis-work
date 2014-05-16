package com.github.kl.webintegration.app.handlers;

import android.os.Bundle;
import android.util.Log;

import com.github.kl.webintegration.app.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import javax.inject.Inject;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public abstract class PostHandler extends ResultHandler {

    @Inject Settings settings;

    private Thread postThread;

    public PostHandler(String type) {
        super(type);
    }

    protected abstract HttpURLConnection getConnection() throws IOException;

    @Override
    public void handleResult(JSONObject result) {
        post(result);
    }

    @Override
    public void handlePluginNotFound(String pluginType) {
        JSONObject jso = new JSONObject();
        try {
            jso.put(settings.getPluginNotFoundKey(), settings.getPluginNotFoundValue());
            jso.put(settings.getPluginTypeKey(), pluginType);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        post(jso);
    }

    @Override
    public void handleCancel(String pluginType) {
        JSONObject jso = new JSONObject();
        try {
            jso.put(settings.getUserCancelKey(), settings.getUserCancelValue());
            jso.put(settings.getPluginTypeKey(), pluginType);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        post(jso);
    }

    @Override
    public void onUserAbort() {
        if (postThread == null) return;
        postThread.interrupt();
    }

    private void post(final JSONObject postData) {
        postThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    int code = performPost(postData);
                    if (Thread.interrupted()) return;
                    if (isResultCodeOK(code)) {
                        notifyHandlerComplete();
                    } else {
                        notifyHandlerError("Server did not return 200 OK. Code was: " + code);
                    }
                } catch (IOException e) {
                    if (Thread.interrupted()) return;
                    Log.e(LOG_TAG, e.getMessage());
                    notifyHandlerError("Server communication error: " + e.getMessage());
                }
            }
        });
        postThread.start();
    }

    private boolean isResultCodeOK(int code) {
        return code >= 200 && code < 300;
    }

    private int performPost(final JSONObject postData) throws IOException {
        HttpURLConnection con = getConnection();
        setConnectionOptions(con);
        sendPost(con, postData.toString());
        return con.getResponseCode();
    }

    private void setConnectionOptions(HttpURLConnection con) throws ProtocolException {
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept-Encoding", "identity");
        con.setDoOutput(true);  // required to POST with body.
    }

    private void sendPost(HttpURLConnection con, String postBody) throws IOException {
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(postBody);
        out.flush();
        out.close();
    }

    @Override
    public boolean isUsingProgressDialog() {
        return true;
    }

    @Override
    public void onCustomizeProgressDialog(Bundle bundle) {
        bundle.putBoolean("indeterminate", true);
        bundle.putString("title", "Posting data to server");
        bundle.putString("message", "Please wait...");
    }
}
