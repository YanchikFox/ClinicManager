package com.clinicmanager.gui;
import com.clinicmanager.gui.localization.LocalizationManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StartMenuController {

    @FXML
    private Label titleLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Button englishButton;
    @FXML
    private Button russianButton;
    @FXML
    private Button polishButton;

    private final LocalizationManager localization = LocalizationManager.getInstance();

    @FXML
    private void initialize() {
        applyLocalization();
        localization.localeProperty().addListener((obs, oldLocale, newLocale) -> applyLocalization());
    }

    @FXML
    private void handleLogin() {
        switchScene("/gui/login.fxml", localization.get("login.title"));
    }

    @FXML
    private void handleRegister() {
        switchScene("/gui/register.fxml", localization.get("register.title"));
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

    private void switchScene(String fxml, String title) {
        try {
            Stage stage = MainFX.getPrimaryStage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyLocalization() {
        titleLabel.setText(localization.get("start.title"));
        loginButton.setText(localization.get("start.login"));
        registerButton.setText(localization.get("start.register"));
        Stage stage = MainFX.getPrimaryStage();
        if (stage != null) {
            stage.setTitle(localization.get("start.title"));
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
