package com.clinicmanager.gui;

import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.RegistrationService;
import com.clinicmanager.controller.BaseControlPanel;

public class AppContext {
    private static BaseControlPanel panel;
    private static final RepositoryManager repos = new RepositoryManager("jdbc:sqlite:clinic.db");
    private static final RegistrationService regService = new RegistrationService();
    private static AppContext sharedInstance;

    private AppContext() {
    }

    public static AppContext getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new AppContext();
        }
        return sharedInstance;
    }

    public static void setPanel(BaseControlPanel p) {
        panel = p;
    }

    public static BaseControlPanel getPanel() {
        return panel;
    }

    public static RepositoryManager getRepositories() {
        return repos;
    }

    public static RegistrationService getRegistrationService() {
        return regService;
    }
}
