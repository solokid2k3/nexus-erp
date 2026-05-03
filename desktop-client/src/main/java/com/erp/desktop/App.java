package com.erp.desktop;

import com.erp.desktop.view.LoginView;
import com.erp.desktop.view.MainView;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class App extends Application {

    private static final String APP_TITLE = "Nexus ERP";
    private final StackPane root = new StackPane();

    @Override
    public void start(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Variable.ttf"), 16);

        var scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        showLogin();

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    private void showLogin() {
        var login = new LoginView(v -> fadeTransition(new MainView(x -> fadeTransition(new LoginView(y -> showLogin()), 300)), 300));
        fadeTransition(login, 400);
    }

    private void fadeTransition(javafx.scene.Node newView, int durationMs) {
        newView.setOpacity(0);
        root.getChildren().clear();
        root.getChildren().add(newView);

        var fade = new FadeTransition(Duration.millis(durationMs), newView);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
