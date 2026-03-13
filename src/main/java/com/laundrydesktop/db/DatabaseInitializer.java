package com.laundrydesktop.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public void initialize() {
        try (Connection connection = DatabaseManager.getConnection(); Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT,
                    address TEXT,
                    note TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS services (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS speeds (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    duration_hours INTEGER NOT NULL DEFAULT 48
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS units (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS service_prices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    service_name TEXT NOT NULL,
                    speed_name TEXT NOT NULL,
                    unit_name TEXT NOT NULL,
                    price INTEGER NOT NULL,
                    UNIQUE(service_name, speed_name, unit_name)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS business_profile (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    business_name TEXT NOT NULL,
                    address TEXT,
                    owner_name TEXT,
                    phone TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    invoice_no TEXT NOT NULL UNIQUE,
                    customer_id INTEGER NOT NULL,
                    service_name TEXT NOT NULL,
                    speed_name TEXT NOT NULL,
                    unit_name TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    unit_price INTEGER NOT NULL,
                    total_price INTEGER NOT NULL,
                    order_status TEXT NOT NULL,
                    payment_status TEXT NOT NULL,
                    payment_method TEXT,
                    down_payment INTEGER NOT NULL DEFAULT 0,
                    paid_amount INTEGER NOT NULL DEFAULT 0,
                    note TEXT NOT NULL DEFAULT '',
                    order_date TEXT NOT NULL,
                    estimate_done TEXT NOT NULL,
                    FOREIGN KEY(customer_id) REFERENCES customers(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payment_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    invoice_no TEXT NOT NULL,
                    payment_date TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    payment_method TEXT,
                    note TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY(invoice_no) REFERENCES orders(invoice_no)
                )
            """);

            try {
                stmt.execute("ALTER TABLE orders ADD COLUMN note TEXT NOT NULL DEFAULT ''");
            } catch (SQLException ignored) {
                // Kolom sudah ada pada database lama/baru.
            }
            try {
                stmt.execute("ALTER TABLE speeds ADD COLUMN duration_hours INTEGER NOT NULL DEFAULT 48");
            } catch (SQLException ignored) {
                // Kolom sudah ada pada database lama/baru.
            }

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_payment_tx_date ON payment_transactions(payment_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_payment_tx_invoice ON payment_transactions(invoice_no)");

            seedDefaults(stmt);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal inisialisasi database", e);
        }
    }

    private void seedDefaults(Statement stmt) throws SQLException {
        stmt.execute("""
            INSERT OR IGNORE INTO users(username, password, role)
            VALUES ('admin', 'admin123', 'ADMIN')
        """);

        stmt.execute("""
            INSERT OR IGNORE INTO services(name)
            VALUES ('Cuci'), ('Setrika'), ('Cuci + Setrika'), ('Dry Clean')
        """);

        stmt.execute("""
            INSERT OR IGNORE INTO speeds(name, duration_hours)
            VALUES ('Normal', 48), ('Express', 24), ('Super Express', 6)
        """);
        stmt.execute("UPDATE speeds SET duration_hours = 48 WHERE lower(name) = 'normal'");
        stmt.execute("UPDATE speeds SET duration_hours = 24 WHERE lower(name) = 'express'");
        stmt.execute("UPDATE speeds SET duration_hours = 6 WHERE lower(name) = 'super express'");

        stmt.execute("""
            INSERT OR IGNORE INTO units(name)
            VALUES ('Kg'), ('Pcs'), ('Item'), ('Meter')
        """);

        stmt.execute("""
            INSERT OR IGNORE INTO service_prices(service_name, speed_name, unit_name, price)
            VALUES
            ('Cuci + Setrika', 'Normal', 'Kg', 7000),
            ('Cuci + Setrika', 'Express', 'Kg', 10000),
            ('Dry Clean', 'Normal', 'Pcs', 15000)
        """);

        stmt.execute("""
            INSERT OR IGNORE INTO business_profile(id, business_name, address, owner_name, phone)
            VALUES (1, 'WashManager Laundry', '', '', '')
        """);
    }
}
