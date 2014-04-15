package com.github.kl.webintegration.app.test;

import android.content.Intent;
import android.test.ServiceTestCase;

import com.github.kl.webintegration.app.handlers.HttpServerHandler.Server;
import com.github.kl.webintegration.app.handlers.HttpServerHandler.ServerService;

import org.json.JSONObject;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ServerServiceTest extends ServiceTestCase<ServerService> {

    @Inject Server server;

    @Module(injects = {ServerService.class, ServerServiceTest.class})
    public class TestModule {

        @Provides @Singleton
        public Server provideServer() {
            return mock(Server.class);
        }
    }

    public ServerServiceTest() {
        super(ServerService.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestHelper.setDexCache();

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

    private void startServerService() {
        Intent intent = new Intent(getContext(), ServerService.class);
        intent.putExtra("JSON", getTestJson());
        startService(intent);
    }

    private String getTestJson() {
        return "{\"data\":\"test\"}";
    }
}



