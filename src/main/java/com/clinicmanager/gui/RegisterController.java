package com.clinicmanager.gui;

import com.clinicmanager.service.RegistrationService;
import com.clinicmanager.model.enums.Role;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML
    private ChoiceBox<String> roleBox;
    @FXML
    private TextField emailField, nameField, dobField, phoneField, licenseField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final RegistrationService regService;

    public RegisterController() {
        this.regService = AppContext.getRegistrationService();
    }

    @FXML
    private void initialize() {
        roleBox.getItems().addAll("DOCTOR", "PATIENT");
        roleBox.setValue("PATIENT");
    }

    @FXML
    private void handleRegister() {
        try {
            String role = roleBox.getValue();
            if (role.equals("DOCTOR")) {
                regService.registerDoctor(
                        emailField.getText(), passwordField.getText(),
                        nameField.getText(), dobField.getText(),
                        phoneField.getText(), licenseField.getText());
            } else {
                regService.registerPatient(
                        emailField.getText(), passwordField.getText(),
                        nameField.getText(), dobField.getText(),
                        phoneField.getText());
            }
            messageLabel.setText("✅ Registered successfully!");
        } catch (Exception e) {
            messageLabel.setText("❌ Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/start_menu.fxml"));
            Stage stage = MainFX.getPrimaryStage();
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}