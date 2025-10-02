package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;
import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.controller.PatientControlPanel;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.service.AccountService;

public class Clinic implements ClinicFacade {
    private final AccountService accountService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public Clinic(AccountService accountService,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository) {
        this.accountService = accountService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public BaseControlPanel login(String email, String password) {
        String token = accountService.login(email, password);
        Account acc = accountService.getAccountByToken(token);

        return switch (acc.role()) {
            case DOCTOR -> new DoctorControlPanel(token, accountService, doctorRepository);
            case PATIENT -> new PatientControlPanel(token, accountService, patientRepository);
        };
    }

}