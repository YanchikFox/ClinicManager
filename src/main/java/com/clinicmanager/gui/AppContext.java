package com.clinicmanager.gui;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.repository.DatabaseInitializer;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.RegistrationService;

public final class AppContext {
    private static final String DB_URL = "jdbc:sqlite:clinic.db";

    static {
        DatabaseInitializer.initialize(DB_URL);
    }

    private static BaseControlPanel panel;
    private static final RepositoryManager repos = new RepositoryManager(DB_URL);
    private static final RegistrationService regService = new RegistrationService();

    private AppContext() {
        throw new UnsupportedOperationException("Utility class");
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
