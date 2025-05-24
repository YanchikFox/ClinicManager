package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PatientPanelController {
    private boolean doctorSelected = false;

    @FXML private Button searchDoctorsBtn;
    @FXML private Button makeAppointmentBtn;
    @FXML private Button viewAppointmentsBtn;
    @FXML private Button viewMedicalCardBtn;
    @FXML private Button logoutBtn;

    @FXML
    private void initialize() {
        makeAppointmentBtn.setDisable(true);

        searchDoctorsBtn.setOnAction(e -> {
            System.out.println("🔎 Найден врач: Dr. House");
            doctorSelected = true;
            makeAppointmentBtn.setDisable(false);
        });

        makeAppointmentBtn.setOnAction(e -> {
            System.out.println("📅 Запись к врачу выполнена");
        });

        viewAppointmentsBtn.setOnAction(e -> {
            System.out.println("📄 Мои приёмы");
        });

        viewMedicalCardBtn.setOnAction(e -> {
            System.out.println("📖 Медкарта открыта");
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("🚪 Выход");
        });
    }
}
