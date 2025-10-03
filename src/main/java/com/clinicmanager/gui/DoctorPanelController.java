package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.SlotGenerationService;
import com.clinicmanager.time.TimeManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoctorPanelController {
  private static final Logger LOG = LoggerFactory.getLogger(DoctorPanelController.class);
  @FXML private Button viewScheduleBtn;
  @FXML private Button viewAppointmentsBtn;
  @FXML private Button viewMedicalCardBtn;
  @FXML private Button addRecordBtn;
  @FXML private Button logoutBtn;
  @FXML private Label virtualTimeLabel;
  @FXML private Label systemTimeLabel;
  @FXML private Label panelTitle;
  @FXML private Button startTimeBtn;
  @FXML private Button stopTimeBtn;
  @FXML private Button setTimeBtn;
  @FXML private Button englishButton;
  @FXML private Button russianButton;
  @FXML private Button polishButton;

  private final TimeManager timeManager = TimeManager.getInstance();
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private final LocalizationManager localization = LocalizationManager.getInstance();
  private final PanelManager panelManager;
  private final Repositories repositories;
  private final SlotGenerationService slotGenerationService;
  private final ViewLoader viewLoader;

  public DoctorPanelController(
      PanelManager panelManager,
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
    viewScheduleBtn.setOnAction(
        e -> openView("/gui/doctor_schedule.fxml", "doctor.schedule.title"));
    viewAppointmentsBtn.setOnAction(
        e -> openView("/gui/doctor_appointments.fxml", "doctor.appointments.title"));
    viewMedicalCardBtn.setOnAction(
        e -> openView("/gui/doctor_patients.fxml", "doctor.patients.title"));

    logoutBtn.setOnAction(
        e -> {
          var panel = panelManager.getCurrentPanel();
          panel.revokeToken();
          panelManager.clear();
          try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            FxViewHelper.replaceScene(
                stage, viewLoader, "/gui/start_menu.fxml", localization.get("start.title"));
          } catch (IllegalStateException ex) {
            LOG.error("Unable to load start menu", ex);
            showError(localization.get("error.viewLoad"));
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
    var slotRepo = repositories.slots();
    var appointmentRepo = repositories.appointments();
    slotRepo
        .findAll()
        .forEach(
            slot -> {
              LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
              boolean isPast = slotEnd.isBefore(time);
              boolean hasActiveAppointment =
                  appointmentRepo.findAll().stream()
                      .anyMatch(
                          a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
              if (isPast && !hasActiveAppointment) {
                slotRepo.delete(slot);
              }
            });
    slotRepo
        .findAll()
        .forEach(
            slot -> {
              LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
              boolean isFuture = slotStart.isAfter(time);
              boolean hasActiveAppointment =
                  appointmentRepo.findAll().stream()
                      .anyMatch(
                          a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
              if (isFuture && !hasActiveAppointment) {
                slotRepo.delete(slot);
              }
            });
    slotGenerationService.ensureFutureSlotsForAllDoctors();
    Platform.runLater(
        () -> {
          for (Window window : Window.getWindows()) {
            if (window.isShowing()
                && window.getScene() != null
                && window.getScene().getRoot() != null) {
              Parent root = window.getScene().getRoot();
              Object controller = root.getProperties().get("fx:controller");
              if (controller instanceof DoctorAppointmentsController appointmentsController) {
                appointmentsController.reloadAppointments();
              }
              if (controller instanceof DoctorScheduleController scheduleController) {
                scheduleController.loadSlots();
              }
            }
          }
        });
  }

  private void openView(String fxml, String titleKey) {
    try {
      FxViewHelper.showInNewStage(viewLoader, fxml, localization.get(titleKey));
    } catch (IllegalStateException ex) {
      LOG.error("Unable to open view {}", fxml, ex);
      showError(localization.get("error.viewLoad"));
    }
  }

  private void showError(String message) {
    new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
  }

  private void handleSetTime() {
    TextInputDialog dialog = new TextInputDialog(dtf.format(timeManager.getCurrentTime()));
    dialog.setTitle(localization.get("time.dialog.title"));
    dialog.setHeaderText(localization.get("time.dialog.header"));
    dialog.setContentText(localization.get("time.dialog.content"));
    dialog
        .showAndWait()
        .ifPresent(
            str -> {
              try {
                LocalDateTime newTime = LocalDateTime.parse(str, dtf);
                timeManager.setCurrentTime(newTime);
              } catch (Exception ex) {
                new Alert(
                        Alert.AlertType.ERROR, localization.get("time.dialog.error"), ButtonType.OK)
                    .showAndWait();
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
