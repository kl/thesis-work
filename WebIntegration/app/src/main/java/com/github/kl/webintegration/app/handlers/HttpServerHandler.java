package com.github.kl.webintegration.app.handlers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.github.kl.webintegration.app.ForApplication;
import com.github.kl.webintegration.app.Injector;
import com.github.kl.webintegration.app.R;
import com.github.kl.webintegration.app.Settings;
import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Named;

import fi.iki.elonen.NanoHTTPD;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

/**
 * This handler starts a NanoHTTPD server which runs in a separate Service. The server listens
 * for a request from the web browser and returns the plugin result, after which it terminates itself.
 * This handler starts the Service (which in turn starts the server) in its plugin callback methods.
 * Then notifyHandlerComplete() is called and the handler exits. At this point the server is running
 * in the Service in the background, and its not possible for the controller activity to know if
 * the AJAX request is successfully served or not. A better way to implement this handler would be
 * to start the server directly without a separate Service class, and then wait to call
 * notifyHandlerComplete until a request has been served. The reason this is not done is that the
 * standard Android web browser pauses all AJAX calls when the browser is not the current activity.
 * So for the AJAX request to come in the the server, the current main activity
 * (which is ControllerActivity) must be popped from the back stack (i.e. it must exit), so that
 * the web browser can become the active activity.
 */
public class HttpServerHandler extends ResultHandler {

    private static final String TYPE = "HTTP_SERVER";

    @Inject @ForApplication Context context;
    @Inject Settings settings;

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
        context.startService(getServerIntent(getPluginNotFoundJson()));
        notifyHandlerComplete();
    }

    @Override
    public void handleCancel(String type) {
        Log.d(LOG_TAG, "handleCancel");
        context.startService(getServerIntent(getCancelJson()));
        notifyHandlerComplete();
    }

    @Override
    public void onUserCancel() {}

    private String getPluginNotFoundJson() {
        return "\"" + settings.getPluginNotFoundKey() + "\":" +
               "\"" + settings.getPluginNotFoundValue() + "\"}";
    }

    private String getCancelJson() {
        return "\"" + settings.getUserCancelKey() + "\":" +
               "\"" + settings.getUserCancelValue() + "\"}";
    }

    private Intent getServerIntent(String json) {
        Intent intent = new Intent(context, ServerService.class);
        intent.putExtra("json", json);
        intent.putExtra("timeout", settings.getLocalServerTimeoutMs());
        return intent;
    }

    public static class ServerService extends Service implements NanoHTTPD.SendCompleted {

        @Inject Server server;
        @Inject @Named("uiThreadHandler") Handler uiThreadHandler;
        @Inject Timer timer;

        private int serverTimeoutMs;

        @Override
        public void onCreate() {
            super.onCreate();
            ((Injector)getApplication()).inject(this);
            NanoHTTPD.sendCompletedListener = this;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d(LOG_TAG, "onStartCommand");
            try {
                serverTimeoutMs = getTimeoutValue(intent);
                JSONObject result = new JSONObject(intent.getStringExtra("json"));
                server.setResponseResult(result);
                server.start();
                startServerTimeout();
            } catch (IOException|JSONException e) {
                throw new RuntimeException(e);
            }
            return START_STICKY;
        }

        private int getTimeoutValue(Intent intent) {
            int timeout = intent.getIntExtra("timeout", -1);
            if (timeout == -1) throw new RuntimeException("timeout not set in service intent");
            return timeout;
        }

        @Override
        public IBinder onBind(Intent intent) { return null; }

        @Override
        public void onSendCompleted() {
            Log.d(LOG_TAG, "onSendCompleted");
            terminate();
        }

        private void startServerTimeout() {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Local server timed out! (" + serverTimeoutMs + " ms)");
                    showTimeoutMessage();
                    terminate();
                }
            }, serverTimeoutMs);
        }

        private void showTimeoutMessage() {
            final Context context = this;
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                   Toast.makeText(context,
                                  context.getText(R.string.local_server_timeout),
                                  Toast.LENGTH_LONG).show();
                }
            });
        }

        private void terminate() {
            if (timer != null) timer.cancel();
            timer = null;
            if (server != null) server.stop();
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











