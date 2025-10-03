package com.clinicmanager.app;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public interface ViewLoader {
  FXMLLoader loader(String fxmlPath);

  Parent load(String fxmlPath) throws IOException;
}
