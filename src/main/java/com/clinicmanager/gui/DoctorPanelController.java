package com.clinicmanager.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.time.format.DateTimeFormatter;
import com.clinicmanager.time.TimeManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorPanelController {
    @FXML
    private Button viewScheduleBtn;
    @FXML
    private Button viewAppointmentsBtn;
    @FXML
    private Button viewMedicalCardBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Label virtualTimeLabel;
    @FXML
    private Button startTimeBtn;
    @FXML
    private Button stopTimeBtn;
    @FXML
    private Button setTimeBtn;
    @FXML
    private Button addRecordBtn;

    private final TimeManager timeManager = TimeManager.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final com.clinicmanager.service.SlotAutoGeneratorService slotAutoGeneratorService = new com.clinicmanager.service.SlotAutoGeneratorService();

    @FXML
    private void initialize() {
        // Open the doctor's schedule window
        viewScheduleBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_schedule.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("My schedule");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Open the doctor's appointments window
        viewAppointmentsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_appointments.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("My appointments");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Open the patient list window
        viewMedicalCardBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_patients.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("My patients");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Log out
        logoutBtn.setOnAction(e -> {
            try {
                var panel = com.clinicmanager.gui.AppContext.getPanel();
                if (panel != null) {
                    panel.revokeToken();
                }
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/start_menu.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Virtual time handling
        updateTimeLabel(timeManager.getCurrentTime());
        timeManager.addListener(this::onTimeChanged);
        startTimeBtn.setOnAction(e -> timeManager.start());
        stopTimeBtn.setOnAction(e -> timeManager.stop());
        setTimeBtn.setOnAction(e -> handleSetTime());

        addRecordBtn.setOnAction(e -> handleAddRecord());
    }

    private void updateTimeLabel(LocalDateTime time) {
        Platform.runLater(() -> virtualTimeLabel.setText(dtf.format(time)));
    }

    private void onTimeChanged(LocalDateTime time) {
        updateTimeLabel(time);
        // Remove all free slots that are fully in the past (end < now)
        var slotRepo = com.clinicmanager.gui.AppContext.getRepositories().slots;
        var appointmentRepo = com.clinicmanager.gui.AppContext.getRepositories().appointments;
        slotRepo.findAll().forEach(slot -> {
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            boolean isPast = slotEnd.isBefore(time);
            boolean hasActiveAppointment = appointmentRepo.findAll().stream()
                    .anyMatch(a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
            if (isPast && !hasActiveAppointment) {
                slotRepo.delete(slot);
            }
        });
        // --- New: remove all free slots that start after the current time (future) ---
        slotRepo.findAll().forEach(slot -> {
            LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
            boolean isFuture = slotStart.isAfter(time);
            boolean hasActiveAppointment = appointmentRepo.findAll().stream()
                    .anyMatch(a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
            if (isFuture && !hasActiveAppointment) {
                slotRepo.delete(slot);
            }
        });
        // --- Auto-generate slots for all doctors after the time changes ---
        slotAutoGeneratorService.ensureFutureSlotsForAllDoctors();
        // --- Refresh all open windows/controllers (e.g. appointment lists, schedules) ---
        javafx.application.Platform.runLater(() -> {
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null
                        && window.getScene().getRoot() instanceof javafx.scene.Parent) {
                    javafx.scene.Parent root = window.getScene().getRoot();
                    Object controller = root.getProperties().get("fx:controller");
                    if (controller instanceof com.clinicmanager.gui.DoctorAppointmentsController) {
                        ((com.clinicmanager.gui.DoctorAppointmentsController) controller).reloadAppointments();
                    }
                    if (controller instanceof com.clinicmanager.gui.DoctorScheduleController) {
                        ((com.clinicmanager.gui.DoctorScheduleController) controller).loadSlots();
                    }
                }
            }
        });
        // TODO: send notifications ten minutes before an appointment
    }

    private void handleSetTime() {
        TextInputDialog dialog = new TextInputDialog(dtf.format(timeManager.getCurrentTime()));
        dialog.setTitle("Set system time");
        dialog.setHeaderText("Enter the new time (yyyy-MM-dd HH:mm):");
        dialog.setContentText("Time:");
        dialog.showAndWait().ifPresent(str -> {
            try {
                LocalDateTime newTime = LocalDateTime.parse(str, dtf);
                timeManager.setCurrentTime(newTime);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Invalid time format!", ButtonType.OK).showAndWait();
            }
        });
    }

    private void handleAddRecord() {
        var panel = com.clinicmanager.gui.AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Doctor doctor)) {
            new Alert(Alert.AlertType.ERROR, "Unable to determine the current doctor.", ButtonType.OK).showAndWait();
            return;
        }
        var repos = com.clinicmanager.gui.AppContext.getRepositories();
        LocalDateTime now = timeManager.getCurrentTime();
        List<com.clinicmanager.model.entities.Appointment> eligibleAppointments = repos.appointments.findAll().stream()
                .filter(app -> app.doctorId() == doctor.id())
                .filter(app -> !app.status().name().equals("CANCELLED"))
                .filter(app -> !repos.records.existsForAppointment(app.id()))
                .filter(app -> {
                    var slot = app.getSlot();
                    if (slot == null) {
                        return false;
                    }
                    LocalDateTime start = LocalDateTime.of(slot.date(), slot.timeRange().start());
                    return !now.isBefore(start);
                })
                .collect(Collectors.toList());

        if (eligibleAppointments.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "No appointments are eligible for adding a medical record right now.", ButtonType.OK).showAndWait();
            return;
        }

        List<AppointmentOption> options = eligibleAppointments.stream()
                .map(AppointmentOption::new)
                .collect(Collectors.toList());
        ChoiceDialog<AppointmentOption> dialog = new ChoiceDialog<>(options.get(0), options);
        dialog.setTitle("Select appointment");
        dialog.setHeaderText("Choose an appointment to add a medical record:");
        dialog.setContentText("Appointment:");
        dialog.showAndWait().ifPresent(choice -> {
            var appointment = choice.appointment();
            var patient = appointment.getPatient();
            if (patient == null) {
                new Alert(Alert.AlertType.ERROR, "Could not load patient data.", ButtonType.OK).showAndWait();
                return;
            }
            if (repos.records.existsForAppointment(appointment.id())) {
                new Alert(Alert.AlertType.WARNING, "A record has already been added for this appointment.",
                        ButtonType.OK).showAndWait();
                return;
            }
            TextInputDialog descDialog = new TextInputDialog();
            descDialog.setTitle("Add record to card");
            descDialog.setHeaderText("Enter a description for the patient's medical record");
            descDialog.setContentText("Description:");
            descDialog.showAndWait().ifPresent(desc -> {
                if (desc.isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Description cannot be empty.", ButtonType.OK).showAndWait();
                    return;
                }
                var card = repos.cards.findById(patient.medicalCardId());
                LocalDateTime currentTime = timeManager.getCurrentTime();
                var record = new com.clinicmanager.model.entities.MedicalRecord(-1, card.id(), doctor.id(),
                        currentTime.toLocalDate(), desc, appointment.id());
                repos.records.save(record);
                new Alert(Alert.AlertType.INFORMATION, "Record added!", ButtonType.OK).showAndWait();
            });
        });
    }

    private static class AppointmentOption {
        private final com.clinicmanager.model.entities.Appointment appointment;

        private AppointmentOption(com.clinicmanager.model.entities.Appointment appointment) {
            this.appointment = appointment;
        }

        public com.clinicmanager.model.entities.Appointment appointment() {
            return appointment;
        }

        @Override
        public String toString() {
            var patient = appointment.getPatient();
            var slot = appointment.getSlot();
            String patientName = patient != null ? patient.name() : "?";
            String date = slot != null ? slot.date().toString() : "?";
            String time = (slot != null)
                    ? slot.timeRange().start() + "-" + slot.timeRange().end()
                    : "?";
            return patientName + " | " + date + " " + time + " | Status: " + appointment.status().name();
        }
    }
}
