package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.repository.Repositories;
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
    private final PanelManager panelManager;
    private final Repositories repositories;
    private final ViewLoader viewLoader;

    public DoctorPatientsController(PanelManager panelManager, Repositories repositories, ViewLoader viewLoader) {
        this.panelManager = panelManager;
        this.repositories = repositories;
        this.viewLoader = viewLoader;
    }

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
        var panel = panelManager.getCurrentPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        // Patients who have an ENDED appointment with this doctor
        List<Appointment> ended = repositories.appointments().findAll().stream()
            .filter(a -> a.doctorId() == doctor.id() && a.status().name().equals("ENDED"))
            .toList();
        treatedPatients = ended.stream()
            .map(a -> repositories.patients().findById(a.patientId()))
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
            javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/medical_card.fxml");
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
