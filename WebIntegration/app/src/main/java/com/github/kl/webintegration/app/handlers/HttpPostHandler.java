package com.github.kl.webintegration.app.handlers;

import android.content.res.Resources;

import com.github.kl.webintegration.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

public class HttpPostHandler extends ResultHandler {

    // TODO: change these fields to preferences
    public static final String POST_CANCEL_KEY = "data";
    public static final String POST_CANCEL_VALUE = "USER_CANCEL";
    public static final String POST_CANCEL_TYPE_KEY = "type";

    public static final String POST_NOT_FOUND_KEY = "data";
    public static final String POST_NOT_FOUND_VALUE = "PLUGIN_NOT_FOUND";
    public static final String POST_NOT_FOUND_TYPE_KEY = "type";

    @Inject Resources resources;
    @Inject HttpClient httpClient;

    private static final String TYPE = "HTTP_POST";

    @Inject
    public HttpPostHandler() {
        super(TYPE);
    }

    @Override
    public void handleResult(JSONObject result) {
        post(result);
    }

    @Override
    public void handlePluginNotFound(String pluginType) {
        JSONObject jso = new JSONObject();
        try {
            jso.put(POST_NOT_FOUND_KEY, POST_NOT_FOUND_VALUE);
            jso.put(POST_NOT_FOUND_TYPE_KEY, pluginType);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        post(jso);
    }

    @Override
    public void handleCancel(String pluginType) {
        JSONObject jso = new JSONObject();
        try {
            jso.put(POST_CANCEL_KEY, POST_CANCEL_VALUE);
            jso.put(POST_CANCEL_TYPE_KEY, pluginType);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        post(jso);
    }

    private void post(JSONObject postData) {
        new Thread(getPostRunnable(postData)).start();
    }

    private String serverPostURI() {
        String IP   = resources.getString(R.string.SERVER_IP);
        String post = resources.getString(R.string.SERVER_POST);
        int port    = resources.getInteger(R.integer.SERVER_PORT);

        return "http://" + IP + ":" + port + "/" + post;
    }

    private Runnable getPostRunnable(final JSONObject postData) {

       return new Runnable() {

           @Override public void run() {
               try {
                   HttpPost httppost = new HttpPost(serverPostURI());
                   httppost.setEntity(new StringEntity(postData.toString()));
                   HttpResponse response = httpClient.execute(httppost);
                   response.getEntity().consumeContent();
                   notifyHandlerComplete();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       };
    }
}










