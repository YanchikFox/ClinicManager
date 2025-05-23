package com.clinicmanager.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize(String dbUrl, String schemaFilePath) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            String sql = Files.readString(Path.of(schemaFilePath));
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Failed to read schema file", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DB", e);
        }
    }
}
