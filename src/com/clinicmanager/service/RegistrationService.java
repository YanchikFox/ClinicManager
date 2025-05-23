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

    public RegistrationService(AccountRepository accountRepo, DoctorRepository doctorRepo, PatientRepository patientRepo) {
        this.accountRepo = accountRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
    }

    public void registerDoctor(String email, String rawPassword, String name, String dateOfBirth, String phone, String licenseCode) {
        if (!VALID_LICENSES.contains(licenseCode)) {
            throw new RegistrationException("Invalid license code: " + licenseCode);
        }

        int doctorId = generateId(); // временно, пока нет автоинкремента
        Doctor doctor = new Doctor(doctorId, name, dateOfBirth, phone, -1);
        doctorRepo.save(doctor);

        Account acc = new Account(generateId(), email, HashUtil.sha256(rawPassword), Role.DOCTOR, doctorId, true);
        accountRepo.save(acc);
    }

    public void registerPatient(String email, String rawPassword, String name, String dateOfBirth, String phone) {
        int patientId = generateId();
        Patient patient = new Patient(patientId, name, dateOfBirth, phone, -1);
        patientRepo.save(patient);

        Account acc = new Account(generateId(), email, HashUtil.sha256(rawPassword), Role.PATIENT, patientId, true);
        accountRepo.save(acc);
    }

    private int generateId() {
        // Для SQLite, можно заменить позже на автоинкремент
        return (int)(Math.random() * 100000);
    }
}