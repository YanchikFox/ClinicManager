package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.RepositoryManager;

public class PatientControlPanel extends BaseControlPanel {
    private Patient patient;

    public PatientControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        super(token, accountManager, notificationManager);
        // Pobierz konto i przypisz pacjenta na podstawie ownerId
        Account acc = accountManager.getAccountByToken(token);
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.patient = repos.patients.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requirePatientRole();
        return patient;
    }

    // Zwraca kartę medyczną pacjenta
    public com.clinicmanager.model.entities.MedicalCard viewMedicalCard() {
        requireValidToken();
        requirePatientRole();
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        return repos.cards.findById(patient.medicalCardId());
    }

    // Zwraca wszystkie wizyty przypisane do pacjenta
    public java.util.List<com.clinicmanager.model.entities.Appointment> getMyAppointments() {
        requireValidToken();
        requirePatientRole();
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        return repos.appointments.findAll().stream()
                .filter(a -> a.patientId() == patient.id())
                .toList();
    }

    // Sprawdza czy zalogowany użytkownik ma rolę PATIENT
    private void requirePatientRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("PATIENT"))) {
            throw new com.clinicmanager.exception.InvalidTokenException("Access denied: not a patient");
        }
    }
}
