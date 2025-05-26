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
                "SELECT * FROM schedules WHERE doctor_id = ? LIMIT 1")) {  // LIMIT 1 dla pewności
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Schedule(rs.getInt("id"), rs.getInt("doctor_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null; // brak wyniku
    }


    public List<Slot> getDoctorSlots(int doctorId, LocalDate date) {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT * FROM slots WHERE doctor_id = ? AND date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalTime startTime = rs.getTime("start_time").toLocalTime();
                LocalTime endTime = rs.getTime("end_time").toLocalTime();

                TimeRange timeRange = new TimeRange(startTime, endTime);  // zakładam taki konstruktor

                slots.add(new Slot(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("date").toLocalDate(),
                        timeRange
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return slots;
    }

    public List<Slot> getOutgoingSchedule(int doctorId) {
        List<Slot> slots = new ArrayList<>();
        String sql = "SELECT * FROM slots WHERE doctor_id = ? AND date >= ? ORDER BY date, start_time";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalTime startTime = rs.getTime("start_time").toLocalTime();
                LocalTime endTime = rs.getTime("end_time").toLocalTime();
                TimeRange timeRange = new TimeRange(startTime, endTime);

                slots.add(new Slot(
                        rs.getInt("id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("date").toLocalDate(),
                        timeRange
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return slots;
    }
}