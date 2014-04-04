package com.github.kl.webintegration.app;

import com.github.kl.webintegration.app.controllers.PluginController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PluginControllerCollection {

    private final Set<PluginController> controllerSet = new HashSet<>();

    public PluginControllerCollection(PluginController... controllers) {
        controllerSet.addAll(new HashSet<>(Arrays.asList(controllers)));
    }

    public PluginController getPluginController(String type) throws PluginControllerNotFoundException {
        for (PluginController controller : controllerSet) {
            if (controller.getType().equals(type)) return controller;
        }
        throw new PluginControllerNotFoundException(type);
    }

    public static class PluginControllerNotFoundException extends Exception {

        private String type;

        public PluginControllerNotFoundException(String type) {
            super("Could not find PluginController for type: " + type);
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}






