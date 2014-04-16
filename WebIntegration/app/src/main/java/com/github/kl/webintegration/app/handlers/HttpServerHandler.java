package com.github.kl.webintegration.app.handlers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.github.kl.webintegration.app.ForApplication;
import com.github.kl.webintegration.app.Injector;
import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import fi.iki.elonen.NanoHTTPD;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public class HttpServerHandler extends ResultHandler {

    private static final String TYPE = "HTTP_SERVER";

    @Inject @ForApplication
    Context context;

    @Inject
    public HttpServerHandler() {
        super(TYPE);
    }

    @Override
    public void handleResult(JSONObject result) {
        Log.d(LOG_TAG, "handleResult");
        context.startService(getServerIntent(result.toString()));

        notifyHandlerComplete();
    }

    @Override
    public void handlePluginNotFound(String pluginType) {
        Log.d(LOG_TAG, "handlePluginNotFound");
        String json = "{\"message\": \"PLUGIN_NOT_FOUND\"}";
        context.startService(getServerIntent(json));

        notifyHandlerComplete();
    }

    @Override
    public void handleCancel(String type) {
        Log.d(LOG_TAG, "handleCancel");
        String json = "{\"message\": \"USER_CANCEL\"}";
        context.startService(getServerIntent(json));

        notifyHandlerComplete();
    }

    private Intent getServerIntent(String json) {
        Intent intent = new Intent(context, ServerService.class);
        intent.putExtra("JSON", json);
        return intent;
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
            try {
                JSONObject result = new JSONObject(intent.getStringExtra("JSON"));
                server.setResponseResult(result);
                server.start();
            } catch (IOException|JSONException e) {
                throw new RuntimeException(e);
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











