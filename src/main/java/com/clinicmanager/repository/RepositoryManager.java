package com.clinicmanager.repository;

public class RepositoryManager {
    public final AccountRepository accounts;
    public final DoctorRepository doctors;
    public final PatientRepository patients;
    public final AppointmentRepository appointments;
    public final ScheduleRepository schedules;
    public final SlotRepository slots;
    public final MedicalCardRepository cards;
    public final MedicalRecordRepository records;
    public final NotificationRepository notifications;
    public final FavoriteDoctorRepository favoriteDoctors;

    public RepositoryManager(String dbUrl) {
        this.patients = new PatientRepository(dbUrl);
        this.accounts = new AccountRepository(dbUrl);
        this.slots = new SlotRepository(dbUrl);
        this.doctors = new DoctorRepository(dbUrl);
        this.appointments = new AppointmentRepository(dbUrl);
        this.schedules = new ScheduleRepository(dbUrl);
        this.cards = new MedicalCardRepository(dbUrl);
        this.records = new MedicalRecordRepository(dbUrl);
        this.notifications = new NotificationRepository(dbUrl);
        this.favoriteDoctors = new FavoriteDoctorRepository(dbUrl);
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
