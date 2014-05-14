package com.github.kl.webintegration.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.controllers.SystemPluginController;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler.HandlerCompletedListener;
import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import static com.github.kl.webintegration.app.WebIntegrationApplication.LOG_TAG;

public class ControllerActivity extends Activity
        implements PluginResultListener, HandlerCompletedListener, Dialog.OnClickListener {

    private static final String TAG_STATE_FRAGMENT    = "state_fragment";
    private static final String TAG_ERROR_FRAGMENT    = "error_fragment";
    private static final String TAG_PROGRESS_FRAGMENT = "progress_fragment";

    @Inject @Named("pluginControllers") Set<PluginController> pluginControllers;
    @Inject @Named("resultHandlers")    Set<ResultHandler>    resultHandlers;

    @Inject Settings settings;
    @Inject SystemPluginController systemPluginController;

    private PluginController controller;
    private ResultHandler    handler;
    private StateFragment    stateFragment;

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
        } else {
            restoreRetainedState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (controller != null) controller.removePluginResultListener(this);
        if (handler != null) handler.removeOnCompletedListener(this);
    }

    private void bootstrapInjection() {
        ((Injector)getApplication()).inject(this);
    }

    private void startPlugin(Intent intent) {
        String handlerType = getHandlerType(intent);
        String pluginType  = getPluginType(intent);

        handler = findResultHandler(handlerType);
        if (handler != null) {
            stateFragment.resultHandler = handler;
            handler.addOnCompletedListener(this);
        } else {
            finish();
        }

        controller = findPluginController(pluginType);
        if (controller != null) {
            stateFragment.pluginController = controller;
            controller.addPluginResultListener(this);
            startActivityForResult(controller.getPluginIntent(), controller.getRequestCode());
        } else {
            handleProgressDialog();
            handler.handlePluginNotFound(pluginType);
        }
    }

    private void restoreRetainedState() {
        Log.d(LOG_TAG, "restoreRetainedState");
        controller = stateFragment.pluginController;
        handler = stateFragment.resultHandler;

        controller.addPluginResultListener(this);
        handler.addOnCompletedListener(this);

        if (stateFragment.retainedErrorMessage != null) {
            postErrorMessage(stateFragment.retainedErrorMessage);
            stateFragment.retainedErrorMessage = null;
        }
    }

    private String getPluginType(Intent intent) {
        return parseUri(intent.getData()).get(0);
    }

    private String getHandlerType(Intent intent) {
        return parseUri(intent.getData()).get(1);
    }

    private List<String> parseUri(Uri uri) {
        String[] parts = uri.toString().split("/+");
        return Arrays.asList(Arrays.copyOfRange(parts, 1, parts.length));
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

        systemPluginController.setPluginIntent(pluginType);
        if (settings.getSystemPluginsEnabled() && isIntentAvailable(systemPluginController.getPluginIntent())) {
            return systemPluginController;
        } else {
            return null;
        }
    }

    private boolean isIntentAvailable(Intent intent) {
        PackageManager pm = getPackageManager();
        assert pm != null;
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        controller.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPluginResult(JSONObject result, PluginController controller) {
        Log.d(LOG_TAG, "onPluginResult");
        handleProgressDialog();
        handler.handleResult(result);
    }

    @Override
    public void onPluginCancel(PluginController controller) {
        Log.d(LOG_TAG, "onPluginCancel");
        handleProgressDialog();
        handler.handleCancel(controller.getType());
    }

    private void handleProgressDialog() {
        if (handler.isUsingProgressDialog()) {
            Bundle options = new Bundle();
            handler.onCustomizeProgressDialog(options);

            ProgressDialogFragment progressFragment = ProgressDialogFragment.newInstance(options);
            progressFragment.setCancelable(false);
            progressFragment.show(getFragmentManager(), TAG_PROGRESS_FRAGMENT);
        }
    }

    @Override
    public void onHandlerCompleted() {
        Log.d(LOG_TAG, "onHandlerCompleted");
        dismissDialogFragment(TAG_PROGRESS_FRAGMENT);
        finish();
    }

    @Override
    public void onHandlerError(String errorMessage) {
        Log.d(LOG_TAG, "onHandlerError");
        dismissDialogFragment(TAG_PROGRESS_FRAGMENT);
        postErrorMessage(errorMessage);
    }

    private void postErrorMessage(final String errorMessage) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (isChangingConfigurations()) {
                    stateFragment.retainedErrorMessage = errorMessage;
                    return;
                }
                if (isFinishing()) return;

                ErrorDialogFragment errorFragment = ErrorDialogFragment.newInstance(errorMessage);
                errorFragment.setCancelable(false);
                errorFragment.show(getFragmentManager(), TAG_ERROR_FRAGMENT);
            }
        });
    }

    private void onErrorDialogFinish() {
        dismissDialogFragment(TAG_ERROR_FRAGMENT);
        finish();
    }

    private void onProgressDialogCancel() {
        dismissDialogFragment(TAG_PROGRESS_FRAGMENT);
        handler.onUserCancel();
        finish();
    }

    private void dismissDialogFragment(String dialogFragmentTag) {
        FragmentManager fm = getFragmentManager();
        DialogFragment dialogFragment = (DialogFragment)fm.findFragmentByTag(dialogFragmentTag);
        if (dialogFragment != null) dialogFragment.dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }

    public static class StateFragment extends Fragment {

        // These objects need to be retained across configuration changes
        PluginController pluginController;
        ResultHandler resultHandler;
        String retainedErrorMessage;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        public static ProgressDialogFragment newInstance(Bundle options) {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            fragment.setArguments(options);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Bundle options = getArguments();
            if (options == null) throw new RuntimeException("setArguments not called");

            final ControllerActivity activity = (ControllerActivity)getActivity();
            if (activity == null) throw new RuntimeException("getActivity returned null");

            ProgressDialog dialog = getDialog(activity, options);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    activity.onProgressDialogCancel();
                }
            });

            return dialog;
        }

        private ProgressDialog getDialog(Activity activity, Bundle options) {
            ProgressDialog dialog = new ProgressDialog(activity);
            dialog.setIndeterminate(options.getBoolean("indeterminate", true));
            dialog.setTitle(options.getString("title", "Result Handler"));
            dialog.setMessage(options.getString("message", "Loading..."));
            return dialog;
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {

        public static ErrorDialogFragment newInstance(String message) {
            ErrorDialogFragment fragment = new ErrorDialogFragment();
            Bundle args = new Bundle();
            args.putString("message", message);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final ControllerActivity activity = (ControllerActivity)getActivity();
            if (activity == null) throw new RuntimeException("getActivity returned null");

            Bundle options = getArguments();
            if (options == null) throw new RuntimeException("setArguments not called");

            return new AlertDialog.Builder(activity)
                    .setMessage(options.getString("message"))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            activity.onErrorDialogFinish();
                        }
                    })
                    .create();
        }
    }
}


















