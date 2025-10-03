package com.clinicmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class RepositoryManager implements Repositories, AutoCloseable {
    private final Connection connection;
    private final boolean ownsConnection;
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
        this(openConnection(dbUrl), true);
    }

    public RepositoryManager(Connection connection) {
        this(connection, false);
    }

    private RepositoryManager(Connection connection, boolean ownsConnection) {
        this.connection = Objects.requireNonNull(connection, "connection must not be null");
        this.ownsConnection = ownsConnection;
        enableForeignKeys(this.connection);
        this.patients = new PatientRepository(this.connection);
        this.accounts = new AccountRepository(this.connection);
        this.slots = new SlotRepository(this.connection);
        this.doctors = new DoctorRepository(this.connection);
        this.appointments = new AppointmentRepository(this.connection, this.slots);
        this.schedules = new ScheduleRepository(this.connection);
        this.cards = new MedicalCardRepository(this.connection);
        this.records = new MedicalRecordRepository(this.connection);
        this.notifications = new NotificationRepository(this.connection);
        this.favoriteDoctors = new FavoriteDoctorRepository(this.connection);
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

    public Connection connection() {
        return connection;
    }

    public void closeAll() {
        RuntimeException repositoryCloseError = null;
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
            repositoryCloseError = new RuntimeException("Error closing repository resources", e);
        } finally {
            if (ownsConnection) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    if (repositoryCloseError == null) {
                        repositoryCloseError = new RuntimeException("Error closing DB connection", e);
                    } else {
                        repositoryCloseError.addSuppressed(e);
                    }
                }
            }
        }

        if (repositoryCloseError != null) {
            throw repositoryCloseError;
        }
    }

    private static Connection openConnection(String dbUrl) {
        if (dbUrl == null || dbUrl.isBlank()) {
            throw new IllegalArgumentException("Database URL must not be null or blank");
        }
        try {
            return DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }

    private static void enableForeignKeys(Connection connection) {
        try (Statement pragma = connection.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to enable foreign keys", e);
        }
    }

    @Override
    public void close() {
        closeAll();
    }
}
