package com.clinicmanager.gui;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.time.TimeManager;
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
import javafx.scene.control.*;

public class DoctorPanelController {
    @FXML
    private Button viewScheduleBtn;
    @FXML
    private Button viewAppointmentsBtn;
    @FXML
    private Button viewMedicalCardBtn;
    @FXML
    private Button addRecordBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Label virtualTimeLabel;
    @FXML
    private Label systemTimeLabel;
    @FXML
    private Label panelTitle;
    @FXML
    private Button startTimeBtn;
    @FXML
    private Button stopTimeBtn;
    @FXML
    private Button setTimeBtn;
    @FXML
    private Button englishButton;
    @FXML
    private Button russianButton;
    @FXML
    private Button polishButton;

    private final TimeManager timeManager = TimeManager.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final com.clinicmanager.service.SlotAutoGeneratorService slotAutoGeneratorService = new com.clinicmanager.service.SlotAutoGeneratorService();
    private final LocalizationManager localization = LocalizationManager.getInstance();


    @FXML
    private void initialize() {
        applyLocalization();
        localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
        // Open the doctor's schedule window
        viewScheduleBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_schedule.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle(localization.get("doctor.schedule.title"));
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
                stage.setTitle(localization.get("doctor.appointments.title"));
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
                stage.setTitle(localization.get("doctor.patients.title"));
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
                stage.setTitle(localization.get("start.title"));
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
        dialog.setTitle(localization.get("time.dialog.title"));
        dialog.setHeaderText(localization.get("time.dialog.header"));
        dialog.setContentText(localization.get("time.dialog.content"));
        dialog.showAndWait().ifPresent(str -> {
            try {
                LocalDateTime newTime = LocalDateTime.parse(str, dtf);
                timeManager.setCurrentTime(newTime);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, localization.get("time.dialog.error"), ButtonType.OK).showAndWait();
            }
        });
    }
    @FXML
    private void switchToEnglish() {
        localization.setLocale(LocalizationManager.ENGLISH);
    }

    @FXML
    private void switchToRussian() {
        localization.setLocale(LocalizationManager.RUSSIAN);
    }

    @FXML
    private void switchToPolish() {
        localization.setLocale(LocalizationManager.POLISH);
    }

    private void applyLocalization() {
        systemTimeLabel.setText(localization.get("common.systemTime"));
        startTimeBtn.setText(localization.get("time.start"));
        stopTimeBtn.setText(localization.get("time.stop"));
        setTimeBtn.setText(localization.get("time.set"));
        panelTitle.setText(localization.get("doctor.title"));
        viewScheduleBtn.setText(localization.get("doctor.viewSchedule"));
        viewAppointmentsBtn.setText(localization.get("doctor.viewAppointments"));
        viewMedicalCardBtn.setText(localization.get("doctor.openPatientCard"));
        if (addRecordBtn != null) {
            addRecordBtn.setText(localization.get("doctor.addRecord"));
        }
        logoutBtn.setText(localization.get("common.logout"));
        var stage = MainFX.getPrimaryStage();
        if (stage != null) {
            stage.setTitle(localization.get("doctor.title"));
        }
        updateLanguageButtons();
    }

    private void updateLanguageButtons() {
        var current = localization.getLocale();
        englishButton.setDisable(LocalizationManager.ENGLISH.equals(current));
        russianButton.setDisable(LocalizationManager.RUSSIAN.equals(current));
        polishButton.setDisable(LocalizationManager.POLISH.equals(current));
    }
}
