package com.clinicmanager.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Abstrakcyjna klasa bazowa do obsługi połączenia z bazą danych i operacji CRUD
public abstract class AbstractDatabaseManager<T> implements Repository<T> {
    // Wspólne połączenie z bazą danych dla wszystkich repozytoriów
    protected static final Connection conn;

    static {
        try {
            // Inicjalizacja połączenia z bazą SQLite (ścieżka na sztywno)
            conn = DriverManager.getConnection("jdbc:sqlite:clinic.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect do DB", e);
        }
    }

    public AbstractDatabaseManager(String dbUrl) {
    }

    // Zapisuje encję do bazy i zwraca jej ID
    public abstract int save(T entity);

    // Zamyka połączenie z bazą danych
    public void close() throws SQLException {
        conn.close();
    }
}
