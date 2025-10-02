package com.clinicmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractDatabaseManager<T> implements Repository<T> {
    protected final Connection conn;

    public AbstractDatabaseManager(String dbUrl) {
        try {
            this.conn = DriverManager.getConnection(dbUrl);
            enableForeignKeys(this.conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to DB", e);
        }
    }

    public abstract int save(T entity);

    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    private void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement pragma = connection.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
    }
}
