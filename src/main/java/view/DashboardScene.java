package com.walletwise.view;

import com.walletwise.dao.BudgetDAO;
import com.walletwise.dao.Database;
import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.Budget;
import com.walletwise.model.Transaction;
import com.walletwise.model.TransactionType;
import com.walletwise.util.CurrencyService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardScene {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    private String currency = "BDT";
    private double rate = 1.0;
    private String rateError = null;

    public VBox getRoot() {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        String month = YearMonth.now().toString();
        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-text-fill: #777;");

        // read currency from settings
        currency = loadSetting("currency", "BDT");

        if (!currency.equals("BDT")) {
            try {
                rate = CurrencyService.getRate(currency);
            } catch (Exception e) {
                rateError = e.getMessage();
                rate = 1.0;
            }
        }

        double income = 0;
        double expense = 0;
        Map<String, Double> spent = new HashMap<>();

        try {
            List<Transaction> txs = txDAO.findByMonth(month);
            for (int i = 0; i < txs.size(); i++) {
                Transaction t = txs.get(i);
                if (t.getType() == TransactionType.INCOME) {
                    income = income + t.getAmount();
                } else {
                    expense = expense + t.getAmount();
                    String cn = t.getCategory().getName();
                    double prev = spent.getOrDefault(cn, 0.0);
                    spent.put(cn, prev + t.getAmount());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double balance = income - expense;

        // convert if needed
        double shownIncome = income * rate;
        double shownExpense = expense * rate;
        double shownBalance = balance * rate;

        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER_LEFT);
        cards.getChildren().add(makeCard("Income", shownIncome, "#27ae60"));
        cards.getChildren().add(makeCard("Expense", shownExpense, "#c0392b"));
        cards.getChildren().add(makeCard("Balance", shownBalance, shownBalance >= 0 ? "#2980b9" : "#c0392b"));

        Label currencyNote = new Label();
        if (currency.equals("BDT")) {
            currencyNote.setText("Amounts in BDT");
        } else if (rateError != null) {
            currencyNote.setText("Could not fetch " + currency + " rate (" + rateError + ")");
            currencyNote.setStyle("-fx-text-fill: #c0392b;");
        } else {
            currencyNote.setText(String.format("Converted to %s  (1 BDT = %.6f)", currency, rate));
        }
        currencyNote.setStyle(currencyNote.getStyle() + "; -fx-text-fill: #777; -fx-font-size: 11px;");

        Label sectionTitle = new Label("Budget Progress");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox budgetBox = new VBox(10);
        try {
            List<Budget> bs = budgetDAO.findByMonth(month);
            if (bs.size() == 0) {
                Label none = new Label("No budgets yet for " + month);
                none.setStyle("-fx-text-fill: #888;");
                budgetBox.getChildren().add(none);
            } else {
                for (int i = 0; i < bs.size(); i++) {
                    Budget b = bs.get(i);
                    double used = spent.getOrDefault(b.getCategory().getName(), 0.0);
                    budgetBox.getChildren().add(makeBudgetRow(b, used));
                }
            }
        } catch (Exception ex) {
            budgetBox.getChildren().add(new Label("Could not load budgets."));
        }

        VBox root = new VBox();
        root.setSpacing(20);
        root.setPadding(new Insets(25));
        root.getChildren().addAll(title, monthLabel, cards, currencyNote, sectionTitle, budgetBox);
        return root;
    }

    private VBox makeCard(String name, double amount, String color) {
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #666;");

        Label amtLbl = new Label(String.format("%.2f", amount));
        amtLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        VBox box = new VBox(6);
        box.getChildren().addAll(nameLbl, amtLbl);
        box.setPadding(new Insets(15));
        box.setPrefWidth(200);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        return box;
    }

    private HBox makeBudgetRow(Budget b, double used) {
        Label catLabel = new Label(b.getCategory().getName());
        catLabel.setPrefWidth(120);

        double ratio = 0;
        if (b.getLimitAmount() > 0) {
            ratio = used / b.getLimitAmount();
        }

        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(300);
        if (ratio > 1) {
            bar.setProgress(1);
        } else {
            bar.setProgress(ratio);
        }

        if (ratio >= 1) {
            bar.setStyle("-fx-accent: #c0392b;");
        } else if (ratio >= 0.8) {
            bar.setStyle("-fx-accent: #e67e22;");
        } else {
            bar.setStyle("-fx-accent: #27ae60;");
        }

        Label detailLabel = new Label(String.format("%.0f / %.0f", used, b.getLimitAmount()));
        detailLabel.setPrefWidth(120);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(catLabel, bar, detailLabel);
        return row;
    }

    private String loadSetting(String key, String fallback) {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("value");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fallback;
    }
}