package com.erp.core.app;

import com.erp.core.view.LoginView;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public abstract class BaseApp extends Application {

    protected Stage primaryStage;
    protected Scene scene;
    protected StackPane root;

    protected abstract String getAppTitle();
    protected abstract javafx.scene.Node createMainView();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.root = new StackPane();
        this.scene = new Scene(root, 1280, 820);

        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter-Variable.ttf"), 14);
        var css = getClass().getResource("/css/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle(getAppTitle());
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();

        showLogin();
    }

    private void showLogin() {
        var loginView = new LoginView(getAppTitle(), ignored -> {
            var mainView = createMainView();
            var fadeOut = new FadeTransition(Duration.millis(200), root.getChildren().get(0));
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                root.getChildren().setAll(mainView);
                var fadeIn = new FadeTransition(Duration.millis(300), mainView);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        });
        root.getChildren().setAll(loginView);
    }
}
