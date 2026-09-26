package com.walletwise.util;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

public class SceneRouter {

    private static BorderPane root;

    public static void setRoot(BorderPane r) {
        root = r;
    }

    public static void show(Region content) {
        root.setCenter(content);
    }
}