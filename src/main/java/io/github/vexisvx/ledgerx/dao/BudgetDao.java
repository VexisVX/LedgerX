package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.model.Budget;
import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class BudgetDao {
    public List<Budget> findAll() {
        List<Budget> l = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT b.id,b.category_id,c.name AS cn,b.monthly_limit " +
                "FROM budgets b JOIN categories c ON b.category_id=c.id ORDER BY c.name");
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                Budget b = new Budget();
                b.setId(r.getLong("id"));
                b.setCategoryId(r.getLong("category_id"));
                b.setCategoryName(r.getString("cn"));
                b.setMonthlyLimit(new BigDecimal(r.getString("monthly_limit")));
                l.add(b);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return l;
    }

    public void upsert(long catId, BigDecimal limit) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "INSERT INTO budgets (category_id,monthly_limit) VALUES (?,?) " +
                "ON CONFLICT(category_id) DO UPDATE SET monthly_limit=excluded.monthly_limit")) {
            p.setLong(1, catId); p.setString(2, limit.toPlainString());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(long id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM budgets WHERE id=?")) {
            p.setLong(1, id); p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
