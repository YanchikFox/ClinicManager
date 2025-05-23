package com.clinicmanager.repository;

import com.clinicmanager.model.actors.Account;
import com.clinicmanager.model.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRepository extends AbstractDatabaseManager<Account> {
    public AccountRepository(String dbUrl) {
        super(dbUrl);
    }

    @Override
    public void save(Account acc) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO accounts (email, password_hash, role, owner_id) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, acc.email());
            stmt.setString(2, acc.passwordHash());
            stmt.setString(3, acc.role().name());
            stmt.setInt(4, acc.ownerId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save account", e);
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
                true // пароль уже захеширован в базе
        );
    }
}
