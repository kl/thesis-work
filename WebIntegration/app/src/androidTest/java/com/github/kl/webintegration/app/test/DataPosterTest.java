package com.github.kl.webintegration.app.test;

import android.content.res.Resources;
import android.test.InstrumentationTestCase;

import com.github.kl.webintegration.app.DataPoster;
import com.github.kl.webintegration.app.R;

import junit.framework.TestCase;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

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

public class DataPosterTest extends TestCase {

    @Inject DataPoster dataPoster;

    // These objects are injected with @Singleton. Therefore they are they same objects that
    // are injected in to the class under test, and they can be checked in the test methods.
    @Inject Resources resources;
    @Inject HttpClient httpClient;

    @Module(
            injects = DataPosterTest.class,
            overrides = true
    )
    static class TestModule {

        @Provides @Singleton
        Resources provideResources() {
           return mock(Resources.class);
        }

        @Provides @Singleton
        HttpClient provideHttpClient() {
            return mock(HttpClient.class);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestHelper.setDexCache();

        ObjectGraph.create(new TestModule()).inject(this);

        dataPoster.post(getStubTestData());
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

    private Map<String, String> getStubTestData() {
        return new HashMap<>();
    }
}













