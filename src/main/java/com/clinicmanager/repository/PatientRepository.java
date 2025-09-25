package com.clinicmanager.repository;

import com.clinicmanager.model.actors.Patient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientRepository extends AbstractDatabaseManager<Patient> {
    public PatientRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public int save(Patient patient) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO patients (name, date_of_birth, phone_number, medical_card_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, patient.name());
            stmt.setString(2, patient.dateOfBirth());
            stmt.setString(3, patient.phoneNumber());
            stmt.setInt(4, patient.medicalCardId());
            stmt.executeUpdate();
            try (Statement s = conn.createStatement()) {
                ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next())
                    return rs2.getInt(1);
            }
            throw new RuntimeException("No ID returned for patient");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save patient: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Patient patient) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM patients WHERE id = ?")) {
            stmt.setInt(1, patient.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete patient", e);
        }
    }

    @Override
    public void update(Patient patient) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE patients SET name = ?, date_of_birth = ?, phone_number = ?, medical_card_id = ? WHERE id = ?")) {
            stmt.setString(1, patient.name());
            stmt.setString(2, patient.dateOfBirth());
            stmt.setString(3, patient.phoneNumber());
            stmt.setInt(4, patient.medicalCardId());
            stmt.setInt(5, patient.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update patient", e);
        }
    }

    @Override
    public Patient findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM patients WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getInt("medical_card_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
        return null;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM patients");
            while (rs.next()) {
                list.add(new Patient(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getInt("medical_card_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
        return list;
    }

    @Override
    public Patient findByEmail(String email) {
        return null; // Not used
    }

    public List<Integer> getPatientIdsOfDoctor(int doctorId) {
        List<Integer> patientIds = new ArrayList<>();
        String sql = """
                    SELECT DISTINCT p.id
                    FROM patients p
                    JOIN appointments a ON p.id = a.patient_id
                    WHERE a.doctor_id = ?
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                patientIds.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get patient IDs of doctor", e);
        }
        return patientIds;
    }

}
