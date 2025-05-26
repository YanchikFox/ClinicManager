package com.clinicmanager.repository;

import com.clinicmanager.model.entitys.MedicalRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordRepository extends AbstractDatabaseManager<MedicalRecord> {
    public MedicalRecordRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public int save(MedicalRecord record) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO medical_records (medical_card_id, doctor_id, date, description) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, record.medicalCardId());
            stmt.setInt(2, record.doctorId());
            stmt.setString(3, record.date().toString());
            stmt.setString(4, record.description());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
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
                "UPDATE medical_records SET medical_card_id = ?, doctor_id = ?, date = ?, description = ? WHERE id = ?")) {
            stmt.setInt(1, record.medicalCardId());
            stmt.setInt(2, record.doctorId());
            stmt.setString(3, record.date().toString());
            stmt.setString(4, record.description());
            stmt.setInt(5, record.id());
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
                return new MedicalRecord(
                        rs.getInt("id"),
                        rs.getInt("medical_card_id"),
                        rs.getInt("doctor_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("description")
                );
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
                list.add(new MedicalRecord(
                        rs.getInt("id"),
                        rs.getInt("medical_card_id"),
                        rs.getInt("doctor_id"),
                        LocalDate.parse(rs.getString("date")),
                        rs.getString("description")
                ));
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
}
