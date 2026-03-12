package com.laundrydesktop.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private static final String APP_DIR = ".laundry-desktop";
    private static final String DB_NAME = "laundry.db";

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        Path appDir = Paths.get(System.getProperty("user.home"), APP_DIR);
        try {
            Files.createDirectories(appDir);
        } catch (Exception e) {
            throw new SQLException("Gagal membuat direktori database: " + appDir, e);
        }
        String jdbcUrl = "jdbc:sqlite:" + appDir.resolve(DB_NAME);
        return DriverManager.getConnection(jdbcUrl);
    }
}
