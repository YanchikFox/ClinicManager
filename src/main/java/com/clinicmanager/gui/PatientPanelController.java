package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.NotificationService;
import com.clinicmanager.time.TimeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private final PanelManager panelManager;
    private final Repositories repositories;
    private final NotificationService notificationService;
    private final ViewLoader viewLoader;

    public PatientPanelController(PanelManager panelManager,
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
        searchDoctorsBtn.setOnAction(e -> openWindow("/gui/doctor_search.fxml",
                localization.get("patient.doctorSearch.title")));
        viewAppointmentsBtn.setOnAction(e -> openWindow("/gui/patient_appointments.fxml",
                localization.get("patient.appointments.title")));
        viewMedicalCardBtn.setOnAction(e -> openWindow("/gui/medical_card.fxml",
                localization.get("patient.medicalCard.title")));

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

        Platform.runLater(this::updateNotificationsButtonStyle);
        notificationsBtn.setOnAction(e -> {
            var panel = panelManager.getCurrentPanel();
            if (!(panel.currentPerson() instanceof Patient patient)) {
                return;
            }
            NotificationWindow.showNotifications((javafx.stage.Stage) notificationsBtn.getScene().getWindow(),
                    notificationService,
                    patient.id());
            Platform.runLater(this::updateNotificationsButtonStyle);
        });
        notificationsBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((o, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((of, was, isNow) -> {
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
        try {
            var panel = panelManager.getCurrentPanel();
            if (!(panel.currentPerson() instanceof Patient patient)) {
                return;
            }
            var favs = repositories.favoriteDoctors().findByPatientId(patient.id());
            List<com.clinicmanager.model.actors.Doctor> doctors = favs.stream()
                    .map(fav -> repositories.doctors().findById(fav.doctorId()))
                    .filter(d -> d != null)
                    .toList();
            javafx.fxml.FXMLLoader loader = viewLoader.loader("/gui/doctor_search.fxml");
            Parent root = loader.load();
            DoctorSearchController controller = loader.getController();
            controller.setDoctors(doctors);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(localization.get("patient.favoriteDoctors.title"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openWindow(String fxml, String title) {
        try {
            javafx.fxml.FXMLLoader loader = viewLoader.loader(fxml);
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTimeLabel(LocalDateTime time) {
        Platform.runLater(() -> virtualTimeLabel.setText(dtf.format(time)));
    }

    private void onTimeChanged(LocalDateTime time) {
        updateTimeLabel(time);
        Platform.runLater(() -> {
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null
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
        var panel = panelManager.getCurrentPanel();
        if (!(panel.currentPerson() instanceof Patient patient)) {
            return;
        }
        boolean hasUnread = !notificationService.getUnreadNotificationsByPersonId(patient.id()).isEmpty();
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
