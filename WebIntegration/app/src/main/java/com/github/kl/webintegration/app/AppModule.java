package com.github.kl.webintegration.app;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.github.kl.webintegration.app.controllers.AllScanController;
import com.github.kl.webintegration.app.controllers.BarcodeScanController;
import com.github.kl.webintegration.app.controllers.PluginController;
import com.github.kl.webintegration.app.controllers.ProductScanController;
import com.github.kl.webintegration.app.controllers.QRScanController;
import com.github.kl.webintegration.app.controllers.SpeedTestController;
import com.github.kl.webintegration.app.handlers.HttpPostHandler;
import com.github.kl.webintegration.app.handlers.HttpServerHandler;
import com.github.kl.webintegration.app.handlers.HttpServerHandler.ServerService;
import com.github.kl.webintegration.app.handlers.HttpsPostHandler;
import com.github.kl.webintegration.app.handlers.ResultHandler;
import com.google.common.collect.Lists;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/*
WARNING: Be careful when using the @Singleton annotation. @Singleton makes Dagger only create a
single instance of the provided object that is then cached for subsequent injections.
This caching happens once for the APPLICATION lifetime, not once per activity. In some cases
multiple copies of an activity is created (see http://developer.android.com/guide/components/tasks-and-back-stack.html).
This can happen when the ControllerActivity is started by the launcher in order to display
the preference fragment, and then later started again when a plugin link is clicked in the browser.
If the application process is still running (cached) when the plugin link is clicked, the new
ControllerActivity will be added to the application process, and the injection process will happen
again for this new activity. It's important to make sure that any object that is injected with the
@Singleton annotation still functions in this case. This is the case for immutable objects or
application objects such as the application context, but it is NOT the case for, for example, the
HttpHandler.Server object if the port number is passed in to the constructor (which is currently the case).
In this case if @Singleton is used, a newly created activity could receive an old singleton Server object
that was constructed with an outdated port number that has since been changed in the preferences.
*/
@Module(
        injects = {ControllerActivity.class, ServerService.class},
        library = true
)
public class AppModule {

    private final Application application;

    public AppModule(Application application) {
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

    @Provides @Named("pluginControllers")
    Set<PluginController> providePluginControllers(
            BarcodeScanController bsc,
            QRScanController qsc,
            AllScanController asc,
            ProductScanController psc,
            SpeedTestController stc
    )
    {
        Set<PluginController> controllers = new HashSet<>();
        controllers.addAll(Lists.newArrayList(bsc, qsc, asc, psc, stc));
        return controllers;
    }

    @Provides @Named("resultHandlers")
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

    @Provides
    HttpServerHandler.Server provideServer(Settings settings) {
        return new HttpServerHandler.Server(settings.getLocalServerPort());
    }

    @Provides
    Timer provideTimer() { return new Timer(); }

    @Provides @Named("uiThreadHandler")
    Handler provideUiThreadHandler() { return new Handler(Looper.getMainLooper()); }
}





