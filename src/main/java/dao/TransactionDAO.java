package com.walletwise.dao;

import com.walletwise.model.Category;
import com.walletwise.model.Transaction;
import com.walletwise.model.TransactionType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public Transaction insert(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions(amount, type, category_id, date, note, receipt_path) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getType().name());
            ps.setInt(3, t.getCategory().getId());
            ps.setString(4, t.getDate().toString());
            ps.setString(5, t.getNote());
            ps.setString(6, t.getReceiptPath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
        }
        return t;
    }

    public List<Transaction> findAll() throws SQLException {
        String sql = baseSelect() + " ORDER BY t.date DESC, t.id DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Transaction> findByMonth(String month) throws SQLException {
        String sql = baseSelect() + " WHERE substr(t.date, 1, 7) = ? ORDER BY t.date DESC, t.id DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void update(Transaction t) throws SQLException {
        String sql = "UPDATE transactions SET amount=?, type=?, category_id=?, " +
                "date=?, note=?, receipt_path=? WHERE id=?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getType().name());
            ps.setInt(3, t.getCategory().getId());
            ps.setString(4, t.getDate().toString());
            ps.setString(5, t.getNote());
            ps.setString(6, t.getReceiptPath());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private String baseSelect() {
        return "SELECT t.id, t.amount, t.type, t.date, t.note, t.receipt_path, " +
                "c.id AS cat_id, c.name AS cat_name, c.type AS cat_type " +
                "FROM transactions t JOIN categories c ON t.category_id = c.id";
    }

    private Transaction map(ResultSet rs) throws SQLException {
        Category cat = new Category(
                rs.getInt("cat_id"),
                rs.getString("cat_name"),
                TransactionType.valueOf(rs.getString("cat_type"))
        );
        return new Transaction(
                rs.getInt("id"),
                rs.getDouble("amount"),
                TransactionType.valueOf(rs.getString("type")),
                cat,
                LocalDate.parse(rs.getString("date")),
                rs.getString("note"),
                rs.getString("receipt_path")
        );
    }
}