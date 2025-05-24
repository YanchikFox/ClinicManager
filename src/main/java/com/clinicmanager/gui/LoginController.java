package com.clinicmanager.gui;

import com.clinicmanager.app.Clinic;
import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.controller.PatientControlPanel;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.security.TokenService;
import com.clinicmanager.service.AccountManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final Clinic clinic;

    public LoginController() {
        RepositoryManager repos = AppContext.getRepositories();
        AccountManager manager = new AccountManager(repos.accounts, new TokenService());
        this.clinic = new Clinic(manager);
    }

    @FXML
    private void handleLogin() {
        try {
            String email = emailField.getText();
            String pass = passwordField.getText();

            BaseControlPanel panel = clinic.login(email, pass);
            AppContext.setPanel(panel);

            String fxml = (panel instanceof DoctorControlPanel)
                    ? "/gui/doctor_panel.fxml"
                    : "/gui/patient_panel.fxml";

            Stage stage = MainFX.getPrimaryStage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) {
            messageLabel.setText("Ошибка: " + e.getMessage());
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
