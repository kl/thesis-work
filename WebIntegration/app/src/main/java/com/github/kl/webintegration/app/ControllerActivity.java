package com.github.kl.webintegration.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.handlers.HttpPostHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class ControllerActivity extends Activity implements PluginResultHandler, ResultHandler.HandlerCompletedListener {

    public static final String LOG_TAG = "WebIntegration";

    @Inject @Named("pluginControllers") Set<PluginController> pluginControllers;
    @Inject @Named("resultHandlers") Set<ResultHandler> resultHandlers;

    @Inject PostProgressDialogHandler progressDialogHandler;

    private PluginController controller;
    private ResultHandler    handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrapInjection();

        // TODO: refactor

        Intent intent = getIntent();
        String pluginType = getPluginType(intent);
        String handlerType = getHandlerType(intent);

        handler = findResultHandler(handlerType);
        if (handler == null) finish();
        handler.addOnCompletedListener(this);

        controller = findPluginController(pluginType);
        if (controller != null) {
            startActivityForResult(controller.getPluginIntent(), controller.getRequestCode());
        } else {
            handler.handlePluginNotFound(pluginType);
        }
    }

    private void bootstrapInjection() {
        ((Injector)getApplication()).inject(this);
    }

    private String getPluginType(Intent intent) {
        return getDataPathSegment(intent, 0);
    }

    private String getHandlerType(Intent intent) {
        return getDataPathSegment(intent, 1);
    }

    private String getDataPathSegment(Intent intent, int index) {
        Uri data = intent.getData();
        List<String> segments = data.getPathSegments();
        return segments.get(index);
    }

    private ResultHandler findResultHandler(String handlerType) {
        for (ResultHandler handler : resultHandlers) {
            if (handler.getType().equals(handlerType)) return handler;
        }
        return null;
    }

    private PluginController findPluginController(String pluginType) {
        for (PluginController controller : pluginControllers) {
            if (controller.getType().equals(pluginType)) return controller;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        controller.onActivityResult(requestCode, resultCode, data, this);
    }

    @Override
    public void onPluginResult(Map<String, String> result, PluginController controller) {
        progressDialogHandler.show(this);
        handler.handleResult(result);
    }

    @Override
    public void onPluginCancel(PluginController controller) {
        progressDialogHandler.show(this);
        handler.handleCancel(controller.getType());
    }

    @Override
    public void onHandlerCompleted() {
        progressDialogHandler.dismiss();
        finish();
    }
}


















