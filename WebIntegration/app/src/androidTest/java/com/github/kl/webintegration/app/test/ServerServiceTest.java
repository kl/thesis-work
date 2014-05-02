package com.github.kl.webintegration.app.test;

import android.content.Intent;
import android.os.Handler;
import android.test.ServiceTestCase;

import com.github.kl.webintegration.app.handlers.HttpServerHandler.Server;
import com.github.kl.webintegration.app.handlers.HttpServerHandler.ServerService;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Timer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ServerServiceTest extends ServiceTestCase<ServerService> {

    private static final int SERVER_TIMEOUT_MS = 100;

    @Inject Server server;
    @Inject @Named("uiThreadHandler") Handler handler;

    @Module(
        injects = {ServerServiceTest.class, ServerService.class}
    )
    public class TestModule {

        @Provides @Singleton
        Server provideServer() {
            return mock(Server.class);
        }

        @Provides @Singleton @Named("uiThreadHandler")
        Handler provideHandler() {
            return mock(Handler.class);
        }

        @Provides @Singleton
        Timer provideTimer() {
            return new Timer();
        }
    }

    public ServerServiceTest() {
        super(ServerService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestUtils.setDexCache();

        MockInjectorApplication app = new MockInjectorApplication(new TestModule());
        app.inject(this);
        setApplication(app);
    }

    public void testStartsServerWhenStartServiceIsCalled() throws IOException {
        startServerService();
        verify(server).start();
    }

    public void testSetsServerResultDataToIntentJson() throws IOException {
        startServerService();

        ArgumentCaptor<JSONObject> jso = ArgumentCaptor.forClass(JSONObject.class);
        verify(server).setResponseResult(jso.capture());
        assertEquals(getTestJson(), jso.getValue().toString());
    }

    public void testStopsServerAfterTimeoutExpires() throws Exception {
        startServerService();
        Thread.sleep(SERVER_TIMEOUT_MS + 50);
        verify(server).stop();
    }

    private void startServerService() {
        Intent intent = new Intent(getContext(), ServerService.class);
        intent.putExtra("json", getTestJson());
        intent.putExtra("timeout", SERVER_TIMEOUT_MS);
        startService(intent);
    }

    private String getTestJson() {
        return "{\"data\":\"test\"}";
    }
}



