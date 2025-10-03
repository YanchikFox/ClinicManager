package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.NotificationService;
import com.clinicmanager.time.TimeManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatientPanelController {
  private static final Logger LOG = LoggerFactory.getLogger(PatientPanelController.class);
  @FXML private Button searchDoctorsBtn;
  @FXML private Button viewAppointmentsBtn;
  @FXML private Button viewMedicalCardBtn;
  @FXML private Button logoutBtn;
  @FXML private Button notificationsBtn;
  @FXML private Button favoriteDoctorsBtn;
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
  private final NotificationService notificationService;
  private final ViewLoader viewLoader;

  public PatientPanelController(
      PanelManager panelManager,
      Repositories repositories,
      NotificationService notificationService,
      ViewLoader viewLoader) {
    this.panelManager = panelManager;
    this.repositories = repositories;
    this.notificationService = notificationService;
    this.viewLoader = viewLoader;
  }

  @FXML
  private void initialize() {
    applyLocalization();
    localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
    searchDoctorsBtn.setOnAction(
        e -> openWindow("/gui/doctor_search.fxml", localization.get("patient.doctorSearch.title")));
    viewAppointmentsBtn.setOnAction(
        e ->
            openWindow(
                "/gui/patient_appointments.fxml", localization.get("patient.appointments.title")));
    viewMedicalCardBtn.setOnAction(
        e -> openWindow("/gui/medical_card.fxml", localization.get("patient.medicalCard.title")));

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

    Platform.runLater(this::updateNotificationsButtonStyle);
    notificationsBtn.setOnAction(
        e -> {
          var panel = panelManager.getCurrentPanel();
          if (!(panel.currentPerson() instanceof Patient patient)) {
            return;
          }
          NotificationWindow.showNotifications(
              (Stage) notificationsBtn.getScene().getWindow(), notificationService, patient.id());
          Platform.runLater(this::updateNotificationsButtonStyle);
        });
    notificationsBtn
        .sceneProperty()
        .addListener(
            (obs, oldScene, newScene) -> {
              if (newScene != null) {
                newScene
                    .windowProperty()
                    .addListener(
                        (o, oldWin, newWin) -> {
                          if (newWin != null) {
                            newWin
                                .focusedProperty()
                                .addListener(
                                    (of, was, isNow) -> {
                                      if (isNow) {
                                        Platform.runLater(this::updateNotificationsButtonStyle);
                                      }
                                    });
                          }
                        });
              }
            });

    favoriteDoctorsBtn.setOnAction(e -> showFavoriteDoctors());

    updateTimeLabel(timeManager.getCurrentTime());
    timeManager.addListener(this::onTimeChanged);
    startTimeBtn.setOnAction(e -> timeManager.start());
    stopTimeBtn.setOnAction(e -> timeManager.stop());
    setTimeBtn.setOnAction(e -> handleSetTime());
  }

  private void showFavoriteDoctors() {
    var panel = panelManager.getCurrentPanel();
    if (!(panel.currentPerson() instanceof Patient patient)) {
      return;
    }
    var favorites = repositories.favoriteDoctors().findByPatientId(patient.id());
    List<Doctor> doctors =
        favorites.stream()
            .map(fav -> repositories.doctors().findById(fav.doctorId()))
            .filter(Objects::nonNull)
            .toList();
    try {
      FXMLLoader loader = FxViewHelper.load(viewLoader, "/gui/doctor_search.fxml");
      DoctorSearchController controller = loader.getController();
      controller.setDoctors(doctors);
      FxViewHelper.showInNewStage(loader, localization.get("patient.favoriteDoctors.title"));
    } catch (IllegalStateException ex) {
      LOG.error("Unable to open favourite doctors view", ex);
      showError(localization.get("error.viewLoad"));
    }
  }

  private void openWindow(String fxml, String title) {
    try {
      FxViewHelper.showInNewStage(viewLoader, fxml, title);
    } catch (IllegalStateException ex) {
      LOG.error("Unable to open view {}", fxml, ex);
      showError(localization.get("error.viewLoad"));
    }
  }

  private void updateTimeLabel(LocalDateTime time) {
    Platform.runLater(() -> virtualTimeLabel.setText(dtf.format(time)));
  }

  private void onTimeChanged(LocalDateTime time) {
    updateTimeLabel(time);
    Platform.runLater(
        () -> {
          for (Window window : Window.getWindows()) {
            if (window.isShowing()
                && window.getScene() != null
                && window.getScene().getRoot() != null) {
              Parent root = window.getScene().getRoot();
              Object controller = root.getProperties().get("fx:controller");
              if (controller instanceof PatientAppointmentsController appointmentsController) {
                appointmentsController.reloadAppointments();
              }
            }
          }
        });
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

  private void updateNotificationsButtonStyle() {
    var panel = panelManager.getCurrentPanel();
    if (!(panel.currentPerson() instanceof Patient patient)) {
      return;
    }
    boolean hasUnread =
        !notificationService.getUnreadNotificationsByPersonId(patient.id()).isEmpty();
    Color backgroundColor = hasUnread ? Color.web("#ffcccc") : Color.web("#cccccc");
    notificationsBtn.setBackground(
        new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
    notificationsBtn.setBorder(
        hasUnread
            ? new Border(
                new BorderStroke(
                    Color.RED,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(2)))
            : Border.EMPTY);
  }

  private void showError(String message) {
    new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
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
    panelTitle.setText(localization.get("patient.title"));
    searchDoctorsBtn.setText(localization.get("patient.searchDoctors"));
    viewAppointmentsBtn.setText(localization.get("patient.viewAppointments"));
    viewMedicalCardBtn.setText(localization.get("patient.medicalCard"));
    notificationsBtn.setText(localization.get("patient.notifications"));
    favoriteDoctorsBtn.setText(localization.get("patient.favoriteDoctors"));
    logoutBtn.setText(localization.get("common.logout"));
    var stage = MainFX.getPrimaryStage();
    if (stage != null) {
      stage.setTitle(localization.get("patient.title"));
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
