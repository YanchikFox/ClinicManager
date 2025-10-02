package com.clinicmanager.repository;

public interface Repositories {
    AccountRepository accounts();

    DoctorRepository doctors();

    PatientRepository patients();

    AppointmentRepository appointments();

    ScheduleRepository schedules();

    SlotRepository slots();

    MedicalCardRepository medicalCards();

    MedicalRecordRepository medicalRecords();

    NotificationRepository notifications();

    FavoriteDoctorRepository favoriteDoctors();
}
