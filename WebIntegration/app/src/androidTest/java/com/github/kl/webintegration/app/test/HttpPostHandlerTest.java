package com.github.kl.webintegration.app.test;

import android.content.res.Resources;

import com.github.kl.webintegration.app.handlers.HttpPostHandler;
import com.github.kl.webintegration.app.R;

import junit.framework.TestCase;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpPostHandlerTest extends TestCase {

    @Inject
    HttpPostHandler httpPostHandler;

    // These objects are injected with @Singleton. Therefore they are they same objects that
    // are injected in to the class under test, and they can be checked in the test methods.
    @Inject Resources resources;
    @Inject HttpClient httpClient;

    @Module(
            injects = HttpPostHandlerTest.class,
            overrides = true
    )
    static class TestModule {

        @Provides @Singleton
        Resources provideResources() {
           return mock(Resources.class);
        }

        @Provides @Singleton
        HttpClient provideHttpClient() {

            HttpClient mockClient = mock(HttpClient.class);
            HttpResponse mockResponse = mock(HttpResponse.class);

            try {
                when(mockResponse.getEntity()).thenReturn(mock(HttpEntity.class));
                when(mockClient.execute(any(HttpPost.class))).thenReturn(mockResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return mockClient;
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestHelper.setDexCache();

        ObjectGraph.create(new TestModule()).inject(this);

        httpPostHandler.handleResult(new JSONObject());
        Thread.sleep(500);  // Sleep because post() starts a thread. Is there a better solution?
    }

    public void testReadsServerParametersFromXml() throws InterruptedException {
        verify(resources).getString(R.string.SERVER_IP);
        verify(resources).getString(R.string.SERVER_POST);
        verify(resources).getInteger(R.integer.SERVER_PORT);
    }

    public void testExecutesHttpPostRequest() throws IOException {
        verify(httpClient).execute(any(HttpPost.class));
    }

    public void testSetsContentTypeToJson() throws IOException {
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(httpPostCaptor.capture());
        assertEquals("application/json", httpPostCaptor.getValue().getEntity().getContentType().getValue());
    }
}













