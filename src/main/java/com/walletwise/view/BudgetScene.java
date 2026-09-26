package com.walletwise.view;

import com.walletwise.dao.BudgetDAO;
import com.walletwise.dao.CategoryDAO;
import com.walletwise.model.Budget;
import com.walletwise.model.Category;
import com.walletwise.model.TransactionType;
import com.walletwise.util.Notifiable;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.List;

public class BudgetScene implements Notifiable {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO catDAO = new CategoryDAO();

    private final ComboBox<String> monthBox = new ComboBox<>();
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final TableView<Budget> table = new TableView<>();
    private VBox root;

    @Override
    public void notify(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public VBox getRoot() {
        if (root != null) {
            load();
            return root;
        }

        Label title = new Label("Budgets");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        YearMonth now = YearMonth.now();
        for (int i = -6; i <= 6; i++) {
            monthBox.getItems().add(now.plusMonths(i).toString());
        }
        monthBox.setValue(now.toString());
        monthBox.setOnAction(e -> load());

        try {
            categoryBox.getItems().setAll(catDAO.findByType(TransactionType.EXPENSE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        categoryBox.setPromptText("Category");
        categoryBox.setPrefWidth(180);

        amountField.setPromptText("Limit e.g. 5000");

        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> save());

        HBox form = new HBox(10,
                new Label("Month:"), monthBox,
                new Label("Category:"), categoryBox,
                new Label("Limit:"), amountField,
                saveBtn);
        form.setAlignment(Pos.CENTER_LEFT);

        TableColumn<Budget, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCategory().getName()));
        catCol.setPrefWidth(180);

        TableColumn<Budget, Double> limitCol = new TableColumn<>("Limit");
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

        root = new VBox(15, title, form, table, deleteBtn);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);

        load();
        return root;
    }

    private void load() {
        try {
            String month = monthBox.getValue();
            List<Budget> list = budgetDAO.findByMonth(month);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Category cat = categoryBox.getValue();
            if (cat == null) {
                notify("Pick a category first.");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(amountField.getText().trim());
            } catch (Exception ex) {
                notify("Enter a valid number.");
                return;
            }

            if (amt <= 0) {
                notify("Limit must be positive.");
                return;
            }

            String month = monthBox.getValue();
            budgetDAO.save(new Budget(cat, month, amt));
            amountField.clear();
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSelected() {
        Budget b = table.getSelectionModel().getSelectedItem();
        if (b == null) {
            notify("Pick a row first.");
            return;
        }
        try {
            budgetDAO.delete(b.getId());
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}