package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.sql.*;

public class DatabaseInitializer {
    public static void initialize() {
        try (Connection c = DatabaseManager.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, " +
                "name_en TEXT, type TEXT NOT NULL, icon TEXT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, " +
                "amount TEXT NOT NULL, date TEXT NOT NULL, title TEXT NOT NULL, " +
                "description TEXT, category_id INTEGER)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS budgets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, category_id INTEGER NOT NULL UNIQUE, " +
                "monthly_limit TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, " +
                "target_amount TEXT NOT NULL, saved_amount TEXT NOT NULL, deadline TEXT)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS recurring_transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, " +
                "amount TEXT NOT NULL, title TEXT NOT NULL, description TEXT, " +
                "category_id INTEGER, frequency TEXT NOT NULL, next_date TEXT NOT NULL, " +
                "end_date TEXT, active INTEGER DEFAULT 1)");

            // add name_en column if missing (old databases)
            boolean hasNameEn = false;
            try (ResultSet rs = s.executeQuery("PRAGMA table_info(categories)")) {
                while (rs.next()) {
                    if ("name_en".equalsIgnoreCase(rs.getString("name"))) {
                        hasNameEn = true;
                        break;
                    }
                }
            }
            if (!hasNameEn) {
                s.executeUpdate("ALTER TABLE categories ADD COLUMN name_en TEXT");
            }

            try (ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Store ENGLISH keys only. Translation happens at display via i18n.
                    insert(s, "salary",        "Salary",        "INCOME",  "\uD83D\uDCB0"); // 💰
                    insert(s, "food",          "Food",          "EXPENSE", "\uD83C\uDF54"); // 🍔
                    insert(s, "transport",     "Transport",     "EXPENSE", "\uD83D\uDE97"); // 🚗
                    insert(s, "housing",       "Housing",       "EXPENSE", "\uD83C\uDFE0"); // 🏠
                    insert(s, "entertainment", "Entertainment", "EXPENSE", "\uD83C\uDFAE"); // 🎮
                    insert(s, "shopping",      "Shopping",      "EXPENSE", "\uD83D\uDECD"); // 🛍
                    insert(s, "health",        "Health",        "EXPENSE", "\uD83C\uDFE5"); // 🏥
                    insert(s, "bills",         "Bills",         "EXPENSE", "\uD83D\uDCC4"); // 📄
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    private static void insert(Statement s, String name, String nameEn,
                               String type, String icon) throws SQLException {
        try (PreparedStatement p = s.getConnection().prepareStatement(
                "INSERT INTO categories (name,name_en,type,icon) VALUES (?,?,?,?)")) {
            p.setString(1, name);
            p.setString(2, nameEn);
            p.setString(3, type);
            p.setString(4, icon);
            p.executeUpdate();
        }
    }
}
