package com.clinicmanager.gui;

import com.clinicmanager.app.ClinicFacade;
import com.clinicmanager.app.PanelManager;
import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.service.SlotGenerationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
  private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

  @FXML private TextField emailField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;
  @FXML private Label titleLabel;
  @FXML private Button loginButton;
  @FXML private Button backButton;
  @FXML private Button englishButton;
  @FXML private Button russianButton;
  @FXML private Button polishButton;

  private final ClinicFacade clinic;
  private final PanelManager panelManager;
  private final SlotGenerationService slotGenerationService;
  private final ViewLoader viewLoader;
  private final LocalizationManager localization = LocalizationManager.getInstance();

  public LoginController(
      ClinicFacade clinic,
      PanelManager panelManager,
      SlotGenerationService slotGenerationService,
      ViewLoader viewLoader) {
    this.clinic = clinic;
    this.panelManager = panelManager;
    this.slotGenerationService = slotGenerationService;
    this.viewLoader = viewLoader;
  }

  @FXML
  private void initialize() {
    applyLocalization();
    localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
  }

  @FXML
  private void handleLogin() {
    try {
      String email = emailField.getText();
      String pass = passwordField.getText();

      BaseControlPanel panel = clinic.login(email, pass);
      panelManager.setCurrentPanel(panel);

      // --- Auto-generate slots for all doctors during login ---
      slotGenerationService.ensureFutureSlotsForAllDoctors();

      String fxml =
          (panel instanceof DoctorControlPanel)
              ? "/gui/doctor_panel.fxml"
              : "/gui/patient_panel.fxml";

      Stage stage = MainFX.getPrimaryStage();
      FXMLLoader loader = viewLoader.loader(fxml);
      stage.setScene(new Scene(loader.load()));
      if (panel instanceof DoctorControlPanel) {
        stage.setTitle(localization.get("doctor.title"));
      } else {
        stage.setTitle(localization.get("patient.title"));
      }
    } catch (Exception e) {
      LOG.warn("Login attempt failed", e);
      messageLabel.setText(localization.format("login.error", e.getMessage()));
    }
  }

  @FXML
  private void handleBack() {
    try {
      FXMLLoader loader = viewLoader.loader("/gui/start_menu.fxml");
      Stage stage = MainFX.getPrimaryStage();
      stage.setScene(new Scene(loader.load()));
      stage.setTitle(localization.get("start.title"));
    } catch (Exception e) {
      LOG.error("Unable to load start menu", e);
      showError(localization.get("error.viewLoad"));
    }
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
    titleLabel.setText(localization.get("login.title"));
    emailField.setPromptText(localization.get("login.email"));
    passwordField.setPromptText(localization.get("login.password"));
    loginButton.setText(localization.get("login.submit"));
    backButton.setText(localization.get("common.back"));
    Stage stage = MainFX.getPrimaryStage();
    if (stage != null) {
      stage.setTitle(localization.get("login.title"));
    }
    updateLanguageButtons();
  }

  private void updateLanguageButtons() {
    var current = localization.getLocale();
    englishButton.setDisable(LocalizationManager.ENGLISH.equals(current));
    russianButton.setDisable(LocalizationManager.RUSSIAN.equals(current));
    polishButton.setDisable(LocalizationManager.POLISH.equals(current));
  }

  private void showError(String message) {
    new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
  }
}
