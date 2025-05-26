package com.clinicmanager.repository;

import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.enums.AppointmentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository extends AbstractDatabaseManager<Appointment> {
    public AppointmentRepository(String dbUrl) { super(dbUrl); }

    @Override
    public int save(Appointment a) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO appointments (patient_id, doctor_id, slot_id, status) VALUES (?, ?, ?, ?)") ) {
            stmt.setInt(1, a.patientId());
            stmt.setInt(2, a.doctorId());
            stmt.setInt(3, a.slotId());
            stmt.setString(4, a.status().name());
            stmt.executeUpdate();
            try (Statement s = conn.createStatement()) {
                ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next()) {
                    return rs2.getInt(1);
                }
            }
            throw new RuntimeException("No ID returned for appointment");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(Appointment a) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM appointments WHERE id = ?")) {
            stmt.setInt(1, a.id());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void update(Appointment a) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE appointments SET patient_id = ?, doctor_id = ?, slot_id = ?, status = ? WHERE id = ?")) {
            stmt.setInt(1, a.patientId());
            stmt.setInt(2, a.doctorId());
            stmt.setInt(3, a.slotId());
            stmt.setString(4, a.status().name());
            stmt.setInt(5, a.id());
            stmt.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Appointment findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM appointments WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("slot_id"),
                        AppointmentStatus.valueOf(rs.getString("status")));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return null;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM appointments");
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("slot_id"),
                        AppointmentStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public Appointment findByEmail(String email) {
        return null;
    }


}
