package com.clinicmanager.gui;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.RegistrationService;

public class AppContext {
    // Aktualnie zalogowany panel (pacjenta lub lekarza)
    private static BaseControlPanel panel;

    // Wspólny dostęp do repozytoriów i usług
    private static final RepositoryManager repos = new RepositoryManager("jdbc:sqlite:clinic.db");
    private static final RegistrationService regService = new RegistrationService();

    // Instancja singletona
    private static AppContext sharedInstance;

    // Prywatny konstruktor (wzorzec Singleton)
    private AppContext() {
    }

    // Zwraca globalną instancję AppContext
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
