package com.walletwise.dao;

import com.walletwise.model.Category;
import com.walletwise.model.TransactionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    public Category insert(Category c) throws SQLException {
        String sql = "INSERT INTO categories(name, type) VALUES(?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getType().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        }
        return c;
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name, type FROM categories ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Category> findByType(TransactionType type) throws SQLException {
        String sql = "SELECT id, name, type FROM categories WHERE type = ? ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Category findById(int id) throws SQLException {
        String sql = "SELECT id, name, type FROM categories WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                TransactionType.valueOf(rs.getString("type"))
        );
    }
}