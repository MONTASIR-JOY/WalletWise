package com.walletwise.dao;

import com.walletwise.model.AccountItem;
import com.walletwise.model.Category;
import com.walletwise.model.Expense;
import com.walletwise.model.Income;
import com.walletwise.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public AccountItem insert(AccountItem t) throws SQLException {
        String sql = "INSERT INTO transactions(amount, type, category_id, date, note, receipt_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getType().name());
            ps.setInt(3, t.getCategory().getId());
            ps.setString(4, t.getDate().toString());
            ps.setString(5, t.getNote());
            ps.setString(6, null);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                t.setId(keys.getInt(1));
            }
            keys.close();
        }
        return t;
    }

    public List<AccountItem> findAll() throws SQLException {
        String sql = baseSelect() + " ORDER BY t.date DESC, t.id DESC";
        List<AccountItem> list = new ArrayList<>();
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<AccountItem> findByMonth(String month) throws SQLException {
        String sql = baseSelect() + " WHERE substr(t.date, 1, 7) = ? ORDER BY t.date DESC, t.id DESC";
        List<AccountItem> list = new ArrayList<>();
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

    public void update(AccountItem t) throws SQLException {
        String sql = "UPDATE transactions SET amount=?, type=?, category_id=?, date=?, note=?, receipt_path=? WHERE id=?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, t.getAmount());
            ps.setString(2, t.getType().name());
            ps.setInt(3, t.getCategory().getId());
            ps.setString(4, t.getDate().toString());
            ps.setString(5, t.getNote());
            ps.setString(6, null);
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

    private AccountItem map(ResultSet rs) throws SQLException {
        Category cat = new Category(
                rs.getInt("cat_id"),
                rs.getString("cat_name"),
                TransactionType.valueOf(rs.getString("cat_type"))
        );

        int id = rs.getInt("id");
        double amt = rs.getDouble("amount");
        String typeStr = rs.getString("type");
        LocalDate date = LocalDate.parse(rs.getString("date"));
        String note = rs.getString("note");

        if (typeStr.equals("INCOME")) {
            return new Income(id, amt, cat, date, note);
        } else {
            return new Expense(id, amt, cat, date, note);
        }
    }
}