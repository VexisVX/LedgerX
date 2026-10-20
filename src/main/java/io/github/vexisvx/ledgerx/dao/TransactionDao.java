package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.model.Transaction;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionDao {
    private static final String SELECT =
        "SELECT t.id,t.type,t.amount,t.date,t.title,t.description,t.category_id,c.name AS cn " +
        "FROM transactions t LEFT JOIN categories c ON t.category_id=c.id ";

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(SELECT + "ORDER BY t.date DESC, t.id DESC");
             ResultSet r = p.executeQuery()) {
            while (r.next()) list.add(map(r));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public void insert(Transaction t) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "INSERT INTO transactions (type,amount,date,title,description,category_id) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, t.getType().name());
            p.setString(2, t.getAmount().toPlainString());
            p.setString(3, t.getDate().toString());
            p.setString(4, t.getTitle());
            p.setString(5, t.getDescription());
            if (t.getCategoryId() != null) p.setLong(6, t.getCategoryId()); else p.setNull(6, Types.INTEGER);
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) t.setId(k.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void update(Transaction t) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "UPDATE transactions SET type=?,amount=?,date=?,title=?,description=?,category_id=? WHERE id=?")) {
            p.setString(1, t.getType().name());
            p.setString(2, t.getAmount().toPlainString());
            p.setString(3, t.getDate().toString());
            p.setString(4, t.getTitle());
            p.setString(5, t.getDescription());
            if (t.getCategoryId() != null) p.setLong(6, t.getCategoryId()); else p.setNull(6, Types.INTEGER);
            p.setLong(7, t.getId());
            p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(long id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM transactions WHERE id=?")) {
            p.setLong(1, id); p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public BigDecimal sumByType(TransactionType ty) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(SUM(CAST(amount AS REAL)),0) FROM transactions WHERE type=?")) {
            p.setString(1, ty.name());
            try (ResultSet r = p.executeQuery()) { if (r.next()) return BigDecimal.valueOf(r.getDouble(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumByTypeMonth(TransactionType ty, int y, int m) {
        String ym = String.format("%04d-%02d", y, m);
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT COALESCE(SUM(CAST(amount AS REAL)),0) FROM transactions " +
                "WHERE type=? AND strftime('%Y-%m',date)=?")) {
            p.setString(1, ty.name()); p.setString(2, ym);
            try (ResultSet r = p.executeQuery()) { if (r.next()) return BigDecimal.valueOf(r.getDouble(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return BigDecimal.ZERO;
    }

    private Transaction map(ResultSet r) throws SQLException {
        Transaction t = new Transaction();
        t.setId(r.getLong("id"));
        t.setType(TransactionType.valueOf(r.getString("type")));
        t.setAmount(new BigDecimal(r.getString("amount")));
        t.setDate(LocalDate.parse(r.getString("date")));
        t.setTitle(r.getString("title"));
        t.setDescription(r.getString("description"));
        long cid = r.getLong("category_id");
        if (!r.wasNull()) t.setCategoryId(cid);
        t.setCategoryName(r.getString("cn"));
        return t;
    }
}
