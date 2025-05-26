package com.clinicmanager.repository;

import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.repository.PatientRepository;

import java.sql.*;
import java.util.*;

public class DoctorRepository extends AbstractDatabaseManager<Doctor> {
    private final PatientRepository patientRepository;
    private final SlotRepository slotRepository;

    public DoctorRepository(String dbUrl, PatientRepository patientRepository, SlotRepository slotRepository) {
        super(dbUrl);
        this.patientRepository = patientRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public int save(Doctor d) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO doctors (name, date_of_birth, phone_number, schedule_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, d.name());
            stmt.setString(2, d.dateOfBirth());
            stmt.setString(3, d.phoneNumber());
            stmt.setInt(4, d.scheduleId());
            stmt.executeUpdate();
            try (Statement s = conn.createStatement()) {
                ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next()) return rs2.getInt(1);
            }
            throw new RuntimeException("No ID returned for doctor");
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
                return new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getInt("schedule_id"),
                        patientRepository,
                        slotRepository
                );
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
                list.add(new Doctor(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("date_of_birth"),
                        rs.getString("phone_number"),
                        rs.getInt("schedule_id"),
                        patientRepository,
                        slotRepository
                ));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public Doctor findByEmail(String email) { return null; }

}

