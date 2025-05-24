package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartMenuController {

    @FXML
    private void handleLogin() {
        switchScene("/gui/login.fxml");
    }

    @FXML
    private void handleRegister() {
        switchScene("/gui/register.fxml");
    }

    private void switchScene(String fxml) {
        try {
            Stage stage = MainFX.getPrimaryStage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
