package com.clinicmanager.repository;

import com.clinicmanager.model.entitys.Schedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository extends AbstractDatabaseManager<Schedule> {
    public ScheduleRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public void save(Schedule s) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO schedules (id, doctor_id) VALUES (?, ?)")) {
            stmt.setInt(1, s.id());
            stmt.setInt(2, s.doctorId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Schedule s) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM schedules WHERE id = ?")) {
            stmt.setInt(1, s.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Schedule s) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE schedules SET doctor_id = ? WHERE id = ?")) {
            stmt.setInt(1, s.doctorId());
            stmt.setInt(2, s.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Schedule findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM schedules WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Schedule(rs.getInt("id"), rs.getInt("doctor_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Schedule> findAll() {
        List<Schedule> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM schedules");
            while (rs.next()) {
                list.add(new Schedule(rs.getInt("id"), rs.getInt("doctor_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Schedule findByEmail(String email) {
        return null;
    }
}
