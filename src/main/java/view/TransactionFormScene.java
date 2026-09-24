package com.walletwise.view;

import com.walletwise.dao.CategoryDAO;
import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.Category;
import com.walletwise.model.Transaction;
import com.walletwise.model.TransactionType;
import com.walletwise.util.SceneRouter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

public class TransactionFormScene {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final CategoryDAO catDAO = new CategoryDAO();

    private final Transaction existing;
    private final Runnable onSaved;

    private final ComboBox<TransactionType> typeBox = new ComboBox<>();
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField amountField = new TextField();
    private final DatePicker datePicker = new DatePicker();
    private final TextField noteField = new TextField();

    private final ImageView receiptPreview = new ImageView();
    private String receiptPath;   // current path (relative to project root)

    public TransactionFormScene(Transaction existing, Runnable onSaved) {
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

        // --- Receipt section ---
        Button attachBtn = new Button("Attach Receipt");
        Button clearBtn = new Button("Remove");
        attachBtn.setOnAction(e -> pickReceipt());
        clearBtn.setOnAction(e -> {
            receiptPath = null;
            receiptPreview.setImage(null);
        });

        HBox receiptButtons = new HBox(8, attachBtn, clearBtn);
        receiptButtons.setAlignment(Pos.CENTER_LEFT);

        receiptPreview.setFitWidth(120);
        receiptPreview.setFitHeight(120);
        receiptPreview.setPreserveRatio(true);
        receiptPreview.setStyle("-fx-border-color: #ddd; -fx-border-width: 1;");

        VBox receiptBox = new VBox(8, receiptButtons, receiptPreview);
        form.add(new Label("Receipt:"), 0, 5);
        form.add(receiptBox, 1, 5);

        if (existing != null) {
            typeBox.setValue(existing.getType());
            reloadCategories();
            categoryBox.setValue(existing.getCategory());
            amountField.setText(String.valueOf(existing.getAmount()));
            datePicker.setValue(existing.getDate());
            noteField.setText(existing.getNote());
            receiptPath = existing.getReceiptPath();
            if (receiptPath != null) {
                showPreview(receiptPath);
            }
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

    private void pickReceipt() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pick a receipt image");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File chosen = fc.showOpenDialog(null);
        if (chosen == null) return;

        try {
            Path receiptsDir = Paths.get("receipts");
            if (!Files.exists(receiptsDir)) {
                Files.createDirectories(receiptsDir);
            }

            String ext = "";
            String orig = chosen.getName();
            int dot = orig.lastIndexOf('.');
            if (dot >= 0) ext = orig.substring(dot);

            String newName = System.currentTimeMillis() + ext;
            Path target = receiptsDir.resolve(newName);
            Files.copy(chosen.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            receiptPath = target.toString();
            showPreview(receiptPath);

        } catch (Exception ex) {
            alert("Could not attach receipt", ex.getMessage());
        }
    }

    private void showPreview(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                receiptPreview.setImage(new Image(f.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            alert("Could not load categories", e.getMessage());
        }
    }

    private void save() {
        try {
            Category cat = categoryBox.getValue();
            if (cat == null) {
                alert("Pick a category", "Create one first with + Category.");
                return;
            }

            double amt;
            try {
                amt = Double.parseDouble(amountField.getText().trim());
            } catch (Exception ex) {
                alert("Invalid amount", "Please enter a number.");
                return;
            }
            if (amt <= 0) {
                alert("Invalid amount", "Amount must be greater than 0.");
                return;
            }

            LocalDate date = datePicker.getValue();
            if (date == null) {
                alert("Pick a date", "Please choose a date.");
                return;
            }
            String note = noteField.getText().trim();

            if (existing == null) {
                txDAO.insert(new Transaction(amt, typeBox.getValue(), cat, date, note, receiptPath));
            } else {
                existing.setAmount(amt);
                existing.setType(typeBox.getValue());
                existing.setCategory(cat);
                existing.setDate(date);
                existing.setNote(note);
                existing.setReceiptPath(receiptPath);
                txDAO.update(existing);
            }
            onSaved.run();

        } catch (Exception e) {
            alert("Save failed", e.getMessage());
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
                alert("Could not create category", e.getMessage());
            }
        });
    }

    private void alert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}