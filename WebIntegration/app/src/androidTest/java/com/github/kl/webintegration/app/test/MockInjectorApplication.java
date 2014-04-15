package com.github.kl.webintegration.app.test;

import android.test.mock.MockApplication;

import com.github.kl.webintegration.app.Injector;

import dagger.ObjectGraph;

class MockInjectorApplication extends MockApplication implements Injector {

    private ObjectGraph graph;

    MockInjectorApplication(Object... modules) {
        graph = ObjectGraph.create(modules);
    }

    @Override public void inject(Object object) {
        graph.inject(object);
    }
}
