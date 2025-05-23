package com.clinicmanager.repository;

import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.repository.PatientRepository;

import java.sql.*;
import java.util.*;

public class DoctorRepository extends AbstractDatabaseManager<Doctor> {
    public DoctorRepository(String dbUrl) { super(dbUrl); }

    @Override
    public void save(Doctor d) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO doctors (id, name, date_of_birth, phone_number, schedule_id) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, d.id());
            stmt.setString(2, d.name());
            stmt.setString(3, d.dateOfBirth());
            stmt.setString(4, d.phoneNumber());
            stmt.setInt(5, d.scheduleId());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(Doctor d) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM doctors WHERE id = ?")) {
            stmt.setInt(1, d.id());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Doctor d) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE doctors SET name = ?, date_of_birth = ?, phone_number = ?, schedule_id = ? WHERE id = ?")) {
            stmt.setString(1, d.name());
            stmt.setString(2, d.dateOfBirth());
            stmt.setString(3, d.phoneNumber());
            stmt.setInt(4, d.scheduleId());
            stmt.setInt(5, d.id());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Doctor findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM doctors WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("date_of_birth"),
                        rs.getString("phone_number"), rs.getInt("schedule_id"));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM doctors");
            while (rs.next()) {
                list.add(new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("date_of_birth"),
                        rs.getString("phone_number"), rs.getInt("schedule_id")));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public Doctor findByEmail(String email) { return null; }

    public List<Patient> getPatientsOfDoctor(int doctorId) {
    List<Patient> patients = new ArrayList<>();
    String sql = """
        SELECT p.id, p.name, p.date_of_birth, p.phone_number, p.medical_card_id
        FROM patients p
        JOIN appointments a ON p.id = a.patient_id
        WHERE a.doctor_id = ?
    """;
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Patient patient = new Patient(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("date_of_birth"),
                rs.getString("phone_number"),
                rs.getInt("medical_card_id")
            );
            patients.add(patient);
        }
    } catch (SQLException e) {
        throw new RuntimeException("Failed to get patients of doctor", e);
    }
    return patients;
}

}

