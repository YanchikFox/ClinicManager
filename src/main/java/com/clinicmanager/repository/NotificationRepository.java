package com.clinicmanager.repository;

import com.clinicmanager.model.entitys.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository extends AbstractDatabaseManager<Notification> {
    public NotificationRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public void save(Notification n) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO notifications (id, person_id, message, timestamp, read) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, n.id());
            stmt.setInt(2, n.personId());
            stmt.setString(3, n.message());
            stmt.setString(4, n.timestamp().toString());
            stmt.setBoolean(5, n.isRead());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Notification n) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            stmt.setInt(1, n.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Notification n) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET person_id = ?, message = ?, timestamp = ?, read = ? WHERE id = ?")) {
            stmt.setInt(1, n.personId());
            stmt.setString(2, n.message());
            stmt.setString(3, n.timestamp().toString());
            stmt.setBoolean(4, n.isRead());
            stmt.setInt(5, n.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Notification findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM notifications WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Notification(
                        rs.getInt("id"),
                        rs.getInt("person_id"),
                        rs.getString("message"),
                        LocalDateTime.parse(rs.getString("timestamp")),
                        rs.getBoolean("read")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // fetches list of all notifications for all persons
    @Override
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM notifications");
            while (rs.next()) {
                list.add(new Notification(
                        rs.getInt("id"),
                        rs.getInt("person_id"),
                        rs.getString("message"),
                        LocalDateTime.parse(rs.getString("timestamp")),
                        rs.getBoolean("read")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Notification findByEmail(String email) {
        return null;
    }
}