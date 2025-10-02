package com.clinicmanager.controller;

import com.clinicmanager.exception.InvalidTokenException;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.service.AccountService;

public class DoctorControlPanel extends BaseControlPanel {
    private Doctor doctor;

    public DoctorControlPanel(String token, AccountService accountService, DoctorRepository doctorRepository) {
        super(token, accountService);
        Account acc = accountService.getAccountByToken(token);
        this.doctor = doctorRepository.findById(acc.ownerId());
    }

    @Override
    public Object currentPerson() {
        requireValidToken();
        requireDoctorRole();
        return doctor;
    }

    private void requireDoctorRole() {
        if (!(accountManager.getAccountByToken(token).role().name().equals("DOCTOR"))) {
            throw new InvalidTokenException("Access denied: not a doctor");
        }
    }
}

