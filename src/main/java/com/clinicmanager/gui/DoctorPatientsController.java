package com.clinicmanager.gui;

import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class DoctorPatientsController {
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> nameCol;
    @FXML private TableColumn<Patient, String> dobCol;
    @FXML private TableColumn<Patient, String> phoneCol;
    @FXML private Button viewCardBtn;
    @FXML private Button patientInfoBtn;
    @FXML private Button closeBtn;

    private List<Patient> treatedPatients;
    private Patient selectedPatient;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        setupTable();
        loadPatients();
        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedPatient = newSel;
            viewCardBtn.setDisable(selectedPatient == null);
            patientInfoBtn.setDisable(selectedPatient == null);
        });
        viewCardBtn.setOnAction(e -> handleViewCard());
        patientInfoBtn.setOnAction(e -> handlePatientInfo());
        viewCardBtn.setDisable(true);
        patientInfoBtn.setDisable(true);
    }

    private void setupTable() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        dobCol.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
    }

    private void loadPatients() {
        var panel = com.clinicmanager.gui.AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        // Patients who have an ENDED appointment with this doctor
        List<Appointment> ended = repos.appointments.findAll().stream()
            .filter(a -> a.doctorId() == doctor.id() && a.status().name().equals("ENDED"))
            .toList();
        treatedPatients = ended.stream()
            .map(a -> repos.patients.findById(a.patientId()))
            .filter(p -> p != null)
            .distinct()
            .toList();
        // Remove null values and duplicates and fall back to an empty list
        if (treatedPatients.isEmpty()) {
            patientsTable.setItems(FXCollections.observableArrayList());
        } else {
            patientsTable.setItems(FXCollections.observableArrayList(treatedPatients));
        }
    }

    private void handleViewCard() {
        if (selectedPatient == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/medical_card.fxml"));
            javafx.scene.Parent root = loader.load();
            com.clinicmanager.gui.MedicalCardController controller = loader.getController();
            controller.setPatient(selectedPatient);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Medical card: " + selectedPatient.name());
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handlePatientInfo() {
        if (selectedPatient == null) return;
        String msg = String.format("Name: %s\nDate of birth: %s\nPhone: %s",
                selectedPatient.name(), selectedPatient.dateOfBirth(), selectedPatient.phoneNumber());
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
