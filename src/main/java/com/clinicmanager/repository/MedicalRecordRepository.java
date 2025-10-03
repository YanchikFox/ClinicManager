package com.clinicmanager.repository;

import com.clinicmanager.model.entities.MedicalRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordRepository extends AbstractDatabaseManager<MedicalRecord> {
    public MedicalRecordRepository(Connection connection) {
        super(connection);
    }

    @Override
    public int save(MedicalRecord record) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO medical_records (medical_card_id, doctor_id, date, description, appointment_id) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, record.medicalCardId());
            stmt.setInt(2, record.doctorId());
            stmt.setString(3, record.date().toString());
            stmt.setString(4, record.description());
            if (record.appointmentId() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, record.appointmentId());
            }
            stmt.executeUpdate();
            try (Statement s = conn.createStatement()) {
                ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next()) {
                    return rs2.getInt(1);
                }
            }
            throw new RuntimeException("No ID returned for medical record");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(MedicalRecord record) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM medical_records WHERE id = ?")) {
            stmt.setInt(1, record.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(MedicalRecord record) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE medical_records SET medical_card_id = ?, doctor_id = ?, date = ?, description = ?, appointment_id = ? WHERE id = ?")) {
            stmt.setInt(1, record.medicalCardId());
            stmt.setInt(2, record.doctorId());
            stmt.setString(3, record.date().toString());
            stmt.setString(4, record.description());
            if (record.appointmentId() == null) {
                stmt.setNull(5, Types.INTEGER);
            } else {
                stmt.setInt(5, record.appointmentId());
            }
            stmt.setInt(6, record.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MedicalRecord findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM medical_records WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<MedicalRecord> findAll() {
        List<MedicalRecord> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM medical_records");
            while (rs.next()) {
                list.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public MedicalRecord findByEmail(String email) {
        return null;
    }

    public List<MedicalRecord> findByMedicalCardId(int medicalCardId) {
        List<MedicalRecord> records = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM medical_records WHERE medical_card_id = ?")) {
            stmt.setInt(1, medicalCardId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public MedicalRecord findByAppointmentId(int appointmentId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM medical_records WHERE appointment_id = ?")) {
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean existsForAppointment(int appointmentId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT 1 FROM medical_records WHERE appointment_id = ?")) {
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MedicalRecord mapRecord(ResultSet rs) throws SQLException {
        Integer appointmentId = null;
        Object appointmentObj = rs.getObject("appointment_id");
        if (appointmentObj != null) {
            appointmentId = ((Number) appointmentObj).intValue();
        }
        return new MedicalRecord(
                rs.getInt("id"),
                rs.getInt("medical_card_id"),
                rs.getInt("doctor_id"),
                LocalDate.parse(rs.getString("date")),
                rs.getString("description"),
                appointmentId);
    }
}
