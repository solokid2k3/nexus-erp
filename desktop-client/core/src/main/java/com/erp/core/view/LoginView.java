package com.erp.core.view;

import com.erp.core.auth.AuthService;
import com.erp.core.component.LoadingSpinner;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

import java.util.function.Consumer;

public class LoginView extends StackPane {

    private final AuthService authService = new AuthService();
    private final Consumer<Void> onLoginSuccess;
    private final String clientTitle;

    public LoginView(String clientTitle, Consumer<Void> onLoginSuccess) {
        this.clientTitle = clientTitle;
        this.onLoginSuccess = onLoginSuccess;
        getStyleClass().add("root-pane");
        setAlignment(Pos.CENTER);
        buildUI();
    }

    private void buildUI() {
        var card = new VBox(20);
        card.getStyleClass().add("card-elevated");
        card.setMaxWidth(420);
        card.setPadding(new Insets(48, 40, 40, 40));
        card.setAlignment(Pos.CENTER);

        // Shield icon
        var lockIcon = new FontIcon(BytedanceIconsRegularMZ.SHIELD);
        lockIcon.setIconSize(36);
        lockIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#111111"));

        var title = new Label(clientTitle);
        title.getStyleClass().add("heading-1");

        var subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("body-muted");

        var spacer = new Region();
        spacer.setPrefHeight(8);

        // Username field with icon
        var userIcon = new FontIcon(BytedanceIconsRegularMZ.USER);
        userIcon.setIconSize(16);
        userIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#9CA3AF"));

        var usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(44);
        usernameField.setStyle("-fx-padding: 8 12 8 36;"); // Make room for icon
        
        var usernamePane = new StackPane();
        StackPane.setAlignment(userIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(userIcon, new Insets(0, 0, 0, 12));
        usernamePane.getChildren().addAll(usernameField, userIcon);

        // Password field with icon
        var passIcon = new FontIcon(BytedanceIconsRegularAL.LOCK);
        passIcon.setIconSize(16);
        passIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#9CA3AF"));

        var passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(44);
        passwordField.setStyle("-fx-padding: 8 12 8 36;"); // Make room for icon
        
        var passwordPane = new StackPane();
        StackPane.setAlignment(passIcon, Pos.CENTER_LEFT);
        StackPane.setMargin(passIcon, new Insets(0, 0, 0, 12));
        passwordPane.getChildren().addAll(passwordField, passIcon);

        // Login button
        var loginIcon = new FontIcon(BytedanceIconsRegularAL.LOGIN);
        loginIcon.setIconSize(16);
        loginIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF"));

        var loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setGraphic(loginIcon);
        loginBtn.setPrefHeight(44);
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        var errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        var spinner = new LoadingSpinner("Signing in...");
        spinner.setVisible(false);
        spinner.setManaged(false);

        loginBtn.setOnAction(e -> {
            var user = usernameField.getText().trim();
            var pass = passwordField.getText().trim();
            if (user.isEmpty() || pass.isEmpty()) {
                showError(errorLabel, "Please enter username and password");
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
                resetButton(loginBtn, spinner);
                if (success) {
                    onLoginSuccess.accept(null);
                } else {
                    showError(errorLabel, "Invalid username or password");
                }
            })).exceptionally(ex -> {
                Platform.runLater(() -> {
                    resetButton(loginBtn, spinner);
                    showError(errorLabel, "Connection error — is the backend running?");
                });
                return null;
            });
        });

        passwordField.setOnAction(e -> loginBtn.fire());

        // Demo hint
        var hintIcon = new FontIcon(BytedanceIconsRegularAL.ATTENTION);
        hintIcon.setIconSize(14);
        hintIcon.setIconColor(javafx.scene.paint.Paint.valueOf("#9CA3AF"));

        var demoHint = new Label("Demo: admin / admin123");
        demoHint.setGraphic(hintIcon);
        demoHint.getStyleClass().add("label-text");

        card.getChildren().addAll(lockIcon, title, subtitle, spacer,
                usernamePane, passwordPane, errorLabel, loginBtn, spinner, demoHint);

        // Illustration Box
        var illustrationBox = new VBox(16);
        illustrationBox.setAlignment(Pos.CENTER);
        try {
            var imageStream = getClass().getResourceAsStream("/images/sticker_login.png");
            if (imageStream != null) {
                var image = new javafx.scene.image.Image(imageStream);
                var imageView = new javafx.scene.image.ImageView(image);
                imageView.setFitWidth(320);
                imageView.setPreserveRatio(true);
                
                var brandingLabel = new Label("Enterprise Ready");
                brandingLabel.getStyleClass().add("heading-2");
                
                var brandingSub = new Label("Secure, reliable, and professional.");
                brandingSub.getStyleClass().add("body-muted");
                
                illustrationBox.getChildren().addAll(imageView, brandingLabel, brandingSub);
            }
        } catch (Exception e) {
            System.err.println("Failed to load login sticker: " + e.getMessage());
        }

        var mainContainer = new HBox(80);
        mainContainer.setAlignment(Pos.CENTER);
        if (!illustrationBox.getChildren().isEmpty()) {
            mainContainer.getChildren().add(illustrationBox);
        }
        mainContainer.getChildren().add(card);

        // Entry animation
        mainContainer.setScaleX(0.95);
        mainContainer.setScaleY(0.95);
        mainContainer.setOpacity(0);

        var fadeIn = new FadeTransition(Duration.millis(400), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        var scaleIn = new ScaleTransition(Duration.millis(400), mainContainer);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        fadeIn.play();
        scaleIn.play();

        getChildren().add(mainContainer);
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void resetButton(Button loginBtn, LoadingSpinner spinner) {
        loginBtn.setDisable(false);
        loginBtn.setVisible(true);
        loginBtn.setManaged(true);
        spinner.setVisible(false);
        spinner.setManaged(false);
    }
}
