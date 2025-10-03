package com.clinicmanager.repository;

import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository extends AbstractDatabaseManager<Account> {
    public AccountRepository(Connection connection) {
        super(connection);
    }

    @Override
    public int save(Account acc) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO accounts (email, password_hash, role, owner_id) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, acc.email());
            stmt.setString(2, acc.passwordHash());
            stmt.setString(3, acc.role().name());
            stmt.setInt(4, acc.ownerId());
            stmt.executeUpdate();
            try (Statement s = conn.createStatement()) {
                ResultSet rs2 = s.executeQuery("SELECT last_insert_rowid()");
                if (rs2.next())
                    return rs2.getInt(1);
            }
            throw new RuntimeException("No ID returned for account");
        } catch (SQLException e) {
            // Include the original SQL error message for diagnostics
            throw new RuntimeException("Failed to save account: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Account acc) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
            stmt.setInt(1, acc.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete account", e);
        }
    }

    @Override
    public void update(Account acc) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE accounts SET email = ?, password_hash = ?, role = ?, owner_id = ? WHERE id = ?")) {
            stmt.setString(1, acc.email());
            stmt.setString(2, acc.passwordHash());
            stmt.setString(3, acc.role().name());
            stmt.setInt(4, acc.ownerId());
            stmt.setInt(5, acc.id());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update account", e);
        }
    }

    @Override
    public Account findById(int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapAccount(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
        return null;
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts");
            while (rs.next()) {
                list.add(mapAccount(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
        return list;
    }

    @Override
    public Account findByEmail(String email) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapAccount(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error", e);
        }
        return null;
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")),
                rs.getInt("owner_id"),
                true // the password is already hashed in the database
        );
    }
}
