package com.github.kl.webintegration.app.test;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityUnitTestCase;
import android.test.mock.MockApplication;

import com.github.kl.webintegration.app.ControllerActivity;
import com.github.kl.webintegration.app.DataPoster;
import com.github.kl.webintegration.app.Injector;
import com.github.kl.webintegration.app.PluginControllerCollection;
import com.github.kl.webintegration.app.PluginControllerCollection.PluginControllerNotFoundException;
import com.github.kl.webintegration.app.PostProgressDialogHandler;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
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

    public ControllerActivityTest() {
        super(ControllerActivity.class);
    }

    // These objects are injected with @Singleton. Therefore they are they same objects that
    // are injected in to the class under test, and they can be checked in the test methods.
    @Inject DataPoster poster;
    @Inject PluginControllerCollection controllers;

    @Module(
            injects = {ControllerActivity.class, ControllerActivityTest.class},
            overrides = true
    )
    static class TestModule {

        @Provides @Singleton
        DataPoster provideDataPoster() { return mock(DataPoster.class); }

        @Provides @Singleton
        PluginControllerCollection providePluginControllerCollection() {
            PluginControllerCollection c = mock(PluginControllerCollection.class);

            try {
                when(c.getPluginController(INVALID_PLUGIN_NAME)).thenThrow(new PluginControllerNotFoundException(INVALID_PLUGIN_NAME));
                when(c.getPluginController(VALID_PLUGIN_NAME)).thenReturn(mock(PluginController.class));
            } catch (PluginControllerNotFoundException e) { e.printStackTrace(); } // dafuq we need this??

            return c;
        }

        @Provides @Singleton
        PostProgressDialogHandler providePostProgressDialogHanlder() {
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
        startActivity(getIntentWithAuthority(VALID_PLUGIN_NAME), null, null);

        verify(controllers).getPluginController(VALID_PLUGIN_NAME);
    }

    public void testPostsPluginNotFoundMessageWhenPluginIsUnknown() {
        startActivity(getIntentWithAuthority(INVALID_PLUGIN_NAME), null, null);

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

    private Intent getIntentWithAuthority(String authorityString) {
        Intent intent = new Intent(getInstrumentation().getTargetContext(), ControllerActivity.class);
        Uri uri = new Uri.Builder().authority(authorityString).build();
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










