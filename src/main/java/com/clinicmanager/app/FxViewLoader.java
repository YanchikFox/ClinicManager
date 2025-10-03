package com.clinicmanager.app;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

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
