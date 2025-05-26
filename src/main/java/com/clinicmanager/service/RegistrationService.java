package com.clinicmanager.service;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.MedicalCard;
import com.clinicmanager.model.entities.Schedule;
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
    private final MedicalCardRepository cardRepo;

    public RegistrationService(AccountRepository accountRepo, DoctorRepository doctorRepo, PatientRepository patientRepo, SlotRepository slotRepo, MedicalCardRepository cardRepo) {
        this.accountRepo = accountRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.slotRepo = slotRepo;
        this.cardRepo = cardRepo;
    }

    public void registerDoctor(String email, String rawPassword, String name, String dateOfBirth, String phone, String licenseCode) {
        if (!VALID_LICENSES.contains(licenseCode)) {
            throw new RegistrationException("Invalid license code: " + licenseCode);
        }

        // 1. Сохраняем доктора без расписания
        Doctor doctor = new Doctor(-1, name, dateOfBirth, phone, -1, patientRepo, slotRepo);
        int doctorId = doctorRepo.save(doctor);

        // 2. Создаём расписание для доктора
        Schedule schedule = new Schedule(-1, doctorId);
        int scheduleId = AppContext.getRepositories().schedules.save(schedule); // Получаем id расписания

        // 3. Обновляем доктора с scheduleId
        Doctor doctorWithSchedule = new Doctor(doctorId, name, dateOfBirth, phone, scheduleId, patientRepo, slotRepo);
        doctorRepo.update(doctorWithSchedule);

        // 4. Создаём дефолтные слоты (Пн-Пт, 9:00-17:00, по часу)
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.DayOfWeek[] days = {java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY, java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY};
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate date = today.plusDays(i);
            for (java.time.DayOfWeek d : days) {
                if (date.getDayOfWeek() == d) {
                    for (int hour = 9; hour < 17; hour++) {
                        var timeRange = new com.clinicmanager.model.entities.TimeRange(java.time.LocalTime.of(hour, 0), java.time.LocalTime.of(hour + 1, 0));
                        var slot = new com.clinicmanager.model.entities.Slot(-1, scheduleId, date, timeRange);
                        slotRepo.save(slot);
                    }
                }
            }
        }

        // 5. Создаём аккаунт
        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.DOCTOR, doctorId, true);
        accountRepo.save(acc);
    }

    public void registerPatient(String email, String rawPassword, String name, String dateOfBirth, String phone) {
        // 1. Сохраняем пациента без карты
        Patient patient = new Patient(-1, name, dateOfBirth, phone, -1);
        int patientId = patientRepo.save(patient);
        // 2. Создаём медицинскую карту
        MedicalCard card = new MedicalCard(-1, patientId);
        int cardId = cardRepo.save(card);
        // 3. Обновляем пациента с medicalCardId
        Patient patientWithCard = new Patient(patientId, name, dateOfBirth, phone, cardId);
        patientRepo.update(patientWithCard);
        // 4. Создаём аккаунт
        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.PATIENT, patientId, true);
        accountRepo.save(acc);
    }
}
