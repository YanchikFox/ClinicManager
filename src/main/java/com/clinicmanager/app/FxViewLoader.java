package com.clinicmanager.app;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import javafx.scene.Parent;

import java.io.IOException;

public class FxViewLoader implements ViewLoader {
    private final Callback<Class<?>, Object> controllerFactory;

    public FxViewLoader(Callback<Class<?>, Object> controllerFactory) {
        this.controllerFactory = controllerFactory;
    }

    @Override
    public FXMLLoader loader(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(controllerFactory);
        return loader;
    }

    @Override
    public Parent load(String fxmlPath) throws IOException {
        return loader(fxmlPath).load();
    }
}
