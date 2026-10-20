package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.model.RecurringTransaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class RecurringDao {

    public List<RecurringTransaction> findAll() {
        List<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT r.*, c.name AS cn FROM recurring_transactions r " +
                     "LEFT JOIN categories c ON r.category_id = c.id ORDER BY r.next_date";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public List<RecurringTransaction> findDue(LocalDate today) {
        List<RecurringTransaction> list = new ArrayList<>();
        String sql = "SELECT r.*, c.name AS cn FROM recurring_transactions r " +
                     "LEFT JOIN categories c ON r.category_id = c.id " +
                     "WHERE r.active = 1 AND r.next_date <= ? " +
                     "AND (r.end_date IS NULL OR r.end_date >= r.next_date)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, today.toString());
            try (ResultSet r = p.executeQuery()) { while (r.next()) list.add(map(r)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public void insert(RecurringTransaction t) {
        String sql = "INSERT INTO recurring_transactions " +
            "(type,amount,title,description,category_id,frequency,next_date,end_date,active) " +
            "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, t);
            p.setInt(9, t.isActive() ? 1 : 0);
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) t.setId(k.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(RecurringTransaction t) {
        String sql = "UPDATE recurring_transactions SET " +
            "type=?,amount=?,title=?,description=?,category_id=?,frequency=?,next_date=?,end_date=?,active=? " +
            "WHERE id=?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, t);
            p.setInt(9, t.isActive() ? 1 : 0);
            p.setLong(10, t.getId());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(long id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM recurring_transactions WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private void bind(PreparedStatement p, RecurringTransaction t) throws SQLException {
        p.setString(1, t.getType().name());
        p.setString(2, t.getAmount().toPlainString());
        p.setString(3, t.getTitle());
        p.setString(4, t.getDescription());
        if (t.getCategoryId() != null) p.setLong(5, t.getCategoryId()); else p.setNull(5, Types.INTEGER);
        p.setString(6, t.getFrequency().name());
        p.setString(7, t.getNextDate().toString());
        p.setString(8, t.getEndDate() != null ? t.getEndDate().toString() : null);
    }

    private RecurringTransaction map(ResultSet r) throws SQLException {
        RecurringTransaction t = new RecurringTransaction();
        t.setId(r.getLong("id"));
        t.setType(TransactionType.valueOf(r.getString("type")));
        t.setAmount(new BigDecimal(r.getString("amount")));
        t.setTitle(r.getString("title"));
        t.setDescription(r.getString("description"));
        long cid = r.getLong("category_id");
        if (!r.wasNull()) t.setCategoryId(cid);
        t.setCategoryName(r.getString("cn"));
        t.setFrequency(RecurringTransaction.Frequency.valueOf(r.getString("frequency")));
        t.setNextDate(LocalDate.parse(r.getString("next_date")));
        String end = r.getString("end_date");
        if (end != null) t.setEndDate(LocalDate.parse(end));
        t.setActive(r.getInt("active") == 1);
        return t;
    }
}
