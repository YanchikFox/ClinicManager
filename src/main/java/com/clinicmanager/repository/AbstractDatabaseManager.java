package com.clinicmanager.repository;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDatabaseManager<T> implements Repository<T> {
    protected final Connection conn;

    protected AbstractDatabaseManager(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection must not be null");
        }
        this.conn = connection;
    }

    public abstract int save(T entity);

    public void close() throws SQLException {
        // Repositories share a single connection managed by RepositoryManager.
        // Subclasses may override to release additional resources if needed.
    }
}
