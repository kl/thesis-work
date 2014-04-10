package com.github.kl.webintegration.app.handlers;

import android.content.res.Resources;

import com.github.kl.webintegration.app.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public HttpPostHandler() {
        super(TYPE);
    }

    @Override
    public void handleResult(Map<String, String> result) {
        post(result);
    }

    @Override
    public void handlePluginNotFound(String pluginType) {
        Map<String, String> data = new HashMap<>();
        data.put(POST_NOT_FOUND_KEY, POST_NOT_FOUND_VALUE);
        data.put(POST_NOT_FOUND_TYPE_KEY, pluginType);
        post(data);
    }

    @Override
    public void handleCancel(String pluginType) {
        Map<String, String> data = new HashMap<>();
        data.put(POST_CANCEL_KEY, POST_CANCEL_VALUE);
        data.put(POST_CANCEL_TYPE_KEY, pluginType);
        post(data);
    }

    private void post(final Map<String, String> postData) {
        new Thread(getPostRunnable(postData)).start();
    }

    private String serverPostURI() {
        String IP   = resources.getString(R.string.SERVER_IP);
        String post = resources.getString(R.string.SERVER_POST);
        int port    = resources.getInteger(R.integer.SERVER_PORT);

        return "http://" + IP + ":" + port + "/" + post;
    }

    private Runnable getPostRunnable(final Map<String, String> postData) {

       return new Runnable() {

           @Override public void run() {
               try {
                   HttpPost httppost = new HttpPost(serverPostURI());
                   httppost.setEntity(getPostFormEntity(postData));
                   HttpResponse response = httpClient.execute(httppost);
                   response.getEntity().consumeContent();
                   notifyListeners();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }

           private UrlEncodedFormEntity getPostFormEntity(final Map<String, String> postData) throws IOException {
               List<NameValuePair> nameValuePairs = new ArrayList<>();

               for (Map.Entry<String, String> entry : postData.entrySet()) {
                   nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
               }
               return new UrlEncodedFormEntity(nameValuePairs);
           }
       };
    }
}










