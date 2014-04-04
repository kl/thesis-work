package com.github.kl.webintegration.app;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class DataPoster {

    private static final String LOG_TAG       = "webapptest";
    private static final String INTENT_SCHEME = "salestab.android";
    private static final String SERVER_IP     = "192.168.0.213";
    private static final int SERVER_PORT      = 9001;
    private static final String POST          = "android";

    @Inject public DataPoster() { }

    public void post(final Map<String, String> postData) {
        new Thread(getPostRunnable(postData)).start();
    }

    private String serverPostURI() {
        return "http://" + SERVER_IP + ":" + SERVER_PORT + "/" + POST;
    }

    private Runnable getPostRunnable(final Map<String, String> postData) {

       return new Runnable() {
           @Override public void run() {

               HttpClient httpclient = new DefaultHttpClient();
               HttpPost httppost = new HttpPost(serverPostURI());

               List<NameValuePair> nameValuePairs = new ArrayList<>();

               for (Map.Entry<String, String> entry : postData.entrySet()) {
                   nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
               }

               try {
                   httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                   httpclient.execute(httppost);
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       };
    }

}










