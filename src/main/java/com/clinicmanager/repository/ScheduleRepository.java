package com.clinicmanager.repository;

import com.clinicmanager.model.entities.Schedule;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.model.entities.TimeRange;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRepository extends AbstractDatabaseManager<Schedule> {
    public ScheduleRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public int save(Schedule s) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO schedules (doctor_id) VALUES (?)")) {
            stmt.setInt(1, s.doctorId());
            stmt.executeUpdate();
            try (Statement st = conn.createStatement()) {
                ResultSet rs2 = st.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next()) return rs2.getInt(1);
            }
            throw new RuntimeException("No ID returned for schedule");
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

    public Schedule findByDoctorId(int doctorId) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM schedules WHERE doctor_id = ? LIMIT 1")) {
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Schedule(rs.getInt("id"), rs.getInt("doctor_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Slot> findSlotsByScheduleAndDate(int scheduleId, LocalDate date) {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT * FROM slots WHERE schedule_id = ? AND date = ? ORDER BY start_time";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            stmt.setString(2, date.toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                slots.add(new Slot(
                        rs.getInt("id"),
                        rs.getInt("schedule_id"),
                        LocalDate.parse(rs.getString("date")),
                        new TimeRange(
                                LocalTime.parse(rs.getString("start_time")),
                                LocalTime.parse(rs.getString("end_time"))
                        )));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return slots;
    }

    public List<Slot> getOutgoingSchedule(int scheduleId) {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT * FROM slots WHERE schedule_id = ? AND date >= ? ORDER BY date, start_time";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, scheduleId);
            stmt.setString(2, LocalDate.now().toString());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                slots.add(new Slot(
                        rs.getInt("id"),
                        rs.getInt("schedule_id"),
                        LocalDate.parse(rs.getString("date")),
                        new TimeRange(
                                LocalTime.parse(rs.getString("start_time")),
                                LocalTime.parse(rs.getString("end_time"))
                        )));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return slots;
    }
}
