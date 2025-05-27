package com.clinicmanager.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/start_menu.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage = stage;
        stage.setTitle("Clinic Manager");
        stage.setScene(scene);
        stage.show();

    }
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    public static void main(String[] args) {
        launch(args);
    }
}