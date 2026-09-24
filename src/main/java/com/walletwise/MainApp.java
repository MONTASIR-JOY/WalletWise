package com.walletwise;

import com.walletwise.dao.Database;
import com.walletwise.util.SceneRouter;
import com.walletwise.view.BudgetScene;
import com.walletwise.view.DashboardScene;
import com.walletwise.view.SettingsScene;
import com.walletwise.view.TransactionListScene;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Database.initialize();

        BorderPane root = new BorderPane();
        SceneRouter.setRoot(root);

        DashboardScene dashboard = new DashboardScene();
        TransactionListScene txScene = new TransactionListScene();
        BudgetScene budgetScene = new BudgetScene();
        SettingsScene settingsScene = new SettingsScene();

        HBox nav = new HBox(10);
        nav.setPadding(new Insets(12));
        nav.setAlignment(Pos.CENTER);
        nav.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-width: 1 0 0 0;");

        Button dashBtn = new Button("Dashboard");
        dashBtn.setPrefWidth(140);
        dashBtn.setOnAction(e -> SceneRouter.show(dashboard.getRoot()));

        Button txBtn = new Button("Transactions");
        txBtn.setPrefWidth(140);
        txBtn.setOnAction(e -> SceneRouter.show(txScene.getRoot()));

        Button budgetBtn = new Button("Budgets");
        budgetBtn.setPrefWidth(140);
        budgetBtn.setOnAction(e -> SceneRouter.show(budgetScene.getRoot()));

        Button settingsBtn = new Button("Settings");
        settingsBtn.setPrefWidth(140);
        settingsBtn.setOnAction(e -> SceneRouter.show(settingsScene.getRoot()));

        nav.getChildren().addAll(dashBtn, txBtn, budgetBtn, settingsBtn);
        root.setBottom(nav);
        SceneRouter.show(dashboard.getRoot());

        stage.setTitle("WalletWise");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}