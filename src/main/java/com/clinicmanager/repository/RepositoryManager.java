package com.clinicmanager.repository;

public class RepositoryManager implements Repositories {
    private final AccountRepository accounts;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final ScheduleRepository schedules;
    private final SlotRepository slots;
    private final MedicalCardRepository cards;
    private final MedicalRecordRepository records;
    private final NotificationRepository notifications;
    private final FavoriteDoctorRepository favoriteDoctors;

    public RepositoryManager(String dbUrl) {
        this.patients = new PatientRepository(dbUrl);
        this.accounts = new AccountRepository(dbUrl);
        this.slots = new SlotRepository(dbUrl);
        this.doctors = new DoctorRepository(dbUrl);
        this.appointments = new AppointmentRepository(dbUrl, this.slots);
        this.schedules = new ScheduleRepository(dbUrl);
        this.cards = new MedicalCardRepository(dbUrl);
        this.records = new MedicalRecordRepository(dbUrl);
        this.notifications = new NotificationRepository(dbUrl);
        this.favoriteDoctors = new FavoriteDoctorRepository(dbUrl);
    }

    @Override
    public AccountRepository accounts() {
        return accounts;
    }

    @Override
    public DoctorRepository doctors() {
        return doctors;
    }

    @Override
    public PatientRepository patients() {
        return patients;
    }

    @Override
    public AppointmentRepository appointments() {
        return appointments;
    }

    @Override
    public ScheduleRepository schedules() {
        return schedules;
    }

    @Override
    public SlotRepository slots() {
        return slots;
    }

    @Override
    public MedicalCardRepository medicalCards() {
        return cards;
    }

    @Override
    public MedicalRecordRepository medicalRecords() {
        return records;
    }

    @Override
    public NotificationRepository notifications() {
        return notifications;
    }

    @Override
    public FavoriteDoctorRepository favoriteDoctors() {
        return favoriteDoctors;
    }

    public void closeAll() {
        try {
            accounts.close();
            doctors.close();
            patients.close();
            appointments.close();
            schedules.close();
            slots.close();
            cards.close();
            records.close();
            notifications.close();
            favoriteDoctors.close();
        } catch (Exception e) {
            throw new RuntimeException("Error closing DB connections", e);
        }
    }
}
