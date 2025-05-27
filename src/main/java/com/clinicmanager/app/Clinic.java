package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.controller.PatientControlPanel;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.NotificationRepository;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;

import static com.clinicmanager.model.enums.Role.PATIENT;

/**
 * Klasa Clinic reprezentuje punkt wejścia logiki autoryzacyjnej i
 * przekierowania użytkownika do odpowiedniego panelu kontrolnego.
 * Odpowiada za logowanie oraz inicjalizację kontekstu (czyli odpowiedniego
 * panelu
 * zależnie od roli użytkownika).
 */
public class Clinic {
    private final AccountManager accountManager;
    private final NotificationManager notificationManager;

    /**
     * Konstruktor Clinic – przyjmuje menedżera kont i menedżera powiadomień,
     * które są następnie przekazywane do paneli kontrolnych.
     */
    public Clinic(AccountManager accountManager, NotificationManager notificationManager) {
        this.accountManager = accountManager;
        this.notificationManager = notificationManager;
    }


    public BaseControlPanel login(String email, String password) {
        // Logowanie i uzyskanie tokenu sesji
        String token = accountManager.login(email, password);

        // Pobranie konta użytkownika powiązanego z tokenem
        Account acc = accountManager.getAccountByToken(token);

        // Przekierowanie użytkownika do odpowiedniego panelu
        return switch (acc.role()) {
            case DOCTOR -> new DoctorControlPanel(token, accountManager, notificationManager);
            case PATIENT -> new PatientControlPanel(token, accountManager, notificationManager);
        };
    }
}
