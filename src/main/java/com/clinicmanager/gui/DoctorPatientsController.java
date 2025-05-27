package com.clinicmanager.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DoctorPatientsController {
    @FXML private TableView<?> patientsTable;
    @FXML private TableColumn<?, ?> nameCol;
    @FXML private TableColumn<?, ?> dobCol;
    @FXML private TableColumn<?, ?> phoneCol;
    @FXML private Button viewCardBtn;
    @FXML private Button patientInfoBtn;
    @FXML private Button closeBtn;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        // TODO: implement logic for patients list and card viewing
    }
}

