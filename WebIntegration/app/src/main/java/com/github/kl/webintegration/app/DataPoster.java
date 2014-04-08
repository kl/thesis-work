package com.github.kl.webintegration.app;

import android.content.res.Resources;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class DataPoster {

    @Inject Resources resources;
    @Inject HttpClient httpClient;

    private final Collection<PostCompletedListener> listeners = new HashSet<>();

    @Inject public DataPoster() { }

    public void post(final Map<String, String> postData) {
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
                   //try {
                   //    Thread.sleep(10000);
                   //} catch (InterruptedException e) {
                   //    e.printStackTrace();
                   //}
                   HttpPost httppost = new HttpPost(serverPostURI());
                   httppost.setEntity(getPostFormEntity(postData));
                   HttpResponse result = httpClient.execute(httppost);
                   notifyListeners(result);
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

    private void notifyListeners(HttpResponse result) {
        for (PostCompletedListener l : listeners) l.onPostCompleted(result);
    }

    public void addOnPostCompletedListener(PostCompletedListener listener) {
        listeners.add(listener);
    }

    public static interface PostCompletedListener {
        public void onPostCompleted(HttpResponse result);
    }
}










