package com.clinicmanager.service;

import com.clinicmanager.exception.RegistrationException;
import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.MedicalCard;
import com.clinicmanager.model.entities.Schedule;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;
import com.clinicmanager.model.enums.Role;
import com.clinicmanager.repository.AccountRepository;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.repository.MedicalCardRepository;
import com.clinicmanager.repository.PatientRepository;
import com.clinicmanager.repository.ScheduleRepository;
import com.clinicmanager.repository.SlotRepository;
import com.clinicmanager.time.TimeManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.List;

public class RegistrationService implements RegistrationUseCase {
    private static final Set<String> VALID_LICENSES = Set.of("DOC123", "DOC456", "SURG2025");

    private final AccountRepository accountRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;
    private final MedicalCardRepository medicalCardRepository;
    private final ScheduleRepository scheduleRepository;

    public RegistrationService(AccountRepository accountRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            SlotRepository slotRepository,
            MedicalCardRepository medicalCardRepository,
            ScheduleRepository scheduleRepository) {
        this.accountRepository = accountRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.slotRepository = slotRepository;
        this.medicalCardRepository = medicalCardRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
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

        // --- Create slots for the next 3 days based on the current virtual time ---
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

        Account acc = new Account(-1, email, rawPassword, Role.DOCTOR, doctorId);
        accountRepository.save(acc);
    }

    @Override
    public void registerPatient(String email, String rawPassword, String name, String dateOfBirth, String phone) {
        Patient patient = new Patient(-1, name, dateOfBirth, phone, -1);
        int patientId = patientRepository.save(patient);

        MedicalCard card = new MedicalCard(-1, patientId);
        int cardId = medicalCardRepository.save(card);

        Patient updatedPatient = new Patient(patientId, name, dateOfBirth, phone, cardId);
        patientRepository.update(updatedPatient);

        Account acc = new Account(-1, email, rawPassword, Role.PATIENT, patientId);
        accountRepository.save(acc);
    }

    private boolean isWeekday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
