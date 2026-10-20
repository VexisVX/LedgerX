package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.model.Goal;
import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class GoalDao {
    public List<Goal> findAll() {
        List<Goal> l = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT id,title,target_amount,saved_amount,deadline FROM goals ORDER BY id DESC");
             ResultSet r = p.executeQuery()) {
            while (r.next()) l.add(map(r));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return l;
    }

    public void insert(Goal g) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "INSERT INTO goals (title,target_amount,saved_amount,deadline) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, g.getTitle());
            p.setString(2, g.getTargetAmount().toPlainString());
            p.setString(3, g.getSavedAmount().toPlainString());
            p.setString(4, g.getDeadline() != null ? g.getDeadline().toString() : null);
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) g.setId(k.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(Goal g) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "UPDATE goals SET title=?,target_amount=?,saved_amount=?,deadline=? WHERE id=?")) {
            p.setString(1, g.getTitle());
            p.setString(2, g.getTargetAmount().toPlainString());
            p.setString(3, g.getSavedAmount().toPlainString());
            p.setString(4, g.getDeadline() != null ? g.getDeadline().toString() : null);
            p.setLong(5, g.getId());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(long id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM goals WHERE id=?")) {
            p.setLong(1, id); p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Goal map(ResultSet r) throws SQLException {
        Goal g = new Goal();
        g.setId(r.getLong("id"));
        g.setTitle(r.getString("title"));
        g.setTargetAmount(new BigDecimal(r.getString("target_amount")));
        g.setSavedAmount(new BigDecimal(r.getString("saved_amount")));
        String dl = r.getString("deadline");
        if (dl != null) g.setDeadline(LocalDate.parse(dl));
        return g;
    }
}
