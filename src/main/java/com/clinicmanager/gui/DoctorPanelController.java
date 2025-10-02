package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.SlotGenerationService;
import com.clinicmanager.time.TimeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private final LocalizationManager localization = LocalizationManager.getInstance();
    private final PanelManager panelManager;
    private final Repositories repositories;
    private final SlotGenerationService slotGenerationService;
    private final ViewLoader viewLoader;

    public DoctorPanelController(PanelManager panelManager,
            Repositories repositories,
            SlotGenerationService slotGenerationService,
            ViewLoader viewLoader) {
        this.panelManager = panelManager;
        this.repositories = repositories;
        this.slotGenerationService = slotGenerationService;
        this.viewLoader = viewLoader;
    }


    @FXML
    private void initialize() {
        applyLocalization();
        localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
        // Open the doctor's schedule window
        viewScheduleBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/doctor_schedule.fxml");
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
                javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/doctor_appointments.fxml");
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
                javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/doctor_patients.fxml");
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
                var panel = panelManager.getCurrentPanel();
                panel.revokeToken();
                panelManager.clear();
                javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/start_menu.fxml");
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
        var slotRepo = repositories.slots();
        var appointmentRepo = repositories.appointments();
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
        slotGenerationService.ensureFutureSlotsForAllDoctors();
        // --- Refresh all open windows/controllers (e.g. appointment lists, schedules) ---
        javafx.application.Platform.runLater(() -> {
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null
                        && window.getScene().getRoot() != null) {
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
