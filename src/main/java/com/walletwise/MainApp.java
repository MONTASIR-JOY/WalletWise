package com.walletwise;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * WalletWise - Personal Finance Manager
 * Entry point of the application.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label welcome = new Label("Welcome to WalletWise!");
        StackPane root = new StackPane(welcome);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("WalletWise");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Uses the Launcher inner class to avoid the
        // "JavaFX runtime components are missing" error
        // when run directly from IntelliJ or the command line.
        Launcher.main(args);
    }

    /**
     * A separate class that does NOT extend Application.
     * JavaFX's special module check only triggers when the main
     * class itself extends Application. By launching from a
     * non-Application class, IntelliJ's green ▶ button works.
     */
    public static class Launcher {
        public static void main(String[] args) {
            Application.launch(MainApp.class, args);
        }
    }
}