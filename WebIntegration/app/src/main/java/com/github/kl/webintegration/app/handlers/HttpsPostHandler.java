package com.github.kl.webintegration.app.handlers;

import android.content.res.Resources;
import android.util.Log;

import com.github.kl.webintegration.app.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsPostHandler extends PostHandler {

    private static final String TYPE = "HTTPS_POST";

    @Inject Resources resources;

    @Inject
    public HttpsPostHandler() {
        super(TYPE);
        disableHttpsCertificateChecks();
    }

    @Override
    protected HttpURLConnection getConnection() throws IOException {
        URL url = new URL(getPostUrl());
        return (HttpsURLConnection)url.openConnection();
    }

    private String getPostUrl() {
        String IP   = "192.168.0.213";
        String post = "android";
        int port    = 9000;

        return "http://" + IP + ":" + port + "/" + post;
    }


    // TODO: ughh...
    private void disableHttpsCertificateChecks() {
        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}