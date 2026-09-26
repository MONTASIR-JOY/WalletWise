package com.walletwise.view;

import com.walletwise.dao.BudgetDAO;
import com.walletwise.dao.Database;
import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.AccountItem;
import com.walletwise.model.Budget;
import com.walletwise.model.TransactionType;
import com.walletwise.util.CurrencyService;
import com.walletwise.util.Notifiable;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardScene implements Notifiable {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();

    @Override
    public void notify(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public VBox getRoot() {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        String month = YearMonth.now().toString();
        Label monthLabel = new Label(month);
        monthLabel.setStyle("-fx-text-fill: #777;");

        String currency = readSetting("currency", "BDT");
        double rate = 1.0;
        String rateError = null;

        if (!currency.equals("BDT")) {
            try {
                rate = CurrencyService.getRate(currency);
            } catch (Exception e) {
                rateError = e.getMessage();
            }
        }

        double income = 0;
        double expense = 0;
        Map<String, Double> spent = new HashMap<>();

        try {
            List<AccountItem> items = txDAO.findByMonth(month);
            for (int i = 0; i < items.size(); i++) {
                AccountItem t = items.get(i);
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

        HBox cards = new HBox(15);
        cards.setAlignment(Pos.CENTER_LEFT);
        VBox incomeCard = makeCard("Income", income * rate, "#27ae60");
        VBox expenseCard = makeCard("Expense", expense * rate, "#c0392b");
        VBox balanceCard = makeCard("Balance", balance * rate, balance >= 0 ? "#2980b9" : "#c0392b");
        cards.getChildren().addAll(incomeCard, expenseCard, balanceCard);

        // responsive: each card is 28% of the container's width
        incomeCard.prefWidthProperty().bind(cards.widthProperty().multiply(0.28));
        expenseCard.prefWidthProperty().bind(cards.widthProperty().multiply(0.28));
        balanceCard.prefWidthProperty().bind(cards.widthProperty().multiply(0.28));

        Label note = new Label();
        if (currency.equals("BDT")) {
            note.setText("Amounts in BDT");
            note.setStyle("-fx-text-fill: #777; -fx-font-size: 11px;");
        } else if (rateError != null) {
            note.setText("Could not fetch " + currency + " rate (" + rateError + ")");
            note.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 11px;");
        } else {
            note.setText(String.format("Converted to %s  (1 BDT = %.6f)", currency, rate));
            note.setStyle("-fx-text-fill: #777; -fx-font-size: 11px;");
        }

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
                    budgetBox.getChildren().add(makeRow(b, used));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VBox root = new VBox(20, title, monthLabel, cards, note, sectionTitle, budgetBox);
        root.setPadding(new Insets(25));

        VBox.setVgrow(budgetBox, Priority.ALWAYS);

        return root;
    }

    private VBox makeCard(String name, double amount, String color) {
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #666;");

        Label amtLbl = new Label(String.format("%.2f", amount));
        amtLbl.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        VBox box = new VBox(6, nameLbl, amtLbl);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        return box;
    }

    private HBox makeRow(Budget b, double used) {
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

        Label detail = new Label(String.format("%.0f / %.0f", used, b.getLimitAmount()));
        detail.setPrefWidth(120);

        HBox row = new HBox(10, catLabel, bar, detail);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private String readSetting(String key, String fallback) {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String v = rs.getString("value");
                rs.close();
                return v;
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fallback;
    }
}