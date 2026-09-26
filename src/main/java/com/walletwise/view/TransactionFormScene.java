package com.walletwise.view;

import com.walletwise.dao.CategoryDAO;
import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.AccountItem;
import com.walletwise.model.Category;
import com.walletwise.model.Expense;
import com.walletwise.model.Income;
import com.walletwise.model.TransactionType;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class TransactionFormScene {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final CategoryDAO catDAO = new CategoryDAO();

    private final AccountItem existing;
    private final Runnable onSaved;

    private final ComboBox<TransactionType> typeBox = new ComboBox<>();
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField noteField = new TextField();

    public TransactionFormScene(AccountItem existing, Runnable onSaved) {
        this.existing = existing;
        this.onSaved = onSaved;
    }

    public VBox getRoot() {
        Label title = new Label(existing == null ? "New Transaction" : "Edit Transaction");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        typeBox.getItems().addAll(TransactionType.values());
        typeBox.setValue(TransactionType.EXPENSE);
        typeBox.setOnAction(e -> reloadCategories());

        categoryBox.setPrefWidth(220);
        amountField.setPromptText("0.00");
        datePicker.setValue(LocalDate.now());
        noteField.setPromptText("optional");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.add(new Label("Type:"), 0, 0);
        form.add(typeBox, 1, 0);
        form.add(new Label("Category:"), 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(new Label("Amount:"), 0, 2);
        form.add(amountField, 1, 2);
        form.add(new Label("Date:"), 0, 3);
        form.add(datePicker, 1, 3);
        form.add(new Label("Note:"), 0, 4);
        form.add(noteField, 1, 4);

        if (existing != null) {
            typeBox.setValue(existing.getType());
            reloadCategories();
            categoryBox.setValue(existing.getCategory());
            amountField.setText(String.valueOf(existing.getAmount()));
            datePicker.setValue(existing.getDate());
            noteField.setText(existing.getNote());
        } else {
            reloadCategories();
        }

        Button saveBtn = new Button(existing == null ? "Save" : "Update");
        Button cancelBtn = new Button("Cancel");
        Button newCatBtn = new Button("+ Category");

        saveBtn.setOnAction(e -> save());
        cancelBtn.setOnAction(e -> onSaved.run());
        newCatBtn.setOnAction(e -> createCategory());

        HBox buttons = new HBox(10, saveBtn, cancelBtn, newCatBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(20, title, form, buttons);
        root.setPadding(new Insets(30));
        return root;
    }

    private void reloadCategories() {
        try {
            TransactionType sel = typeBox.getValue();
            List<Category> cats = catDAO.findByType(sel);
            categoryBox.getItems().setAll(cats);
            if (cats.size() > 0 && existing == null) {
                categoryBox.setValue(cats.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            Category cat = categoryBox.getValue();
            if (cat == null) {
                alert("Pick a category first.");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(amountField.getText().trim());
            } catch (Exception ex) {
                alert("Please enter a valid number.");
                return;
            }

            if (amt <= 0) {
                alert("Amount must be greater than 0.");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                alert("Please choose a date.");
                return;
            }

            String note = noteField.getText().trim();
            TransactionType type = typeBox.getValue();

            if (existing == null) {
                AccountItem item;
                if (type == TransactionType.INCOME) {
                    item = new Income(amt, cat, date, note);
                } else {
                    item = new Expense(amt, cat, date, note);
                }
                txDAO.insert(item);
            } else {
                existing.setAmount(amt);
                existing.setCategory(cat);
                existing.setDate(date);
                existing.setNote(note);
                txDAO.update(existing);
            }

            onSaved.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCategory() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("New Category");
        d.setHeaderText("Create a " + typeBox.getValue() + " category");
        d.setContentText("Name:");
        d.showAndWait().ifPresent(name -> {
            String n = name.trim();
            if (n.isEmpty()) return;
            try {
                Category c = catDAO.insert(new Category(n, typeBox.getValue()));
                reloadCategories();
                categoryBox.setValue(c);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}