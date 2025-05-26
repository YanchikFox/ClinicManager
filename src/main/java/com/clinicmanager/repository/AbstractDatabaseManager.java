package com.clinicmanager.repository;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class AbstractDatabaseManager<T> implements Repository<T> {
    protected static final Connection conn;

    static {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:clinic.db"); // hardcoded
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }

    public AbstractDatabaseManager(String dbUrl) {
        // No more dbUrl!
    }

    public abstract int save(T entity);

    public void close() throws SQLException {
        conn.close();
    }
}