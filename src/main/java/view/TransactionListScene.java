package com.walletwise.view;

import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.Transaction;
import com.walletwise.util.SceneRouter;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class TransactionListScene {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final TableView<Transaction> table = new TableView<>();

    public VBox getRoot() {
        Label title = new Label("Transactions");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setPrefWidth(110);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(90);

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<Transaction, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCategory().getName()));
        catCol.setPrefWidth(130);

        TableColumn<Transaction, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(220);

        TableColumn<Transaction, String> receiptCol = new TableColumn<>("Receipt");
        receiptCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getReceiptPath() == null ? "-" : "yes"));
        receiptCol.setPrefWidth(80);

        table.getColumns().addAll(dateCol, typeCol, amountCol, catCol, noteCol, receiptCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No transactions yet."));

        table.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    viewReceipt(row.getItem());
                }
            });
            return row;
        });

        Button addBtn = new Button("+ Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button viewBtn = new Button("View Receipt");
        Button refreshBtn = new Button("Refresh");

        addBtn.setOnAction(e -> openForm(null));
        editBtn.setOnAction(e -> openForm(table.getSelectionModel().getSelectedItem()));
        deleteBtn.setOnAction(e -> deleteSelected());
        viewBtn.setOnAction(e -> {
            Transaction sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Select a transaction first", "No row selected.");
                return;
            }
            viewReceipt(sel);
        });
        refreshBtn.setOnAction(e -> load());

        HBox actions = new HBox(10, addBtn, editBtn, deleteBtn, viewBtn, refreshBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label hint = new Label("Tip: double-click a row to view its receipt.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");

        VBox root = new VBox(15, title, table, actions, hint);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);

        load();
        return root;
    }

    private void load() {
        try {
            List<Transaction> list = txDAO.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showAlert("Could not load transactions", e.getMessage());
        }
    }

    private void openForm(Transaction t) {
        TransactionFormScene form = new TransactionFormScene(t, this::load);
        SceneRouter.show(form.getRoot());
    }

    private void deleteSelected() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a transaction first", "No row selected.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this transaction?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    txDAO.delete(selected.getId());
                    load();
                } catch (Exception e) {
                    showAlert("Delete failed", e.getMessage());
                }
            }
        });
    }

    private void viewReceipt(Transaction t) {
        if (t.getReceiptPath() == null) {
            showAlert("No receipt", "This transaction has no receipt attached.");
            return;
        }
        File f = new File(t.getReceiptPath());
        if (!f.exists()) {
            showAlert("File missing", "The receipt file was moved or deleted.");
            return;
        }

        Stage popup = new Stage();
        popup.setTitle("Receipt");

        Image img = new Image(f.toURI().toString());
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(500);

        VBox box = new VBox(iv);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));

        popup.setScene(new Scene(box));
        popup.show();
    }

    private void showAlert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}