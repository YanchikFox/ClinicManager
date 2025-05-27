package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DoctorPanelController {
    private boolean scheduleOpened = false;
    private boolean medicalCardOpened = false;
    private boolean patientHasAppointment = false;

    @FXML
    private Button viewScheduleBtn;
    @FXML
    private Button editScheduleBtn;
    @FXML
    private Button viewAppointmentsBtn;
    @FXML
    private Button viewMedicalCardBtn;
    @FXML
    private Button addRecordBtn;
    @FXML
    private Button logoutBtn;

    @FXML
    private void initialize() {
        editScheduleBtn.setDisable(true);
        addRecordBtn.setDisable(true);

        viewScheduleBtn.setOnAction(e -> {
            System.out.println("✅ Schedule opened");
            scheduleOpened = true;
            editScheduleBtn.setDisable(false);
        });

        editScheduleBtn.setOnAction(e -> {
            System.out.println("✏️ Editing schedule...");
        });

        viewAppointmentsBtn.setOnAction(e -> {
            System.out.println("📅 Viewing appointments");
        });

        viewMedicalCardBtn.setOnAction(e -> {
            System.out.println("📖 Patient medical card opened");
            medicalCardOpened = true;
            patientHasAppointment = true; // przykładowo: pacjent ma wizytę
            if (patientHasAppointment) {
                addRecordBtn.setDisable(false);
            }
        });

        addRecordBtn.setOnAction(e -> {
            System.out.println("➕ Record added to medical card");
        });

        logoutBtn.setOnAction(e -> {
            try {
                // Wylogowanie i cofnięcie tokena
                var panel = AppContext.getPanel();
                if (panel != null) {
                    panel.revokeToken();
                }
                var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/start_menu.fxml"));
                var scene = new javafx.scene.Scene(loader.load());
                var stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}
