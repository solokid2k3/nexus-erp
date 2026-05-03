package com.erp.desktop.view;

import com.erp.desktop.auth.AuthService;
import com.erp.desktop.component.LoadingSpinner;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.function.Consumer;

public class LoginView extends StackPane {

    private final AuthService authService = new AuthService();
    private final Consumer<Void> onLoginSuccess;

    public LoginView(Consumer<Void> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        getStyleClass().add("root-pane");
        setAlignment(Pos.CENTER);
        buildUI();
    }

    private void buildUI() {
        var card = new VBox(20);
        card.getStyleClass().add("card-elevated");
        card.setMaxWidth(400);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);

        var emoji = new Label("🔐");
        emoji.setStyle("-fx-font-size: 40px;");

        var title = new Label("Nexus ERP");
        title.getStyleClass().add("display-heading");
        title.setStyle("-fx-font-size: 32px;");

        var subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("body-muted");

        var spacer = new Region();
        spacer.setPrefHeight(4);

        var usernameField = new TextField();
        usernameField.setPromptText("👤  Username");
        usernameField.setPrefHeight(42);

        var passwordField = new PasswordField();
        passwordField.setPromptText("🔑  Password");
        passwordField.setPrefHeight(42);

        var loginBtn = new Button("Sign In →");
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setPrefHeight(42);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        var errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        var spinner = new LoadingSpinner("Signing in...");
        spinner.setVisible(false);
        spinner.setManaged(false);

        loginBtn.setOnAction(e -> {
            var user = usernameField.getText().trim();
            var pass = passwordField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                errorLabel.setText("⚠️  Please enter username and password");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }

            loginBtn.setDisable(true);
            loginBtn.setVisible(false);
            loginBtn.setManaged(false);
            spinner.setVisible(true);
            spinner.setManaged(true);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            authService.login(user, pass).thenAccept(success -> Platform.runLater(() -> {
                loginBtn.setDisable(false);
                loginBtn.setVisible(true);
                loginBtn.setManaged(true);
                spinner.setVisible(false);
                spinner.setManaged(false);
                if (success) {
                    onLoginSuccess.accept(null);
                } else {
                    errorLabel.setText("❌  Invalid username or password");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setVisible(true);
                    loginBtn.setManaged(true);
                    spinner.setVisible(false);
                    spinner.setManaged(false);
                    errorLabel.setText("⚠️  Connection error — is the backend running?");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                });
                return null;
            });
        });

        passwordField.setOnAction(e -> loginBtn.fire());

        var demoHint = new Label("💡  Demo: admin / admin123");
        demoHint.getStyleClass().add("label-text");

        card.getChildren().addAll(emoji, title, subtitle, spacer, usernameField, passwordField, errorLabel, loginBtn, spinner, demoHint);

        var fadeIn = new FadeTransition(Duration.millis(500), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        getChildren().add(card);
    }
}
