package com.clinicmanager.gui;

import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;

import java.util.List;

public class PatientAppointmentsController {
    @FXML
    private Label infoLabel;
    @FXML
    private ListView<String> appointmentsListView;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button rescheduleBtn;
    @FXML
    private Button confirmBtn;

    private final RepositoryManager repos = AppContext.getRepositories();
    private final com.clinicmanager.service.NotificationManager notificationManager =
            new com.clinicmanager.service.NotificationManager(AppContext.getRepositories().notifications);
    private List<Appointment> myAppointments;
    private Appointment selectedAppointment;

    @FXML
    private void initialize() {
        Patient patient = (Patient) AppContext.getPanel().currentPerson();
        List<Appointment> all = repos.appointments.findAll();
        myAppointments = all.stream()
                .filter(a -> a.patientId() == patient.id() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                .toList();
        updateList();
        appointmentsListView.getSelectionModel().selectedIndexProperty().addListener((obs, old, idx) -> {
            if (idx != null && idx.intValue() >= 0 && idx.intValue() < myAppointments.size()) {
                selectedAppointment = myAppointments.get(idx.intValue());
                cancelBtn.setDisable(false);
                rescheduleBtn.setDisable(false);
                confirmBtn.setDisable(false);
            } else {
                selectedAppointment = null;
                cancelBtn.setDisable(true);
                rescheduleBtn.setDisable(true);
                confirmBtn.setDisable(true);
            }
        });
        cancelBtn.setOnAction(e -> handleCancel());
        rescheduleBtn.setOnAction(e -> handleReschedule());
        confirmBtn.setOnAction(e -> handleConfirm());
        cancelBtn.setDisable(true);
        rescheduleBtn.setDisable(true);
        confirmBtn.setDisable(true);
        // --- Store the controller in the root properties for global updates ---
        javafx.application.Platform.runLater(() -> {
            if (appointmentsListView.getScene() != null && appointmentsListView.getScene().getRoot() != null) {
                appointmentsListView.getScene().getRoot().getProperties().put("fx:controller", this);
            }
        });
    }

    // --- Public method for updating the appointment list ---
    public void reloadAppointments() {
        Patient patient = (Patient) AppContext.getPanel().currentPerson();
        List<Appointment> all = repos.appointments.findAll();
        myAppointments = all.stream()
                .filter(a -> a.patientId() == patient.id() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                .toList();
        updateList();
    }

    private void updateList() {
        List<String> display = myAppointments.stream().map(a -> {
            Doctor doc = a.getDoctor();
            Slot slot = a.getSlot();
            String doctorName = (doc != null) ? doc.name() : "?";
            String date = (slot != null) ? slot.date().toString() : "?";
            String timeStart = (slot != null) ? slot.timeRange().start().toString() : "?";
            String timeEnd = (slot != null) ? slot.timeRange().end().toString() : "?";
            return String.format("Doctor: %s | Date: %s | Time: %s-%s | Status: %s",
                    doctorName,
                    date,
                    timeStart,
                    timeEnd,
                    a.status().name());
        }).toList();
        infoLabel.setText("Your active appointments:");
        appointmentsListView.setItems(FXCollections.observableArrayList(display));
    }

    private void handleCancel() {
        if (selectedAppointment != null) {
            selectedAppointment.cancel(repos.appointments);
            notificationManager.createNotification(selectedAppointment.patientId(),
                    "Your appointment has been cancelled by the user.");
            myAppointments = repos.appointments.findAll().stream()
                    .filter(a -> a.patientId() == selectedAppointment.patientId() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                    .toList();
            updateList();
        }
    }

    private void handleReschedule() {
        if (selectedAppointment != null) {
            List<Slot> slots = repos.slots.findAll();
            List<Slot> freeSlots = slots.stream()
                    .filter(s -> s.scheduleId() == selectedAppointment.getDoctor().scheduleId() && s.isAvailable())
                    .toList();
            if (freeSlots.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "No available slots to reschedule.", ButtonType.OK).showAndWait();
                return;
            }
            ChoiceDialog<Slot> dialog = new ChoiceDialog<>(freeSlots.get(0), freeSlots);
            dialog.setTitle("Reschedule appointment");
            dialog.setHeaderText("Select a new slot to reschedule:");
            dialog.setContentText("Slot:");
            dialog.setGraphic(null);
            dialog.getDialogPane().setPrefWidth(350);
            dialog.getDialogPane().setPrefHeight(200);
            ListView<Slot> slotListView = new ListView<>();
            slotListView.getItems().addAll(freeSlots);
            slotListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Slot s, boolean empty) {
                    super.updateItem(s, empty);
                    setText((empty || s == null) ? null : s.date() + " " + s.timeRange().start() + "-" + s.timeRange().end());
                }
            });
            slotListView.getSelectionModel().selectFirst();
            VBox vbox = new VBox(new Label("Choose a slot to reschedule the appointment."), slotListView);
            dialog.getDialogPane().setContent(vbox);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return slotListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            dialog.showAndWait().ifPresent(newSlot -> {
                selectedAppointment.reschedule(newSlot.id(), repos.appointments);
                notificationManager.createNotification(selectedAppointment.patientId(),
                        "Your appointment has been rescheduled to: " + newSlot.date() + " " + newSlot.timeRange().start() + "-" + newSlot.timeRange().end());
                myAppointments = repos.appointments.findAll().stream()
                        .filter(a -> a.patientId() == selectedAppointment.patientId() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                        .toList();
                updateList();
            });
        }
    }

    private void handleConfirm() {
        if (selectedAppointment != null) {
            if (!selectedAppointment.status().name().equals("CONFIRMED")) {
                selectedAppointment.confirm(repos.appointments);
                notificationManager.createNotification(selectedAppointment.patientId(),
                        "Your appointment has been confirmed.");
                myAppointments = repos.appointments.findAll().stream()
                        .filter(a -> a.patientId() == selectedAppointment.patientId() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                        .toList();
                updateList();
            }
        }
    }
}
