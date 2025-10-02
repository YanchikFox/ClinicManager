package com.clinicmanager.gui;
import com.clinicmanager.gui.localization.LocalizationManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.clinicmanager.time.TimeManager;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class PatientPanelController {

    @FXML
    private Button searchDoctorsBtn;
    @FXML
    private Button viewAppointmentsBtn;
    @FXML
    private Button viewMedicalCardBtn;
    @FXML
    private Button logoutBtn;
    @FXML
    private Button notificationsBtn;
    @FXML
    private Button favoriteDoctorsBtn;
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

    @FXML
    private void initialize() {
        applyLocalization();
        localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
        searchDoctorsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_search.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle(localization.get("patient.doctorSearch.title"));
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        viewAppointmentsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/patient_appointments.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle(localization.get("patient.appointments.title"));
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        viewMedicalCardBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/medical_card.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle(localization.get("patient.medicalCard.title"));
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        logoutBtn.setOnAction(e -> {
            try {
                // Revoke the token when logging out
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

        // Notifications
        javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
        notificationsBtn.setOnAction(e -> {
            var panel = com.clinicmanager.gui.AppContext.getPanel();
            if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient))
                return;
            var notificationManager = com.clinicmanager.gui.AppContext.getRepositories().notifications;
            var service = new com.clinicmanager.service.NotificationManager(notificationManager);
            // force reload notifications from DB each time
            NotificationWindow.showNotifications((javafx.stage.Stage) notificationsBtn.getScene().getWindow(), service,
                    patient.id());
            // update style after closing window
            javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
        });
        // Add a listener to update the style when the window regains focus
        notificationsBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((of, was, isNow) -> {
                            if (isNow)
                                javafx.application.Platform.runLater(this::updateNotificationsButtonStyle);
                        });
                    }
                });
            }
        });

        favoriteDoctorsBtn.setOnAction(e -> {
            try {
                var panel = com.clinicmanager.gui.AppContext.getPanel();
                if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient))
                    return;
                var repos = com.clinicmanager.gui.AppContext.getRepositories();
                var favs = repos.favoriteDoctors.findByPatientId(patient.id());
                java.util.List<com.clinicmanager.model.actors.Doctor> doctors = favs.stream()
                        .map(fav -> repos.doctors.findById(fav.doctorId()))
                        .filter(d -> d != null)
                        .toList();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/gui/doctor_search.fxml"));
                javafx.scene.Parent root = loader.load();
                com.clinicmanager.gui.DoctorSearchController controller = loader.getController();
                controller.setDoctors(doctors);
                javafx.scene.Scene scene = new javafx.scene.Scene(root);
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle(localization.get("patient.favoriteDoctors.title"));
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Virtual time management
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
        // Automatically refresh all open patient windows after the time changes
        javafx.application.Platform.runLater(() -> {
            // Refresh the patient appointments window if it is open
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null
                        && window.getScene().getRoot() instanceof javafx.scene.Parent) {
                    javafx.scene.Parent root = window.getScene().getRoot();
                    Object controller = root.getProperties().get("fx:controller");
                    if (controller instanceof PatientAppointmentsController) {
                        ((PatientAppointmentsController) controller).reloadAppointments();
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
        dialog.showAndWait().ifPresent(str -> {
            try {
                LocalDateTime newTime = LocalDateTime.parse(str, dtf);
                timeManager.setCurrentTime(newTime);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, localization.get("time.dialog.error"), ButtonType.OK).showAndWait();
            }
        });
    }

    private void updateNotificationsButtonStyle() {
        var panel = com.clinicmanager.gui.AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient))
            return;
        var notificationManager = com.clinicmanager.gui.AppContext.getRepositories().notifications;
        var service = new com.clinicmanager.service.NotificationManager(notificationManager);
        // always reload from DB
        boolean hasUnread = !service.getUnreadNotificationsByPersonId(patient.id()).isEmpty();
        notificationsBtn.setStyle(hasUnread
                ? "-fx-background-color: #ffcccc; -fx-border-color: red; -fx-border-width: 2px;"
                : "-fx-background-color: #cccccc;");
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
