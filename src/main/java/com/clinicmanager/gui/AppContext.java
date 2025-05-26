package com.clinicmanager.gui;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.RegistrationService;

public class AppContext {
    private static BaseControlPanel panel;
    private static final RepositoryManager repos = new RepositoryManager("jdbc:sqlite:clinic.db");
    private static final RegistrationService regService = new RegistrationService(repos.accounts, repos.doctors, repos.patients, repos.slots);

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
