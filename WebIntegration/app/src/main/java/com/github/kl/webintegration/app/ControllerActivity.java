package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler.HandlerCompletedListener;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Range;

import android.preference.Preference.OnPreferenceChangeListener;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

public class ControllerActivity extends Activity implements PluginResultHandler, HandlerCompletedListener {

    public static final String LOG_TAG = "WebIntegration";

    @Inject @Named("pluginControllers") Set<PluginController> pluginControllers;
    @Inject @Named("resultHandlers") Set<ResultHandler> resultHandlers;

    @Inject
    ProgressDialogFactory progressDialogFactory;

    private PluginController controller;
    private ResultHandler    handler;
    private ProgressDialog   progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrapInjection();
        Intent intent = getIntent();

        if (isStartedForPlugin(intent)) {
            startPlugin(intent);
        } else {
            setSettingsFragment();
        }
    }

    private boolean isStartedForPlugin(Intent intent) {
        String scheme = intent.getScheme();
        return scheme != null && scheme.equals("app");
    }

    private void setSettingsFragment() {
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    private void startPlugin(Intent intent) {
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
        List<String> segments = data.getPathSegments(); // TODO: handle null here
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
    public void onPluginResult(JSONObject result, PluginController controller) {
        handleProgressDialog();
        handler.handleResult(result);
    }

    @Override
    public void onPluginCancel(PluginController controller) {
        handleProgressDialog();
        handler.handleCancel(controller.getType());
    }

    @Override
    public void onHandlerCompleted() {
        if (progressDialog != null) progressDialog.dismiss();
        finish();
    }

    private void handleProgressDialog() {
        if (handler.isUsingProgressDialog()) {
            progressDialog = progressDialogFactory.newProgressDialog(this);
            handler.onCustomizeProgressDialog(progressDialog);
            progressDialog.show();
        }
    }


}


















