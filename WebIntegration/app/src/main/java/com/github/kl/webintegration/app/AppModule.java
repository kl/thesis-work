package com.github.kl.webintegration.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;

import com.github.kl.webintegration.app.controllers.BarcodeController;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = ControllerActivity.class,
        library = true
)
public class AppModule {

    private final WebIntegrationApplication application;

    public AppModule(WebIntegrationApplication application) {
        this.application = application;
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides @Singleton @ForApplication
    Context provideApplicationContext() { return application; }

    @Provides @Singleton PluginControllerCollection providePluginControllerCollection(
            BarcodeController c1
    )
    {
        return new PluginControllerCollection(c1);
    }

    @Provides @Singleton
    ContentResolver provideContentResolver() { return application.getContentResolver(); }

    @Provides @Singleton
    Resources provideResources() { return application.getResources(); }

    @Provides
    HttpClient provideHttpClient() { return new DefaultHttpClient(); }
}
