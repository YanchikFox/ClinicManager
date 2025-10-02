package com.clinicmanager.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public interface ViewLoader {
    FXMLLoader loader(String fxmlPath);

    Parent load(String fxmlPath) throws IOException;
}
