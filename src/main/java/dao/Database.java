package com.walletwise.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:sqlite:walletwise.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        String categories = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                type TEXT NOT NULL CHECK(type IN ('INCOME','EXPENSE'))
            )
            """;

        String transactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('INCOME','EXPENSE')),
                category_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                note TEXT,
                receipt_path TEXT,
                FOREIGN KEY(category_id) REFERENCES categories(id)
            )
            """;

        String budgets = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL,
                month TEXT NOT NULL,
                limit_amount REAL NOT NULL,
                UNIQUE(category_id, month),
                FOREIGN KEY(category_id) REFERENCES categories(id)
            )
            """;

        String settings = """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            )
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(categories);
            stmt.execute(transactions);
            stmt.execute(budgets);
            stmt.execute(settings);
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}