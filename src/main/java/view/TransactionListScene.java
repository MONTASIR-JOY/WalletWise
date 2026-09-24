package com.walletwise.view;

import com.walletwise.dao.CategoryDAO;
import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.Transaction;
import com.walletwise.util.SceneRouter;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
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
        noteCol.setPrefWidth(250);

        table.getColumns().addAll(dateCol, typeCol, amountCol, catCol, noteCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No transactions yet."));

        Button addBtn = new Button("+ Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button refreshBtn = new Button("Refresh");

        addBtn.setOnAction(e -> openForm(null));
        editBtn.setOnAction(e -> openForm(table.getSelectionModel().getSelectedItem()));
        deleteBtn.setOnAction(e -> deleteSelected());
        refreshBtn.setOnAction(e -> load());

        HBox actions = new HBox(10, addBtn, editBtn, deleteBtn, refreshBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(15, title, table, actions);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

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

    private void showAlert(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(header);
        a.setContentText(msg);
        a.showAndWait();
    }
}