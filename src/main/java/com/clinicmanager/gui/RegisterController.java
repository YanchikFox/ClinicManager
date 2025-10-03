package com.clinicmanager.gui;

import com.clinicmanager.app.ViewLoader;
import com.clinicmanager.gui.localization.LocalizationManager;
import com.clinicmanager.model.enums.Role;
import com.clinicmanager.service.RegistrationUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
  private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

  @FXML private ChoiceBox<Role> roleBox;
  @FXML private TextField emailField;
  @FXML private TextField nameField;
  @FXML private TextField dobField;
  @FXML private TextField phoneField;
  @FXML private TextField licenseField;
  @FXML private PasswordField passwordField;
  @FXML private Label messageLabel;
  @FXML private Label titleLabel;
  @FXML private Label roleLabel;
  @FXML private Label emailLabel;
  @FXML private Label passwordLabel;
  @FXML private Label nameLabel;
  @FXML private Label dobLabel;
  @FXML private Label phoneLabel;
  @FXML private Label licenseLabel;
  @FXML private Button registerButton;
  @FXML private Button backButton;
  @FXML private Button englishButton;
  @FXML private Button russianButton;
  @FXML private Button polishButton;

  private final RegistrationUseCase regService;
  private final ViewLoader viewLoader;

  public RegisterController(RegistrationUseCase regService, ViewLoader viewLoader) {
    this.regService = regService;
    this.viewLoader = viewLoader;
  }

  private final LocalizationManager localization = LocalizationManager.getInstance();

  @FXML
  private void initialize() {
    roleBox.getItems().addAll(Role.DOCTOR, Role.PATIENT);
    roleBox.setValue(Role.PATIENT);
    applyLocalization();
    localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
  }

  @FXML
  private void handleRegister() {
    try {
      Role role = roleBox.getValue();
      if (role == Role.DOCTOR) {
        regService.registerDoctor(
            emailField.getText(), passwordField.getText(),
            nameField.getText(), dobField.getText(),
            phoneField.getText(), licenseField.getText());
      } else {
        regService.registerPatient(
            emailField.getText(),
            passwordField.getText(),
            nameField.getText(),
            dobField.getText(),
            phoneField.getText());
      }
      messageLabel.setText(localization.get("register.success"));
    } catch (Exception e) {
      LOG.warn("Registration failed", e);
      messageLabel.setText(localization.format("register.error", e.getMessage()));
    }
  }

  @FXML
  private void handleBack() {
    try {
      Stage stage = MainFX.getPrimaryStage();
      FxViewHelper.replaceScene(
          stage, viewLoader, "/gui/start_menu.fxml", localization.get("start.title"));
    } catch (IllegalStateException e) {
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
    titleLabel.setText(localization.get("register.title"));
    roleLabel.setText(localization.get("register.role"));
    emailLabel.setText(localization.get("register.email"));
    passwordLabel.setText(localization.get("register.password"));
    nameLabel.setText(localization.get("register.name"));
    dobLabel.setText(localization.get("register.dob"));
    phoneLabel.setText(localization.get("register.phone"));
    licenseLabel.setText(localization.get("register.license"));
    registerButton.setText(localization.get("register.submit"));
    backButton.setText(localization.get("common.back"));
    Stage stage = MainFX.getPrimaryStage();
    if (stage != null) {
      stage.setTitle(localization.get("register.title"));
    }
    roleBox.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(Role role) {
            if (role == null) {
              return "";
            }
            return switch (role) {
              case DOCTOR -> localization.get("register.role.doctor");
              case PATIENT -> localization.get("register.role.patient");
            };
          }

          @Override
          public Role fromString(String string) {
            if (string == null) {
              return null;
            }
            if (string.equals(localization.get("register.role.doctor"))) {
              return Role.DOCTOR;
            }
            if (string.equals(localization.get("register.role.patient"))) {
              return Role.PATIENT;
            }
            // Fallback: return current selection
            return roleBox.getValue();
          }
        });
    roleBox.setValue(roleBox.getValue());
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
