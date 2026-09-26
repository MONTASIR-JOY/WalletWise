package com.walletwise.view;

import com.walletwise.dao.TransactionDAO;
import com.walletwise.model.AccountItem;
import com.walletwise.util.Notifiable;
import com.walletwise.util.SceneRouter;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class TransactionListScene implements Notifiable {

    private final TransactionDAO txDAO = new TransactionDAO();
    private final TableView<AccountItem> table = new TableView<>();
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

        Label title = new Label("Transactions");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TableColumn<AccountItem, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDate().toString()));
        dateCol.setPrefWidth(110);

        TableColumn<AccountItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getType().toString()));
        typeCol.setPrefWidth(90);

        TableColumn<AccountItem, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getAmount()));
        amountCol.setPrefWidth(100);

        TableColumn<AccountItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCategory().getName()));
        catCol.setPrefWidth(130);

        TableColumn<AccountItem, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNote()));
        noteCol.setPrefWidth(220);

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

        root = new VBox(15, title, table, actions);
        root.setPadding(new Insets(20));
        VBox.setVgrow(table, Priority.ALWAYS);

        load();
        return root;
    }

    private void load() {
        try {
            List<AccountItem> list = txDAO.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openForm(AccountItem t) {
        TransactionFormScene form = new TransactionFormScene(t, this::load);
        SceneRouter.show(form.getRoot());
    }

    private void deleteSelected() {
        AccountItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notify("Select a transaction first.");
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
                    e.printStackTrace();
                }
            }
        });
    }
}