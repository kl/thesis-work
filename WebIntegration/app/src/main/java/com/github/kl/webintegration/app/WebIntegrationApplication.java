package com.github.kl.webintegration.app;

import android.app.Application;
import dagger.ObjectGraph;

public class WebIntegrationApplication extends Application implements Injector {

    public static final String LOG_TAG = WebIntegrationApplication.class.getCanonicalName();

    private ObjectGraph graph;

    @Override public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules());
    }

    protected Object[] getModules() {
        return new Object[] {
                new AppModule(this)
        };
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}
