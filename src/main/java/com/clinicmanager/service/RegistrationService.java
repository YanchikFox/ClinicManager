package com.clinicmanager.service;

import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.enums.Role;
import com.clinicmanager.repository.*;
import com.clinicmanager.security.HashUtil;
import com.clinicmanager.exception.RegistrationException;

import java.util.Set;

public class RegistrationService {
    private final static Set<String> VALID_LICENSES = Set.of("DOC123", "DOC456", "SURG2025");

    private final AccountRepository accountRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final SlotRepository slotRepo;

    public RegistrationService(AccountRepository accountRepo, DoctorRepository doctorRepo, PatientRepository patientRepo, SlotRepository slotRepo) {
        this.accountRepo = accountRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.slotRepo = slotRepo;
    }

    public void registerDoctor(String email, String rawPassword, String name, String dateOfBirth, String phone, String licenseCode) {
        if (!VALID_LICENSES.contains(licenseCode)) {
            throw new RegistrationException("Invalid license code: " + licenseCode);
        }

        Doctor doctor = new Doctor(-1, name, dateOfBirth, phone, -1, patientRepo, slotRepo);
        int doctorId = doctorRepo.save(doctor);
        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.DOCTOR, doctorId, true);
        accountRepo.save(acc);
    }

    public void registerPatient(String email, String rawPassword, String name, String dateOfBirth, String phone) {
        Patient patient = new Patient(-1, name, dateOfBirth, phone, -1);
        int patientId = patientRepo.save(patient);
        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.PATIENT, patientId, true);
        accountRepo.save(acc);
    }
}
