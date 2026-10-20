package io.github.vexisvx.ledgerx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:ledgerx.db";
    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            try (var st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public static void close() {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
    }
}
