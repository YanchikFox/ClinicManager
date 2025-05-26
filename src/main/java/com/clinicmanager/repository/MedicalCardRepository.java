package com.clinicmanager.repository;

import com.clinicmanager.model.entitys.MedicalCard;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalCardRepository extends AbstractDatabaseManager<MedicalCard> {
    public MedicalCardRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public int save(MedicalCard card) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO medical_cards (patient_id) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, card.patientId());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                try (Statement s = conn.createStatement()) {
                    ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                    if (rs2.next()) return rs2.getInt(1);
                }
            }
            throw new RuntimeException("No ID returned for medical card");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(MedicalCard card) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM medical_cards WHERE id = ?")) {
            stmt.setInt(1, card.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(MedicalCard card) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE medical_cards SET patient_id = ? WHERE id = ?")) {
            stmt.setInt(1, card.patientId());
            stmt.setInt(2, card.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MedicalCard findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM medical_cards WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new MedicalCard(rs.getInt("id"), rs.getInt("patient_id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<MedicalCard> findAll() {
        List<MedicalCard> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM medical_cards");
            while (rs.next()) {
                list.add(new MedicalCard(rs.getInt("id"), rs.getInt("patient_id")));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public MedicalCard findByEmail(String email) {
        return null;
    }
}
