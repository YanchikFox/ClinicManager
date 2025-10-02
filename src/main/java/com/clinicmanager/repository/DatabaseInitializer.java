package com.clinicmanager.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseInitializer {
    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
    private static final String SCHEMA_RESOURCE = "schema.sql";
    private static final String DATA_RESOURCE = "data.sql";

    private DatabaseInitializer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initialize(String dbUrl) {
        Path databasePath = resolveDatabasePath(dbUrl);
        boolean databaseExists = databasePath != null && Files.exists(databasePath);

        if (databasePath != null) {
            try {
                Path parent = databasePath.getParent();
                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to prepare database directory", e);
            }
        }

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            connection.setAutoCommit(false);
            enableForeignKeys(connection);

            try {
                runScript(connection, SCHEMA_RESOURCE);
                if (!databaseExists) {
                    runScript(connection, DATA_RESOURCE);
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(connection);
                throw e instanceof RuntimeException ? (RuntimeException) e
                        : new RuntimeException("Failed to initialize database schema", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    private static void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement pragma = connection.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
    }

    private static void runScript(Connection connection, String resourceName) throws SQLException {
        List<String> statements = loadStatements(resourceName);
        if (statements.isEmpty()) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    private static List<String> loadStatements(String resourceName) {
        try (InputStream inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing SQL resource: " + resourceName);
            }

            String script = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            List<String> statements = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;

            for (char c : script.toCharArray()) {
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                } else if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                }

                if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                    String statement = current.toString().trim();
                    if (!statement.isEmpty()) {
                        statements.add(statement);
                    }
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }

            String trailing = current.toString().trim();
            if (!trailing.isEmpty()) {
                statements.add(trailing);
            }

            return statements;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL resource: " + resourceName, e);
        }
    }

    private static Path resolveDatabasePath(String dbUrl) {
        if (dbUrl == null || !dbUrl.startsWith(SQLITE_URL_PREFIX)) {
            return null;
        }

        String pathPart = dbUrl.substring(SQLITE_URL_PREFIX.length());
        if (pathPart.isBlank() || pathPart.startsWith(":")) {
            return null;
        }

        return Paths.get(pathPart).toAbsolutePath();
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Suppressed intentionally; the original exception will be propagated.
        }
    }
}
