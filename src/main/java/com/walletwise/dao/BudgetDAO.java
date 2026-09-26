package com.walletwise.dao;

import com.walletwise.model.Budget;
import com.walletwise.model.Category;
import com.walletwise.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    public void save(Budget b) throws SQLException {
        String sql = "INSERT INTO budgets(category_id, month, limit_amount) VALUES(?, ?, ?) " +
                "ON CONFLICT(category_id, month) DO UPDATE SET limit_amount = excluded.limit_amount";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getCategory().getId());
            ps.setString(2, b.getMonth());
            ps.setDouble(3, b.getLimitAmount());
            ps.executeUpdate();
        }
    }

    public List<Budget> findByMonth(String month) throws SQLException {
        String sql = baseSelect() + " WHERE b.month = ? ORDER BY c.name";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(map(rs));
            }
            rs.close();
        }
        return list;
    }

    public Budget findOne(int categoryId, String month) throws SQLException {
        String sql = baseSelect() + " WHERE b.category_id = ? AND b.month = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, month);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Budget b = map(rs);
                rs.close();
                return b;
            }
            rs.close();
        }
        return null;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private String baseSelect() {
        return "SELECT b.id, b.month, b.limit_amount, " +
                "c.id AS cat_id, c.name AS cat_name, c.type AS cat_type " +
                "FROM budgets b JOIN categories c ON b.category_id = c.id";
    }

    private Budget map(ResultSet rs) throws SQLException {
        Category cat = new Category(
                rs.getInt("cat_id"),
                rs.getString("cat_name"),
                TransactionType.valueOf(rs.getString("cat_type"))
        );
        return new Budget(
                rs.getInt("id"),
                cat,
                rs.getString("month"),
                rs.getDouble("limit_amount")
        );
    }
}