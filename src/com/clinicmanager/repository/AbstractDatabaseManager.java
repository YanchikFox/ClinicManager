package com.clinicmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class AbstractDatabaseManager<T> implements Repository<T> {
    protected final Connection conn;

    public AbstractDatabaseManager(String dbUrl) {
        try {
            this.conn = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }

    public void close() throws SQLException {
        conn.close();
    }
}
