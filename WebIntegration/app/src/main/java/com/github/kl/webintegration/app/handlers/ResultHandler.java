package com.github.kl.webintegration.app.handlers;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.github.kl.webintegration.app.ControllerActivity;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ResultHandler {

    private String type;
    protected Set<HandlerCompletedListener> completedListeners;

    protected void notifyHandlerComplete() {
        for (HandlerCompletedListener l : completedListeners) l.onHandlerCompleted();
    }

    protected void notifyHandlerError(String errorMessage) {
        for (HandlerCompletedListener l : completedListeners) l.onHandlerError(errorMessage);
    }

    public ResultHandler(String type) {
        this.type = type;
        completedListeners = new HashSet<>();
    }

    public String getType() {
        return type;
    }

    public void addOnCompletedListener(HandlerCompletedListener listener) {
       completedListeners.add(listener);
    }

    public void removeOnCompletedListener(HandlerCompletedListener listener) {
        completedListeners.remove(listener);
    }

    public boolean isUsingProgressDialog() {
        return false;
    }

    public void onCustomizeProgressDialog(Bundle dialogConfigBundle) {}

    public abstract void handlePluginNotFound(String pluginType);

    public abstract void handleResult(JSONObject result);

    public abstract void handleCancel(String type);

    public abstract void onUserAbort();

    public static interface HandlerCompletedListener {
        public void onHandlerCompleted();
        public void onHandlerError(String errorMessage);
    }
}
