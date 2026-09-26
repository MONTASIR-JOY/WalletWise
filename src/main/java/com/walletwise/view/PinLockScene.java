package com.walletwise.view;

import com.walletwise.util.PinService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PinLockScene {

    private final Runnable onUnlock;
    private final PasswordField pinField = new PasswordField();
    private final Label message = new Label();

    public PinLockScene(Runnable onUnlock) {
        this.onUnlock = onUnlock;
    }

    public StackPane getRoot() {
        Label title = new Label("WalletWise");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label prompt = new Label("Enter your PIN");
        prompt.setStyle("-fx-text-fill: #666;");

        pinField.setPromptText("****");
        pinField.setPrefWidth(180);
        pinField.setStyle("-fx-font-size: 16px;");

        Button unlockBtn = new Button("Unlock");
        unlockBtn.setDefaultButton(true);
        unlockBtn.setOnAction(e -> tryUnlock());

        message.setStyle("-fx-text-fill: #c0392b;");

        VBox box = new VBox(15, title, prompt, pinField, unlockBtn, message);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        AnchorPane inner = new AnchorPane();
        inner.getChildren().add(box);
        AnchorPane.setTopAnchor(box, 0.0);
        AnchorPane.setBottomAnchor(box, 0.0);
        AnchorPane.setLeftAnchor(box, 0.0);
        AnchorPane.setRightAnchor(box, 0.0);

        StackPane root = new StackPane(inner);
        return root;
    }

    private void tryUnlock() {
        String pin = pinField.getText();
        if (pin.isEmpty()) {
            message.setText("Please enter a PIN.");
            return;
        }
        if (PinService.verify(pin)) {
            onUnlock.run();
        } else {
            message.setText("Wrong PIN.");
            pinField.clear();
        }
    }
}