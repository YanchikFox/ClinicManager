package com.clinicmanager.service;

import com.clinicmanager.gui.AppContext;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.MedicalCard;
import com.clinicmanager.model.entities.Schedule;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.model.enums.Role;
import com.clinicmanager.repository.*;
import com.clinicmanager.security.HashUtil;
import com.clinicmanager.exception.RegistrationException;
import com.clinicmanager.time.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;

public class RegistrationService {
    private static final Set<String> VALID_LICENSES = Set.of("DOC123", "DOC456", "SURG2025");

    private final AccountRepository accountRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;
    private final MedicalCardRepository medicalCardRepository;
    private final ScheduleRepository scheduleRepository;

    public RegistrationService() {
        AppContext.getInstance();
        this.accountRepository = AppContext.getRepositories().accounts;
        AppContext.getInstance();
        this.doctorRepository = AppContext.getRepositories().doctors;
        AppContext.getInstance();
        this.patientRepository = AppContext.getRepositories().patients;
        AppContext.getInstance();
        this.slotRepository = AppContext.getRepositories().slots;
        AppContext.getInstance();
        this.medicalCardRepository = AppContext.getRepositories().cards;
        AppContext.getInstance();
        this.scheduleRepository = AppContext.getRepositories().schedules;
    }

    public void registerDoctor(String email, String rawPassword, String name, String dateOfBirth,
            String phone, String licenseCode) {
        if (!VALID_LICENSES.contains(licenseCode)) {
            throw new RegistrationException("Invalid license code: " + licenseCode);
        }

        Doctor doctor = new Doctor(-1, name, dateOfBirth, phone, -1);
        int doctorId = doctorRepository.save(doctor);

        Schedule schedule = new Schedule(-1, doctorId);
        int scheduleId = scheduleRepository.save(schedule);

        Doctor updatedDoctor = new Doctor(doctorId, name, dateOfBirth, phone, scheduleId);
        doctorRepository.update(updatedDoctor);

        // --- Tworzymy sloty na 3 dni do przodu od aktualnego wirtualnego czasu ---
        LocalDate today = TimeManager.getInstance().getCurrentTime().toLocalDate();
        for (int i = 0; i < 3; i++) {
            LocalDate date = today.plusDays(i);
            if (isWeekday(date)) {
                for (int hour = 9; hour < 17; hour++) {
                    TimeRange range = new TimeRange(LocalTime.of(hour, 0), LocalTime.of(hour + 1, 0));
                    Slot slot = new Slot(scheduleId, date, range);
                    slotRepository.save(slot);
                }
            }
        }

        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.DOCTOR, doctorId, true);
        accountRepository.save(acc);
    }

    public void registerPatient(String email, String rawPassword, String name, String dateOfBirth, String phone) {
        Patient patient = new Patient(-1, name, dateOfBirth, phone, -1);
        int patientId = patientRepository.save(patient);

        MedicalCard card = new MedicalCard(-1, patientId);
        int cardId = medicalCardRepository.save(card);

        Patient updatedPatient = new Patient(patientId, name, dateOfBirth, phone, cardId);
        patientRepository.update(updatedPatient);

        Account acc = new Account(-1, email, HashUtil.sha256(rawPassword), Role.PATIENT, patientId, true);
        accountRepository.save(acc);
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
