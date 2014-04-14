package com.github.kl.webintegration.app.handlers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.kl.webintegration.app.ForApplication;
import com.github.kl.webintegration.app.Injector;
import com.google.common.base.Preconditions;

import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import fi.iki.elonen.NanoHTTPD;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public class HttpServerHandler extends ResultHandler {

    private static JSONObject result;
    public static JSONObject getResult() { return result; }

    private static final String TYPE = "HTTP_SERVER";
    private static final int PORT = 9888;

    @Inject @ForApplication
    Context context;

    @Inject
    public HttpServerHandler() {
        super(TYPE);
    }

    @Override
    public void handlePluginNotFound(String pluginType) {

    }

    @Override
    public void handleResult(JSONObject result) {
        // Start server. Wait for GET from browser. Send result. Call back to activity.

        Log.d(LOG_TAG, "handleResult");
        HttpServerHandler.result = result; // hack to avoid Parcelable

        Intent intent = new Intent(context, ServerService.class);
        context.startService(intent);

        notifyHandlerComplete();
    }

    @Override
    public void handleCancel(String type) {
        Log.d(LOG_TAG, "handleCancel");
        Intent intent = new Intent(context, ServerService.class);
        context.startService(intent);

        notifyHandlerComplete();
    }

    public static class ServerService extends Service implements NanoHTTPD.SendCompleted {

        @Inject Server server;

        @Override
        public void onCreate() {
            super.onCreate();
            ((Injector)getApplication()).inject(this);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d(LOG_TAG, "onStartCommand");
            Server.sendCompletedListener = this;
            server.setResponseResult(HttpServerHandler.getResult());
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) { return null; }

        @Override
        public void onSendCompleted() {
            Log.d(LOG_TAG, "onSendCompleted");
            server.stop();
            server = null;
            stopSelf();
        }
    }

    public static class Server extends NanoHTTPD {

        private JSONObject result;
        public void setResponseResult(JSONObject result) { this.result = result; }

        public Server(int port) {
            super(port);
        }

        @Override
        public NanoHTTPD.Response serve(IHTTPSession session) {
            Log.d(LOG_TAG, "serve called");
            Preconditions.checkNotNull(result, "result must be set with setResponseResult");

            Response response = new Response(result.toString());
            response.setMimeType("application/json");
            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        }
    }
}











