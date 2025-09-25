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
        // Retrieve the account based on the token
        Account acc = accountManager.getAccountByToken(token);
        // Retrieve the patient using the ownerId
        RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        this.patient = repos.patients.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requirePatientRole();
        return patient;
    }

    // Example protected method: fetch the patient's medical card
    public com.clinicmanager.model.entities.MedicalCard viewMedicalCard() {
        requireValidToken();
        requirePatientRole();
        com.clinicmanager.repository.RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        return repos.cards.findById(patient.medicalCardId());
    }

    // Example protected method: fetch the patient's appointments
    public java.util.List<com.clinicmanager.model.entities.Appointment> getMyAppointments() {
        requireValidToken();
        requirePatientRole();
        com.clinicmanager.repository.RepositoryManager repos = com.clinicmanager.gui.AppContext.getRepositories();
        return repos.appointments.findAll().stream()
                .filter(a -> a.patientId() == patient.id())
                .toList();
    }

    private void requirePatientRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("PATIENT"))) {
            throw new com.clinicmanager.exception.InvalidTokenException("Access denied: not a patient");
        }
    }
}
