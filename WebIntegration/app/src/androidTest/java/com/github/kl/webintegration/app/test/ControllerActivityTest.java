package com.github.kl.webintegration.app.test;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.test.ActivityUnitTestCase;

import com.github.kl.webintegration.app.AppModule;
import com.github.kl.webintegration.app.ControllerActivity;
import com.github.kl.webintegration.app.Settings;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.controllers.SystemPluginController;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test class for ControllerActivity.
 */
public class ControllerActivityTest extends ActivityUnitTestCase<ControllerActivity> {

    public static final String VALID_PLUGIN_NAME = "SCANNER_BARCODE";
    public static final String INVALID_PLUGIN_NAME = "UNKNOWN";
    public static final String VALID_HANDLER_NAME = "HTTP_POST";

    public ControllerActivityTest() {
        super(ControllerActivity.class);
    }

    // These objects are injected with @Singleton. Therefore they are they same objects that
    // are injected in to the class under test, and they can be checked in the test methods.
    @Inject @Named("pluginControllers") Set<PluginController> controllers;
    @Inject @Named("resultHandlers") Set<ResultHandler> handlers;
    @Inject Settings settings;
    @Inject SystemPluginController systemPluginController;

    @Module(
        injects = {ControllerActivity.class, ControllerActivityTest.class}
    )
    static class TestModule {

        @Provides @Singleton @Named("pluginControllers")
        Set<PluginController> providePluginControllers() {

            PluginController mockController = mock(PluginController.class);
            when(mockController.getType()).thenReturn(VALID_PLUGIN_NAME);

            Set<PluginController> controllers = new HashSet<>();
            controllers.add(mockController);
            return controllers;
        }

        @Provides @Singleton @Named("resultHandlers")
        Set<ResultHandler> provideResultHandlers() {

            ResultHandler mockHandler = mock(ResultHandler.class);
            when(mockHandler.getType()).thenReturn(VALID_HANDLER_NAME);
            when(mockHandler.isUsingProgressDialog()).thenReturn(true);

            Set<ResultHandler> handlers = new HashSet<>();
            handlers.add(mockHandler);
            return handlers;
        }

        @Provides @Singleton
        Settings provideSettings() {
            return mock(Settings.class);
        }

        @Provides @Singleton
        SystemPluginController provideSystemPluginController() {
            return mock(SystemPluginController.class);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestUtils.setDexCache();

        MockInjectorApplication app = new MockInjectorApplication(new TestModule());
        app.inject(this);
        setApplication(app);
    }

    public void testFindsPluginWithFirstPathSegmentOfUrl() throws Exception {
        startActivityWithValidPlugin();

        PluginController selectedController = getControllerWithType(VALID_PLUGIN_NAME);
        verify(selectedController).getPluginIntent();
        verify(selectedController).getRequestCode();
    }

    public void testCallsHandlePluginNotFoundWithUnknownPlugin() {
        startActivityWithIntentUrlPath("/" + INVALID_PLUGIN_NAME + "/" + VALID_HANDLER_NAME);

        ResultHandler selectedHandler = getHandlerWithType(VALID_HANDLER_NAME);
        verify(selectedHandler).handlePluginNotFound(INVALID_PLUGIN_NAME);
    }

    public void testCallsHandleResultWhenPluginIsFound() {
        startActivityWithValidPlugin();

        ControllerActivity activity = getActivity();
        JSONObject testResult = jsonmap("data", "test");
        activity.onPluginResult(testResult, mock(PluginController.class)); // the second argument does not matter

        ResultHandler selectedHandler = getHandlerWithType(VALID_HANDLER_NAME);
        verify(selectedHandler).handleResult(testResult);
    }

    public void testCallsHandleCancelWhenOnCancelIsCalled() {
        startActivityWithValidPlugin();

        ControllerActivity activity = getActivity();
        activity.onPluginCancel(getControllerWithType(VALID_PLUGIN_NAME));

        ResultHandler selectedHandler = getHandlerWithType(VALID_HANDLER_NAME);
        verify(selectedHandler).handleCancel(VALID_PLUGIN_NAME);
    }

    public void testCallsOnCustomizePlugin() {
        startActivityWithValidPlugin();

        ControllerActivity activity = getActivity();
        activity.onPluginResult(jsonmap("data", "test"), mock(PluginController.class)); // the second argument does not matter

        ResultHandler selectedHandler = getHandlerWithType(VALID_HANDLER_NAME);
        verify(selectedHandler).onCustomizeProgressDialog(any(Bundle.class));
    }

    private void startActivityWithValidPlugin() {
        startActivityWithIntentUrlPath("/" + VALID_PLUGIN_NAME + "/" + VALID_HANDLER_NAME);
    }

    private void startActivityWithIntentUrlPath(String path) {
        Intent intent = getIntentWithUrlPath(path);
        startActivity(intent, null, null);
    }

    private Intent getIntentWithUrlPath(String path) {
        Intent intent = new Intent(getInstrumentation().getTargetContext(), ControllerActivity.class);
        Uri uri = new Uri.Builder().path(path).build();
        intent.setData(uri);
        return intent;
    }

    private PluginController getControllerWithType(String type) {
        for (PluginController controller : controllers) {
           if (controller.getType().equals(type)) return controller;
        }
        return null;
    }

    private ResultHandler getHandlerWithType(String type) {
        for (ResultHandler handler : handlers) {
            if (handler.getType().equals(type)) return handler;
        }
        return null;
    }

    private JSONObject jsonmap(String... input)  {
        Preconditions.checkArgument((input.length % 2) == 0, "Input must be of even length");
        JSONObject jso = new JSONObject();
        try {
            for (int i = 0; i < input.length; i += 2) {
                jso.put(input[i], input[i + 1]);
            }
        } catch (JSONException e) { throw new RuntimeException(e); }
        return jso;
    }
}
