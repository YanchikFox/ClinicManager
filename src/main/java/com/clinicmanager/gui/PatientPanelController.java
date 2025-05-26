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
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_search.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Поиск врача");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        makeAppointmentBtn.setOnAction(e -> {
            System.out.println("📅 Запись к врачу выполнена");
        });

        viewAppointmentsBtn.setOnAction(e -> {
            System.out.println("📄 Мои приёмы");
        });

        viewMedicalCardBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/medical_card.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Медицинская карта");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        logoutBtn.setOnAction(e -> {
            System.out.println("🚪 Выход");
        });
    }
}
