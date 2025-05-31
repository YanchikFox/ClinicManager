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
    private Button endAppointmentBtn; //
    @FXML
    private Button closeBtn;

    private List<Appointment> myAppointments;
    private Appointment selectedAppointment;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        setupTable();
        loadAppointments();
        appointmentsTable.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx != null && idx.intValue() >= 0 && idx.intValue() < myAppointments.size()) {
                selectedAppointment = myAppointments.get(idx.intValue());
                // TODO: enable/disable buttons based on selection
            } else {
                selectedAppointment = null;
            }
        });
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
        myAppointments = repos.appointments.findAll().stream()
                .filter(a -> a.doctorId() == doctor.id())
                .toList();
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

        public String getPatientName() {
            return patientName;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public String getStatus() {
            return status;
        }
    }
}

