//package com.clinicmanager.repository;
//
//import com.clinicmanager.model.actors.Doctor;
//import com.clinicmanager.model.actors.Patient;
//import com.clinicmanager.model.entities.Appointment;
//import com.clinicmanager.model.entities.Schedule;
//import com.clinicmanager.model.entities.Slot;
//import com.clinicmanager.model.entities.TimeRange;
//import com.clinicmanager.model.enums.AppointmentStatus;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//class RepositoryTransactionIntegrationTest {
//
//    @Test
//    void multipleRepositoriesShareSingleConnectionWithinTransaction() throws Exception {
//        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
//            runSchemaMigrations(connection);
//
//            int appointmentId;
//            try (RepositoryManager repositories = new RepositoryManager(connection)) {
//                connection.setAutoCommit(false);
//                try {
//                    int doctorId = repositories.doctors().save(new Doctor(0, "Dr. Smith", "1980-01-01", "123456789", 0));
//                    int scheduleId = repositories.schedules().save(new Schedule(0, doctorId));
//                    int patientId = repositories.patients().save(new Patient(0, "John Doe", "1990-01-01", "987654321", 0));
//                    int slotId = repositories.slots().save(new Slot(scheduleId, LocalDate.of(2024, 1, 1),
//                            new TimeRange(LocalTime.of(9, 0), LocalTime.of(9, 30))));
//                    appointmentId = repositories.appointments().save(new Appointment(0, patientId, doctorId, slotId,
//                            AppointmentStatus.PENDING, "Checkup"));
//                    connection.commit();
//                } catch (Exception e) {
//                    connection.rollback();
//                    throw e;
//                } finally {
//                    connection.setAutoCommit(true);
//                }
//
//                Appointment stored = repositories.appointments().findById(appointmentId);
//                assertNotNull(stored);
//                assertEquals(AppointmentStatus.PENDING, stored.status());
//                assertEquals(appointmentId, stored.id());
//            }
//        }
//    }
//
//    private void runSchemaMigrations(Connection connection) throws IOException, SQLException {
//        executeSqlScript(connection, "db/migration/V1__init.sql");
//    }
//
//    private void executeSqlScript(Connection connection, String resourcePath) throws IOException, SQLException {
//        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
//            if (inputStream == null) {
//                throw new IllegalStateException("Migration script not found: " + resourcePath);
//            }
//            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
//            for (String rawStatement : sql.split(";")) {
//                String statement = rawStatement.trim();
//                if (!statement.isEmpty()) {
//                    try (Statement stmt = connection.createStatement()) {
//                        stmt.execute(statement);
//                    }
//                }
//            }
//        }
//    }
//}
