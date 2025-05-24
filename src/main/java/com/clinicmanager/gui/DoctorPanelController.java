package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DoctorPanelController {
    private boolean scheduleOpened = false;
    private boolean medicalCardOpened = false;
    private boolean patientHasAppointment = false;

    @FXML private Button viewScheduleBtn;
    @FXML private Button editScheduleBtn;
    @FXML private Button viewAppointmentsBtn;
    @FXML private Button viewMedicalCardBtn;
    @FXML private Button addRecordBtn;
    @FXML private Button logoutBtn;

    @FXML
    private void initialize() {
        editScheduleBtn.setDisable(true);
        addRecordBtn.setDisable(true);

        viewScheduleBtn.setOnAction(e -> {
            System.out.println("✅ Открыто расписание");
            scheduleOpened = true;
            editScheduleBtn.setDisable(false);
        });

        editScheduleBtn.setOnAction(e -> {
            System.out.println("✏️ Редактируем расписание...");
        });

        viewAppointmentsBtn.setOnAction(e -> {
            System.out.println("📅 Просмотр приёмов");
        });

        viewMedicalCardBtn.setOnAction(e -> {
            System.out.println("📖 Медкарта пациента открыта");
            medicalCardOpened = true;
            patientHasAppointment = true; // допустим, есть приём
            if (patientHasAppointment) {
                addRecordBtn.setDisable(false);
            }
        });

        addRecordBtn.setOnAction(e -> {
            System.out.println("➕ Добавлена запись в медкарту");
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("🚪 Выход");
        });
    }
}
