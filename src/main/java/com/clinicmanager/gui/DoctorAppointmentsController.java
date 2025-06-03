package com.clinicmanager.gui;

import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import com.clinicmanager.gui.AppContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorAppointmentsController {
    @FXML
    private TableView<AppointmentTableRow> appointmentsTable;
    @FXML
    private TableColumn<AppointmentTableRow, String> patientCol;
    @FXML
    private TableColumn<AppointmentTableRow, String> dateCol;
    @FXML
    private TableColumn<AppointmentTableRow, String> timeCol;
    @FXML
    private TableColumn<AppointmentTableRow, String> statusCol;
    @FXML
    private Button viewDetailsBtn;
    @FXML
    private Button openCardBtn;
    @FXML
    private Button patientInfoBtn;
    @FXML
    private Button addRecordBtn;
    @FXML
    private Button endAppointmentBtn;
    @FXML
    private Button closeBtn;
    @FXML
    private CheckBox showActiveOnlyCheckBox;

    private List<Appointment> myAppointments;
    private Appointment selectedAppointment;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        setupTable();
        loadAppointments();
        // Обработчики кнопок
        viewDetailsBtn.setOnAction(e -> handleViewDetails());
        openCardBtn.setOnAction(e -> handleOpenCard());
        patientInfoBtn.setOnAction(e -> handlePatientInfo());
        addRecordBtn.setOnAction(e -> handleAddRecord());
        endAppointmentBtn.setOnAction(e -> handleEndAppointment());
        // Слушатель выбора строки
        appointmentsTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx != null && idx.intValue() >= 0 && idx.intValue() < myAppointments.size()) {
                selectedAppointment = myAppointments.get(idx.intValue());
                setButtonsEnabled(true);
            } else {
                selectedAppointment = null;
                setButtonsEnabled(false);
            }
        });
        setButtonsEnabled(false);

        // Добавляем чекбокс для фильтрации только когда сцена уже установлена
        appointmentsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                if (showActiveOnlyCheckBox == null) {
                    showActiveOnlyCheckBox = new CheckBox("Pokaż tylko aktywne wizyty");
                    showActiveOnlyCheckBox.setSelected(true);
                    BorderPane root = (BorderPane) newScene.getRoot();
                    HBox hbox = (HBox) root.getBottom();
                    hbox.getChildren().add(0, showActiveOnlyCheckBox);
                    showActiveOnlyCheckBox.selectedProperty().addListener((o, ov, nv) -> loadAppointments());
                }
            }
        });

        // --- Сохраняем контроллер в свойствах root для глобального обновления ---
        javafx.application.Platform.runLater(() -> {
            if (appointmentsTable.getScene() != null && appointmentsTable.getScene().getRoot() != null) {
                appointmentsTable.getScene().getRoot().getProperties().put("fx:controller", this);
            }
        });
    }

    // --- Публичный метод для обновления списка визитов ---
    public void reloadAppointments() {
        loadAppointments();
    }

    private void setButtonsEnabled(boolean enabled) {
        boolean canEnd = enabled && selectedAppointment != null &&
                !(selectedAppointment.status().name().equals("ENDED") || selectedAppointment.status().name().equals("CANCELLED"));
        boolean canAddRecord = canEnd;
        viewDetailsBtn.setDisable(!enabled);
        openCardBtn.setDisable(!enabled);
        patientInfoBtn.setDisable(!enabled);
        addRecordBtn.setDisable(!canAddRecord);
        endAppointmentBtn.setDisable(!canEnd);
    }

    private void handleViewDetails() {
        if (selectedAppointment == null) return;
        Patient patient = selectedAppointment.getPatient();
        Slot slot = selectedAppointment.getSlot();
        String msg = String.format("Pacjent: %s\nData: %s\nGodzina: %s-%s\nStatus: %s\nOpis problemu: %s",
                patient != null ? patient.name() : "?",
                slot != null ? slot.date() : "?",
                slot != null ? slot.timeRange().start() : "?",
                slot != null ? slot.timeRange().end() : "?",
                selectedAppointment.status().name(),
                selectedAppointment.problemDescription() != null && !selectedAppointment.problemDescription().isBlank() ? selectedAppointment.problemDescription() : "(brak opisu)");
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void handleOpenCard() {
        if (selectedAppointment == null) return;
        Patient patient = selectedAppointment.getPatient();
        if (patient == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/medical_card.fxml"));
            Parent root = loader.load();
            MedicalCardController controller = loader.getController();
            controller.setPatient(patient); // Передаём пациента явно
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Karta medyczna: " + patient.name());
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handlePatientInfo() {
        if (selectedAppointment == null) return;
        Patient patient = selectedAppointment.getPatient();
        if (patient == null) return;
        String msg = String.format("Imię: %s\nData urodzenia: %s\nTelefon: %s",
                patient.name(), patient.dateOfBirth(), patient.phoneNumber());
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void handleAddRecord() {
        if (selectedAppointment == null) return;
        if (selectedAppointment.status().name().equals("ENDED") || selectedAppointment.status().name().equals("CANCELLED")) return;
        Patient patient = selectedAppointment.getPatient();
        if (patient == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Dodaj wpis do karty");
        dialog.setHeaderText("Podaj opis wpisu do karty medycznej pacjenta");
        dialog.setContentText("Opis:");
        dialog.showAndWait().ifPresent(desc -> {
            if (!desc.isBlank()) {
                var repos = AppContext.getRepositories();
                var card = repos.cards.findById(patient.medicalCardId());
                var doctor = ((DoctorControlPanel)AppContext.getPanel()).currentPerson();
                var record = new com.clinicmanager.model.entities.MedicalRecord(-1, card.id(), ((Doctor)doctor).id(), java.time.LocalDate.now(), desc);
                repos.records.save(record);
                new Alert(Alert.AlertType.INFORMATION, "Wpis dodany!", ButtonType.OK).showAndWait();
            }
        });
    }

    private void handleEndAppointment() {
        if (selectedAppointment == null) return;
        if (selectedAppointment.status().name().equals("ENDED") || selectedAppointment.status().name().equals("CANCELLED")) return;
        selectedAppointment.end(AppContext.getRepositories().appointments);
        loadAppointments();
        new Alert(Alert.AlertType.INFORMATION, "Wizyta zakończona!", ButtonType.OK).showAndWait();
    }

    private void setupTable() {
        appointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadAppointments() {
        var panel = AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        RepositoryManager repos = AppContext.getRepositories();
        List<Appointment> all = repos.appointments.findAll().stream()
                .filter(a -> a.doctorId() == doctor.id())
                .toList();
        boolean onlyActive = showActiveOnlyCheckBox != null && showActiveOnlyCheckBox.isSelected();
        if (onlyActive) {
            myAppointments = all.stream().filter(a ->
                    !(a.status().name().equals("ENDED") || a.status().name().equals("CANCELLED"))
            ).toList();
        } else {
            myAppointments = all;
        }
        ObservableList<AppointmentTableRow> rows = FXCollections.observableArrayList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (var app : myAppointments) {
            Patient patient = app.getPatient();
            Slot slot = app.getSlot();
            String date = slot != null ? slot.date().format(fmt) : "";
            String time = slot != null ? slot.timeRange().start() + "-" + slot.timeRange().end() : "";
            rows.add(new AppointmentTableRow(
                    patient != null ? patient.name() : "?",
                    date,
                    time,
                    app.status().name()
            ));
        }
        appointmentsTable.setItems(rows);
    }

    public static class AppointmentTableRow {
        private final String patientName;
        private final String date;
        private final String time;
        private final String status;

        public AppointmentTableRow(String patientName, String date, String time, String status) {
            this.patientName = patientName;
            this.date = date;
            this.time = time;
            this.status = status;
        }

        public String getPatientName() { return patientName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getStatus() { return status; }
    }
}

