package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.repository.Repositories;
import java.util.List;
import java.util.Objects;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorPatientsController {
  private static final Logger LOG = LoggerFactory.getLogger(DoctorPatientsController.class);

  @FXML private TableView<Patient> patientsTable;
  @FXML private TableColumn<Patient, String> nameCol;
  @FXML private TableColumn<Patient, String> dobCol;
  @FXML private TableColumn<Patient, String> phoneCol;
  @FXML private Button viewCardBtn;
  @FXML private Button patientInfoBtn;
  @FXML private Button closeBtn;

  private Patient selectedPatient;
  private final PanelManager panelManager;
  private final Repositories repositories;
  private final ViewLoader viewLoader;
  private final LocalizationManager localization = LocalizationManager.getInstance();

  public DoctorPatientsController(
      PanelManager panelManager, Repositories repositories, ViewLoader viewLoader) {
    this.panelManager = panelManager;
    this.repositories = repositories;
    this.viewLoader = viewLoader;
  }

  @FXML
  private void initialize() {
    closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
    setupTable();
    loadPatients();
    patientsTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSel, newSel) -> {
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
    if (!(panel instanceof DoctorControlPanel doctorPanel)) {
      patientsTable.setItems(FXCollections.observableArrayList());
      return;
    }
    Doctor doctor = (Doctor) doctorPanel.currentPerson();
    List<Appointment> finishedAppointments =
        repositories.appointments().findAll().stream()
            .filter(a -> a.doctorId() == doctor.id() && a.status().name().equals("ENDED"))
            .toList();
    List<Patient> patients =
        finishedAppointments.stream()
            .map(a -> repositories.patients().findById(a.patientId()))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    patientsTable.setItems(FXCollections.observableArrayList(patients));
  }

  private void handleViewCard() {
    if (selectedPatient == null) {
      return;
    }
    try {
      var loader = FxViewHelper.load(viewLoader, "/gui/medical_card.fxml");
      MedicalCardController controller = loader.getController();
      controller.setPatient(selectedPatient);
      FxViewHelper.showInNewStage(loader, "Medical card: " + selectedPatient.name());
    } catch (IllegalStateException ex) {
      LOG.error("Failed to open medical card for patient {}", selectedPatient.id(), ex);
      showError(localization.get("error.viewLoad"));
    }
  }

  private void handlePatientInfo() {
    if (selectedPatient == null) return;
    String msg =
        String.format(
            "Name: %s\nDate of birth: %s\nPhone: %s",
            selectedPatient.name(), selectedPatient.dateOfBirth(), selectedPatient.phoneNumber());
    new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
  }

  private void showError(String message) {
    new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
  }
}
