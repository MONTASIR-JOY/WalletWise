package com.walletwise.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardScene {

    public VBox getRoot() {
        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label ph = new Label("(coming soon)");

        VBox root = new VBox(20, title, ph);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-padding: 40;");
        return root;
    }
}