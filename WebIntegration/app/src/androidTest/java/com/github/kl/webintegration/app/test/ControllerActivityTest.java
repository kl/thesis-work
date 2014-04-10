package com.github.kl.webintegration.app.test;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.mock.MockApplication;

import com.github.kl.webintegration.app.ControllerActivity;
import com.github.kl.webintegration.app.handlers.HttpPostHandler;
import com.github.kl.webintegration.app.Injector;
import com.github.kl.webintegration.app.PostProgressDialogHandler;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.github.kl.webintegration.app.ControllerActivity.*;

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

            Set<ResultHandler> handlers = new HashSet<>();
            handlers.add(mockHandler);
            return handlers;
        }

        @Provides @Singleton
        PostProgressDialogHandler providePostProgressDialogHandler() {
            return mock(PostProgressDialogHandler.class);
        }
    }

    static class MockInjectorApplication extends MockApplication implements Injector {

        private ObjectGraph graph = ObjectGraph.create(new TestModule());

        @Override public void inject(Object object) {
            graph.inject(object);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestHelper.setDexCache();

        MockInjectorApplication app = new MockInjectorApplication();
        app.inject(this);
        setApplication(app);
    }

    public void testCallsPluginControllerWithIntentAuthorityString() throws Exception {
        Intent intent = getIntentWithUrlPath("/" + VALID_PLUGIN_NAME + "/" + VALID_HANDLER_NAME);
        startActivity(intent, null, null);

        PluginController selectedController = controllers.iterator().next();

        verify(selectedController).getPluginIntent();
        verify(selectedController).getRequestCode();
    }

    // TODO: fix these tests

    /*
    public void testFindsPluginWithFirstPathSegmentOfUrl() {
        Intent intent = getIntentWithUrlPath("/" + VALID_PLUGIN_NAME);
        startActivity(intent, null, null);

        verify(poster).post(map(POST_NOT_FOUND_KEY, POST_NOT_FOUND_VALUE));
    }

    public void testPostsCancelMessageWhenOnCancelCalled() {
        startActivity(getIntentWithAuthority(VALID_PLUGIN_NAME), null, null);

        String testPluginType = "test_type";
        PluginController mockController = mock(PluginController.class);
        when(mockController.getType()).thenReturn(testPluginType);

        getActivity().onPluginCancel(mockController);

        verify(poster).post(map(POST_CANCEL_KEY, POST_CANCEL_VALUE,
                                POST_CANCEL_TYPE_KEY, testPluginType));
    }

    public void testPostsPluginDataWhenOnPluginResultIsCalled() {
        startActivity(getIntentWithAuthority(VALID_PLUGIN_NAME), null, null);

        Map<String, String> pluginResult = map("test_key", "test_value");
        getActivity().onPluginResult(pluginResult, mock(PluginController.class));

        verify(poster).post(pluginResult);
    }
    */

    private Intent getIntentWithUrlPath(String path) {
        Intent intent = new Intent(getInstrumentation().getTargetContext(), ControllerActivity.class);
        Uri uri = new Uri.Builder().path(path).build();
        intent.setData(uri);
        return intent;
    }

    @SafeVarargs
    private final <T> Map<T, T> map(T... input) {
        Preconditions.checkArgument((input.length % 2) == 0, "Input must be of even length");
        Map<T, T> map = new HashMap<>();
        for (int i = 0; i < input.length; i += 2) {
           map.put(input[i], input[i+1]);
        }
        return map;
    }
}










