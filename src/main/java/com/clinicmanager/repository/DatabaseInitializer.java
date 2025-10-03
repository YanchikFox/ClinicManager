package com.clinicmanager.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.flywaydb.core.Flyway;

public final class DatabaseInitializer {
  private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";
  private static final String MIGRATIONS_LOCATION = "classpath:db/migration";

  private DatabaseInitializer() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void initialize(String dbUrl) {
    String normalizedUrl = dbUrl;
    if (dbUrl == null || dbUrl.isBlank()) {
      throw new IllegalArgumentException("Database URL must not be null or blank");
    }

    Path databasePath = resolveDatabasePath(dbUrl);
    if (databasePath != null) {
      try {
        Path parent = databasePath.getParent();
        if (parent != null && Files.notExists(parent)) {
          Files.createDirectories(parent);
        }
        normalizedUrl = "jdbc:sqlite:" + databasePath.toAbsolutePath();
      } catch (IOException e) {
        throw new RuntimeException("Failed to prepare database directory", e);
      }
    }

    Flyway flyway =
        Flyway.configure()
            .dataSource(normalizedUrl, null, null)
            .locations(MIGRATIONS_LOCATION)
            .load();

    flyway.migrate();
  }

  private static Path resolveDatabasePath(String dbUrl) {
    if (!dbUrl.startsWith(SQLITE_URL_PREFIX)) {
      return null;
    }

    String pathPart = dbUrl.substring(SQLITE_URL_PREFIX.length());
    if (pathPart.isBlank() || pathPart.startsWith(":")) {
      return null;
    }

    return Paths.get(pathPart).toAbsolutePath();
  }
}
