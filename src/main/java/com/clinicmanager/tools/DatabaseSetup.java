package com.clinicmanager.tools;

import com.clinicmanager.repository.DatabaseInitializer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DatabaseSetup {
    private static final String SQLITE_URL_PREFIX = "jdbc:sqlite:";

    private DatabaseSetup() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void main(String[] args) {
        String target = args.length > 0 ? args[0] : "clinic.db";
        String dbUrl = target.startsWith(SQLITE_URL_PREFIX) ? target : SQLITE_URL_PREFIX + target;

        Path databasePath = resolveDatabasePath(dbUrl);
        boolean existedBefore = databasePath != null && Files.exists(databasePath);

        DatabaseInitializer.initialize(dbUrl);

        if (databasePath != null) {
            String message = existedBefore ?
                    "Database already existed at " + databasePath :
                    "Database created at " + databasePath;
            System.out.println(message);
        } else {
            System.out.println("Database initialized using URL: " + dbUrl);
        }
    }

    private static Path resolveDatabasePath(String dbUrl) {
        String path = dbUrl.substring(SQLITE_URL_PREFIX.length());
        if (path.isBlank() || path.startsWith(":")) {
            return null;
        }
        return Paths.get(path).toAbsolutePath();
    }
}
