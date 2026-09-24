package com.walletwise.view;

import com.walletwise.dao.BudgetDAO;
import com.walletwise.dao.CategoryDAO;
import com.walletwise.model.Budget;
import com.walletwise.model.Category;
import com.walletwise.model.TransactionType;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class BudgetScene {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO catDAO = new CategoryDAO();

    private final ComboBox<String> monthBox = new ComboBox<>();
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final TableView<Budget> table = new TableView<>();

    public VBox getRoot() {
        Label title = new Label("Budgets");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // month picker
        YearMonth now = YearMonth.now();
        for (int i = -6; i <= 6; i++) {
            monthBox.getItems().add(now.plusMonths(i).toString());
        }
        monthBox.setValue(now.toString());
        monthBox.setOnAction(e -> load());

        // category picker
        try {
            categoryBox.getItems().setAll(catDAO.findByType(TransactionType.EXPENSE));
        } catch (Exception e) {
            // ignore
        }
        categoryBox.setPromptText("Category");

        amountField.setPromptText("Limit e.g. 5000");

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> save());

        HBox form = new HBox(10,
                new Label("Month:"), monthBox,
                new Label("Category:"), categoryBox,
                new Label("Limit:"), amountField,
                saveBtn);
        form.setAlignment(Pos.CENTER_LEFT);

        // table
        TableColumn<Budget, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCategory().getName()));
        catCol.setPrefWidth(180);

        TableColumn<Budget, Double> limitCol = new TableColumn<>("Limit");
        limitCol.setCellValueFactory(new javafx.beans.property.SimpleDoubleProperty(
                0).asObject() == null ? null : null);
        limitCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getLimitAmount()));
        limitCol.setPrefWidth(120);

        TableColumn<Budget, String> monthCol = new TableColumn<>("Month");
        monthCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMonth()));
        monthCol.setPrefWidth(100);

        table.getColumns().addAll(catCol, limitCol, monthCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No budgets set."));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteSelected());

        VBox root = new VBox(15, title, form, table, deleteBtn);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        load();
        return root;
    }

    private void load() {
        try {
            String month = monthBox.getValue();
            List<Budget> list = budgetDAO.findByMonth(month);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            alert("Load failed", e.getMessage());
        }
    }

    private void save() {
        try {
            Category cat = categoryBox.getValue();
            if (cat == null) {
                alert("Pick a category", "Select a category first.");
                return;
            }
            double amt = Double.parseDouble(amountField.getText().trim());
            if (amt <= 0) {
                alert("Invalid amount", "Limit must be positive.");
                return;
            }
            String month = monthBox.getValue();
            budgetDAO.save(new Budget(cat, month, amt));
            amountField.clear();
            load();
        } catch (NumberFormatException e) {
            alert("Invalid amount", "Enter a number.");
        } catch (Exception e) {
            alert("Save failed", e.getMessage());
        }
    }

    private void deleteSelected() {
        Budget b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            alert("Nothing selected", "Pick a row first.");
            return;
        }
        try {
            budgetDAO.delete(b.getId());
            load();
        } catch (Exception e) {
            alert("Delete failed", e.getMessage());
        }
    }

    private void alert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}