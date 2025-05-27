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
    @FXML private Label infoLabel;
    @FXML private ListView<String> appointmentsListView;
    @FXML private Button cancelBtn;
    @FXML private Button rescheduleBtn;
    @FXML private Button confirmBtn;

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
    }

    private void updateList() {
        List<String> display = myAppointments.stream().map(a -> {
            Doctor doc = a.getDoctor();
            Slot slot = a.getSlot();
            return String.format("Lekarz: %s | Data: %s | Godzina: %s-%s | Status: %s",
                    doc.name(),
                    slot.date(),
                    slot.timeRange().start(),
                    slot.timeRange().end(),
                    a.status().name());
        }).toList();
        infoLabel.setText("Twoje aktywne wizyty:");
        appointmentsListView.setItems(FXCollections.observableArrayList(display));
    }

    private void handleCancel() {
        if (selectedAppointment != null) {
            selectedAppointment.cancel(repos.appointments);
            notificationManager.createNotification(selectedAppointment.patientId(),
                "Twoja wizyta została anulowana przez użytkownika.");
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
                new Alert(Alert.AlertType.WARNING, "Нет свободных слотов для переноса.", ButtonType.OK).showAndWait();
                return;
            }
            ChoiceDialog<Slot> dialog = new ChoiceDialog<>(freeSlots.get(0), freeSlots);
            dialog.setTitle("Перенос записи");
            dialog.setHeaderText("Выберите новый слот для переноса:");
            dialog.setContentText("Слот:");
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
            VBox vbox = new VBox(new Label("Выберите слот для переноса записи."), slotListView);
            dialog.getDialogPane().setContent(vbox);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return slotListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            dialog.showAndWait().ifPresent(newSlot -> {
                if (newSlot != null) {
                    selectedAppointment.reschedule(newSlot.id(), repos.appointments);
                    notificationManager.createNotification(selectedAppointment.patientId(),
                        "Twoja wizyta została przełożona на: " + newSlot.date() + " " + newSlot.timeRange().start() + "-" + newSlot.timeRange().end());
                    myAppointments = repos.appointments.findAll().stream()
                            .filter(a -> a.patientId() == selectedAppointment.patientId() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                            .toList();
                    updateList();
                }
            });
        }
    }

    private void handleConfirm() {
        if (selectedAppointment != null) {
            selectedAppointment.confirm(repos.appointments);
            notificationManager.createNotification(selectedAppointment.patientId(),
                "Twoja wizyta została potwierdzona.");
            myAppointments = repos.appointments.findAll().stream()
                    .filter(a -> a.patientId() == selectedAppointment.patientId() && (a.status().name().equals("CONFIRMED") || a.status().name().equals("PENDING")))
                    .toList();
            updateList();
        }
    }
}
