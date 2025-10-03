package com.clinicmanager.repository;

import com.clinicmanager.model.entities.FavoriteDoctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDoctorRepository extends AbstractDatabaseManager<FavoriteDoctor> {
  public FavoriteDoctorRepository(Connection connection) {
    super(connection);
  }

  @Override
  public int save(FavoriteDoctor fav) {
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "INSERT INTO favorite_doctors (patient_id, doctor_id) VALUES (?, ?)")) {
      stmt.setInt(1, fav.patientId());
      stmt.setInt(2, fav.doctorId());
      stmt.executeUpdate();
      try (Statement s = conn.createStatement()) {
        ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
        if (rs2.next()) return rs2.getInt(1);
      }
      throw new RuntimeException("No ID returned for favorite doctor");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(FavoriteDoctor fav) {
    try (PreparedStatement stmt =
        conn.prepareStatement("DELETE FROM favorite_doctors WHERE id = ?")) {
      stmt.setInt(1, fav.id());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void update(FavoriteDoctor fav) {
    // Not needed for this entity
  }

  @Override
  public FavoriteDoctor findById(int id) {
    try (PreparedStatement stmt =
        conn.prepareStatement("SELECT * FROM favorite_doctors WHERE id = ?")) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return new FavoriteDoctor(rs.getInt("id"), rs.getInt("patient_id"), rs.getInt("doctor_id"));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  @Override
  public List<FavoriteDoctor> findAll() {
    List<FavoriteDoctor> list = new ArrayList<>();
    try (Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery("SELECT * FROM favorite_doctors");
      while (rs.next()) {
        list.add(
            new FavoriteDoctor(rs.getInt("id"), rs.getInt("patient_id"), rs.getInt("doctor_id")));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  @Override
  public FavoriteDoctor findByEmail(String email) {
    return null;
  }

  public List<FavoriteDoctor> findByPatientId(int patientId) {
    List<FavoriteDoctor> list = new ArrayList<>();
    try (PreparedStatement stmt =
        conn.prepareStatement("SELECT * FROM favorite_doctors WHERE patient_id = ?")) {
      stmt.setInt(1, patientId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        list.add(
            new FavoriteDoctor(rs.getInt("id"), rs.getInt("patient_id"), rs.getInt("doctor_id")));
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return list;
  }

  public void deleteByPatientAndDoctor(int patientId, int doctorId) {
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "DELETE FROM favorite_doctors WHERE patient_id = ? AND doctor_id = ?")) {
      stmt.setInt(1, patientId);
      stmt.setInt(2, doctorId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isFavorite(int patientId, int doctorId) {
    try (PreparedStatement stmt =
        conn.prepareStatement(
            "SELECT COUNT(*) FROM favorite_doctors WHERE patient_id = ? AND doctor_id = ?")) {
      stmt.setInt(1, patientId);
      stmt.setInt(2, doctorId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return false;
  }
}
