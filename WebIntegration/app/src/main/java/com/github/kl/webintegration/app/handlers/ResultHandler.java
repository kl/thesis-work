package com.github.kl.webintegration.app.handlers;

import com.github.kl.webintegration.app.ControllerActivity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ResultHandler {

    protected Set<HandlerCompletedListener> completedListeners;

    protected void notifyListeners() {
        for (HandlerCompletedListener l : completedListeners) l.onHandlerCompleted();
    }

    private String type;

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

    public abstract void handlePluginNotFound(String pluginType);

    public abstract void handleResult(Map<String, String> result);

    public abstract void handleCancel(String type);

    public static interface HandlerCompletedListener {
        public void onHandlerCompleted();
    }
}
