package com.clinicmanager.repository;

import com.clinicmanager.model.entitys.Slot;
import com.clinicmanager.model.entitys.TimeRange;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SlotRepository extends AbstractDatabaseManager<Slot> {
    public SlotRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public void save(Slot slot) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO slots (id, schedule_id, date, start_time, end_time) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, slot.id());
            stmt.setInt(2, slot.scheduleId());
            stmt.setString(3, slot.date().toString());
            stmt.setString(4, slot.timeRange().start().toString());
            stmt.setString(5, slot.timeRange().end().toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Slot slot) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM slots WHERE id = ?")) {
            stmt.setInt(1, slot.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Slot slot) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE slots SET schedule_id = ?, date = ?, start_time = ?, end_time = ? WHERE id = ?")) {
            stmt.setInt(1, slot.scheduleId());
            stmt.setString(2, slot.date().toString());
            stmt.setString(3, slot.timeRange().start().toString());
            stmt.setString(4, slot.timeRange().end().toString());
            stmt.setInt(5, slot.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Slot findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM slots WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Slot(
                        rs.getInt("id"),
                        rs.getInt("schedule_id"),
                        LocalDate.parse(rs.getString("date")),
                        new TimeRange(
                                LocalTime.parse(rs.getString("start_time")),
                                LocalTime.parse(rs.getString("end_time"))
                        )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<Slot> findAll() {
        List<Slot> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM slots");
            while (rs.next()) {
                list.add(new Slot(
                        rs.getInt("id"),
                        rs.getInt("schedule_id"),
                        LocalDate.parse(rs.getString("date")),
                        new TimeRange(
                                LocalTime.parse(rs.getString("start_time")),
                                LocalTime.parse(rs.getString("end_time"))
                        )
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Slot findByEmail(String email) {
        return null;
    }


}
