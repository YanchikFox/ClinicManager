package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.service.AccountService;

public class PatientControlPanel extends BaseControlPanel {
    private final Patient patient;

    public PatientControlPanel(String token, AccountService accountService, PatientRepository patientRepository) {
        super(token, accountService);
        Account acc = accountService.getAccountByToken(token);
        this.patient = patientRepository.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requirePatientRole();
        return patient;
    }

    private void requirePatientRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("PATIENT"))) {
            throw new InvalidTokenException("Access denied: not a patient");
        }
    }
}
