package com.walletwise.view;

import com.walletwise.dao.Database;
import com.walletwise.util.CurrencyService;
import com.walletwise.util.PinService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SettingsScene {

    private final ComboBox<String> currencyBox = new ComboBox<>();
    private final Label rateLabel = new Label("");

    public VBox getRoot() {
        Label title = new Label("Settings");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // --- currency section ---
        currencyBox.getItems().addAll("BDT", "USD", "EUR", "GBP", "INR", "JPY");
        currencyBox.setValue(loadSetting("currency", "BDT"));
        currencyBox.setPrefWidth(120);

        Button testBtn = new Button("Test Rate");
        testBtn.setOnAction(e -> testRate());

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            saveSetting("currency", currencyBox.getValue());
            alert(Alert.AlertType.INFORMATION, "Saved", "Currency set to " + currencyBox.getValue());
        });

        HBox row = new HBox(10,
                new Label("Display currency:"), currencyBox, testBtn, saveBtn);
        row.setAlignment(Pos.CENTER_LEFT);

        rateLabel.setStyle("-fx-text-fill: #555;");

        // --- pin section ---
        Label pinTitle = new Label("Security");
        pinTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 15 0 0 0;");

        Label pinStatus = new Label();
        refreshPinStatus(pinStatus);

        Button enableBtn = new Button("Enable PIN");
        Button changeBtn = new Button("Change PIN");
        Button disableBtn = new Button("Disable PIN");

        enableBtn.setOnAction(e -> askNewPin(pinStatus, false));
        changeBtn.setOnAction(e -> askNewPin(pinStatus, true));
        disableBtn.setOnAction(e -> disablePin(pinStatus));

        boolean hasPin = PinService.isEnabled();
        enableBtn.setDisable(hasPin);
        changeBtn.setDisable(!hasPin);
        disableBtn.setDisable(!hasPin);

        HBox pinRow = new HBox(10, enableBtn, changeBtn, disableBtn);
        pinRow.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(20, title, row, rateLabel, pinTitle, pinStatus, pinRow);
        root.setPadding(new Insets(25));
        return root;
    }

    private void refreshPinStatus(Label lbl) {
        if (PinService.isEnabled()) {
            lbl.setText("PIN is enabled.");
        } else {
            lbl.setText("PIN is not set.");
        }
        lbl.setStyle("-fx-text-fill: #666;");
    }

    private void askNewPin(Label statusLabel, boolean requireOld) {
        if (requireOld) {
            TextInputDialog oldD = new TextInputDialog();
            oldD.setTitle("Current PIN");
            oldD.setHeaderText("Enter your current PIN");
            oldD.setContentText("PIN:");
            oldD.showAndWait().ifPresent(old -> {
                if (!PinService.verify(old)) {
                    alert(Alert.AlertType.ERROR, "Wrong PIN", "That PIN is incorrect.");
                    return;
                }
                promptForNewPin(statusLabel);
            });
        } else {
            promptForNewPin(statusLabel);
        }
    }

    private void promptForNewPin(Label statusLabel) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("New PIN");
        d.setHeaderText("Choose a PIN");
        d.setContentText("4 digits:");
        d.showAndWait().ifPresent(pin -> {
            String p = pin.trim();
            if (p.length() < 4) {
                alert(Alert.AlertType.ERROR, "Too short", "Use at least 4 characters.");
                return;
            }
            PinService.setPin(p);
            refreshPinStatus(statusLabel);
            alert(Alert.AlertType.INFORMATION, "Saved", "PIN updated.");
        });
    }

    private void disablePin(Label statusLabel) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Turn off PIN lock?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                PinService.removePin();
                refreshPinStatus(statusLabel);
            }
        });
    }

    private void testRate() {
        String target = currencyBox.getValue();
        rateLabel.setText("Fetching...");
        try {
            double rate = CurrencyService.getRate(target);
            rateLabel.setText(String.format("1 BDT = %.6f %s", rate, target));
        } catch (Exception e) {
            rateLabel.setText("Failed: " + e.getMessage());
        }
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

    private void saveSetting(String key, String value) {
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO settings(key, value) VALUES(?, ?) " +
                             "ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void alert(Alert.AlertType type, String header, String msg) {
        Alert a = new Alert(type);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}