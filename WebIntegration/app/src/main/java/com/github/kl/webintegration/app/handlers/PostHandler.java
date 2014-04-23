package com.github.kl.webintegration.app.handlers;

import android.app.ProgressDialog;
import android.util.Log;

import com.github.kl.webintegration.app.Settings;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.inject.Inject;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public abstract class PostHandler extends ResultHandler {

    @Inject Settings settings;

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

    private void post(final JSONObject postData) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    performPost(postData);
                    notifyHandlerComplete();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    notifyHandlerComplete(); // TODO: make error callback here
                }
            }
        }).start();
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
    public void onCustomizeProgressDialog(ProgressDialog dialog) {
        dialog.setIndeterminate(true);
        dialog.setTitle("Posting data to server");
        dialog.setMessage("Please wait...");
    }
}
