package com.clinicmanager.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Remove future free slots when the application starts to eliminate
        // scheduling inconsistencies
        var slotRepo = com.clinicmanager.gui.AppContext.getRepositories().slots;
        var appointmentRepo = com.clinicmanager.gui.AppContext.getRepositories().appointments;
        java.time.LocalDateTime now = com.clinicmanager.time.TimeManager.getInstance().getCurrentTime();
        slotRepo.findAll().forEach(slot -> {
            java.time.LocalDateTime slotStart = java.time.LocalDateTime.of(slot.date(), slot.timeRange().start());
            boolean isFuture = slotStart.isAfter(now);
            boolean hasActiveAppointment = appointmentRepo.findAll().stream()
                    .anyMatch(a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
            if (isFuture && !hasActiveAppointment) {
                slotRepo.delete(slot);
            }
        });

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
