package com.github.kl.webintegration.app.test;

import android.content.Context;
import android.content.Intent;

import com.github.kl.webintegration.app.ForApplication;
import com.github.kl.webintegration.app.Settings;
import com.github.kl.webintegration.app.handlers.HttpServerHandler;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class HttpServerHandlerTest extends TestCase {

    // SUT
    @Inject HttpServerHandler httpServerHandler;

    // These objects are injected with @Singleton. Therefore they are they same objects that
    // are injected in to the class under test, and they can be checked in the test methods.
    @Inject @ForApplication
    Context context;

    @Module(
        injects = HttpServerHandlerTest.class
    )
    static class TestModule {

        @Provides @Singleton @ForApplication
        Context provideApplicationContext() {
            return mock(Context.class);
        }

        @Provides @Singleton
        Settings provideSettings() {
            return mock(Settings.class);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestUtils.setDexCache();

        ObjectGraph.create(new TestModule()).inject(this);
    }

    public void testStartsServerServiceWhenOnResult() {
        httpServerHandler.handleResult(mock(JSONObject.class));

        ArgumentCaptor<Intent> intent = ArgumentCaptor.forClass(Intent.class);
        verify(context).startService(intent.capture());
        assertEquals(HttpServerHandler.ServerService.class.getName(),
                     intent.getValue().getComponent().getClassName());
    }

}






