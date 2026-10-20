package io.github.vexisvx.ledgerx.dao;

import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.DatabaseManager;

import java.sql.*;
import java.util.*;

public class CategoryDao {
    public List<Category> findAll() {
        List<Category> l = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT id,name,name_en,type,icon FROM categories ORDER BY type,name");
             ResultSet r = p.executeQuery()) {
            while (r.next()) l.add(map(r));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return l;
    }

    public List<Category> findByType(TransactionType ty) {
        List<Category> l = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "SELECT id,name,name_en,type,icon FROM categories WHERE type=? ORDER BY name")) {
            p.setString(1, ty.name());
            try (ResultSet r = p.executeQuery()) { while (r.next()) l.add(map(r)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return l;
    }

    public void insert(Category cat) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(
                "INSERT INTO categories (name,name_en,type,icon) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, cat.getName());
            p.setString(2, cat.getNameEn());
            p.setString(3, cat.getType().name());
            p.setString(4, cat.getIcon());
            p.executeUpdate();
            try (ResultSet k = p.getGeneratedKeys()) { if (k.next()) cat.setId(k.getLong(1)); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void delete(long id) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM categories WHERE id=?")) {
            p.setLong(1, id); p.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Category map(ResultSet r) throws SQLException {
        Category c = new Category();
        c.setId(r.getLong("id"));
        c.setName(r.getString("name"));
        c.setNameEn(r.getString("name_en"));
        c.setType(TransactionType.valueOf(r.getString("type")));
        c.setIcon(r.getString("icon"));
        return c;
    }
}
