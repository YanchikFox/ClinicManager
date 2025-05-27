package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PatientPanelController {

    @FXML private Button searchDoctorsBtn;
    @FXML private Button viewAppointmentsBtn;
    @FXML private Button viewMedicalCardBtn;
    @FXML private Button logoutBtn;
    @FXML private Button notificationsBtn;
    @FXML private Button favoriteDoctorsBtn;

    @FXML
    private void initialize() {
        searchDoctorsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_search.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Wyszukiwanie lekarza");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        viewAppointmentsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/patient_appointments.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Moje wizyty");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        viewMedicalCardBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/medical_card.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Karta medyczna");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        logoutBtn.setOnAction(e -> {
            try {
                // revoke токен при выходе
                var panel = com.clinicmanager.gui.AppContext.getPanel();
                if (panel != null) {
                    panel.revokeToken();
                }
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/start_menu.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Уведомления
        javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
        notificationsBtn.setOnAction(e -> {
            var panel = com.clinicmanager.gui.AppContext.getPanel();
            if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) return;
            var notificationManager = com.clinicmanager.gui.AppContext.getRepositories().notifications;
            var service = new com.clinicmanager.service.NotificationManager(notificationManager);
            // force reload notifications from DB each time
            NotificationWindow.showNotifications((javafx.stage.Stage) notificationsBtn.getScene().getWindow(), service, patient.id());
            // update style after closing window
            javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
        });
        // add listener to update style when window regains focus
        notificationsBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((of, was, isNow) -> {
                            if (isNow) javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
                        });
                    }
                });
            }
        });

        favoriteDoctorsBtn.setOnAction(e -> {
            try {
                var panel = com.clinicmanager.gui.AppContext.getPanel();
                if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) return;
                var repos = com.clinicmanager.gui.AppContext.getRepositories();
                var favs = repos.favoriteDoctors.findByPatientId(patient.id());
                java.util.List<com.clinicmanager.model.actors.Doctor> doctors = favs.stream()
                    .map(fav -> repos.doctors.findById(fav.doctorId()))
                    .filter(d -> d != null)
                    .toList();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_search.fxml"));
                javafx.scene.Parent root = loader.load();
                com.clinicmanager.gui.DoctorSearchController controller = loader.getController();
                controller.setDoctors(doctors);
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Ulubieni lekarze");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateNotificationsButtonStyle() {
        var panel = com.clinicmanager.gui.AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) return;
        var notificationManager = com.clinicmanager.gui.AppContext.getRepositories().notifications;
        var service = new com.clinicmanager.service.NotificationManager(notificationManager);
        // always reload from DB
        boolean hasUnread = !service.getUnreadNotificationsByPersonId(patient.id()).isEmpty();
        notificationsBtn.setStyle(hasUnread
            ? "-fx-background-color: #ffcccc; -fx-border-color: red; -fx-border-width: 2px;"
            : "-fx-background-color: #cccccc;");
    }
}
