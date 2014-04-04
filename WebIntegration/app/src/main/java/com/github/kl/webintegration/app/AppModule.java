package com.github.kl.webintegration.app;

import android.content.ContentResolver;
import android.content.Context;

import com.github.kl.webintegration.app.controllers.FilePickerPluginController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = ControllerActivity.class,
        complete = false,
        library = true
)
public class AppModule {

    private final WebIntegrationApplication application;

    public AppModule(WebIntegrationApplication application) {
        this.application = application;
    }

    @Provides @Singleton
    ContentResolver provideContentResolver() {
        return application.getContentResolver();
    }

    @Provides PluginControllerCollection providePluginControllerCollection() {

        return new PluginControllerCollection(
                new FilePickerPluginController()
        );
    }

    /**
     * Allow the application context to be injected but require that it be annotated with
     * {@link ForApplication @Annotation} to explicitly differentiate it from an activity context.
     */
    @Provides @Singleton @ForApplication
    Context provideApplicationContext() {
        return application;
    }
}
