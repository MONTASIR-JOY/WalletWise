package com.walletwise.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class BudgetScene {

    public VBox getRoot() {
        Label title = new Label("Budgets");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label placeholder = new Label("(Budget screen coming in Phase 4)");

        VBox root = new VBox(20, title, placeholder);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        return root;
    }
}