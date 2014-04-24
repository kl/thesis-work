package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler.HandlerCompletedListener;

import org.json.JSONObject;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public class ControllerActivity extends Activity implements PluginResultHandler, HandlerCompletedListener {

    private static final String TAG_STATE_FRAGMENT = "state_fragment";
    private static final String TAG_ERROR_FRAGMENT = "error_fragment";

    @Inject @Named("pluginControllers") Set<PluginController> pluginControllers;
    @Inject @Named("resultHandlers")    Set<ResultHandler>    resultHandlers;
    @Inject ProgressDialogFactory progressDialogFactory;

    private PluginController    controller;
    private ResultHandler       handler;
    private ProgressDialog      progressDialog;
    private StateFragment       stateFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrapInjection();

        FragmentManager fm = getFragmentManager();
        stateFragment = (StateFragment)fm.findFragmentByTag(TAG_STATE_FRAGMENT);

        if (stateFragment == null) {
            stateFragment = new StateFragment();
            fm.beginTransaction().add(stateFragment, TAG_STATE_FRAGMENT).commit();
            startPlugin(getIntent());
            retainState();
        } else {
            restoreState();
        }
    }

    private void bootstrapInjection() {
        ((Injector)getApplication()).inject(this);
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

    private void restoreState() {
        Log.d(LOG_TAG, "restoreState");
        handler    = stateFragment.resultHandler;
        controller = stateFragment.pluginController;
    }

    private void retainState() {
        Log.d(LOG_TAG, "retainState");
        stateFragment.resultHandler = handler;
        stateFragment.pluginController = controller;
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
        Log.d(LOG_TAG, "onActivityResult");
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
        dismissProgressDialog();
        finish();
    }

    @Override
    public void onHandlerError(String errorMessage) {
        dismissProgressDialog();
        postErrorMessage(errorMessage);
    }

    private void postErrorMessage(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showErrorMesssage(errorMessage);
            }
        });
    }

    private void showErrorMesssage(String errorMessage) {
        if (isFinishing()) return;
        ErrorDialogFragment errorFragment = ErrorDialogFragment.newInstance(errorMessage);
        errorFragment.show(getFragmentManager(), TAG_ERROR_FRAGMENT);
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) progressDialog.dismiss();
    }

    private void handleProgressDialog() {
        if (handler.isUsingProgressDialog()) {
            progressDialog = progressDialogFactory.newProgressDialog(this);
            handler.onCustomizeProgressDialog(progressDialog);
            progressDialog.show();
        }
    }

    private void onErrorDialogFinish() {
        FragmentManager fm = getFragmentManager();
        ErrorDialogFragment edf = (ErrorDialogFragment)fm.findFragmentByTag(TAG_ERROR_FRAGMENT);
        if (edf != null) edf.dismiss();
        finish();
    }

    private static class StateFragment extends Fragment {

        // The following objects need to be retained across configuration changes
        PluginController pluginController;
        ResultHandler resultHandler;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {

        public static ErrorDialogFragment newInstance(String message) {
            ErrorDialogFragment dialog = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putString("message", message);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ControllerActivity activity = (ControllerActivity)getActivity();
            if (activity == null) throw new RuntimeException("getActivity returned null");

            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface di, int i) {
                            activity.onErrorDialogFinish();
                        }
                    })
                    .create();
        }
    }
}


















