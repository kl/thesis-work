package com.github.kl.webintegration.app;

import com.github.kl.webintegration.app.handlers.HttpServerHandler.ServerService;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.github.kl.webintegration.app.controllers.AllScanController;
import com.github.kl.webintegration.app.controllers.BarcodeScanController;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.controllers.ProductScanController;
import com.github.kl.webintegration.app.controllers.QRScanController;
import com.github.kl.webintegration.app.handlers.HttpPostHandler;
import com.github.kl.webintegration.app.handlers.HttpServerHandler;
import com.github.kl.webintegration.app.handlers.HttpsPostHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {ControllerActivity.class, ServerService.class},
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
    Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton @ForApplication
    SharedPreferences provideApplicationSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides @Singleton @Named("pluginControllers")
    Set<PluginController> providePluginControllers(
            BarcodeScanController bsc,
            QRScanController qsc,
            AllScanController asc,
            ProductScanController psc
    )
    {
        Set<PluginController> controllers = new HashSet<>();
        controllers.addAll(Lists.newArrayList(bsc, qsc, asc, psc));
        return controllers;
    }

    @Provides @Singleton @Named("resultHandlers")
    Set<ResultHandler> provideResultHandlers(
            HttpPostHandler hph,
            HttpsPostHandler hsph,
            HttpServerHandler hsh
    )
    {
        Set<ResultHandler> handlers = new HashSet<>();
        handlers.addAll(Lists.newArrayList(hph, hsph, hsh));
        return handlers;
    }

    @Provides @Singleton
    ContentResolver provideContentResolver() { return application.getContentResolver(); }

    @Provides @Singleton
    Resources provideResources() { return application.getResources(); }

    @Provides @Singleton
    HttpServerHandler.Server provideServer(Settings settings) {
        return new HttpServerHandler.Server(settings.getLocalServerPort());
    }
}





