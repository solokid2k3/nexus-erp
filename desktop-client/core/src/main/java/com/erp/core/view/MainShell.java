package com.erp.core.view;

import com.erp.core.auth.AuthService;
import com.erp.core.auth.SessionContext;
import com.erp.core.component.NavItem;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class MainShell extends BorderPane {

    private final StackPane contentArea;
    private final List<NavItem> navItems;
    private final VBox navContainer;
    private NavItem activeItem;
    private final Runnable onLogout;

    public MainShell(String appTitle, FontAwesomeSolid appIcon, List<NavItem> navItems, Runnable onLogout) {
        this.navItems = navItems;
        this.onLogout = onLogout;
        getStyleClass().add("root-pane");

        // --- Sidebar ---
        var sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        // App header
        var headerBox = new VBox(4);
        headerBox.getStyleClass().add("sidebar-header");
        var appIconNode = new FontIcon(appIcon);
        appIconNode.setIconSize(20);
        appIconNode.setStyle("-fx-icon-color: #111111;");
        var titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        var titleLabel = new Label(appTitle);
        titleLabel.getStyleClass().add("heading-2");
        titleRow.getChildren().addAll(appIconNode, titleLabel);
        headerBox.getChildren().add(titleRow);

        // Navigation
        navContainer = new VBox(2);
        navContainer.setPadding(new Insets(16, 0, 0, 0));

        for (var item : navItems) {
            var btn = item.createButton();
            btn.setOnAction(e -> navigateTo(item));
            navContainer.getChildren().add(btn);
        }

        // Spacer
        var spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info at bottom
        var userBox = new VBox(6);
        userBox.getStyleClass().add("sidebar-user");

        var session = SessionContext.getInstance();
        var userName = session.getUserName() != null ? session.getUserName() : "User";
        var userRole = session.getUserRole() != null ? session.getUserRole() : "Staff";

        var userIcon = new FontIcon(FontAwesomeSolid.USER_CIRCLE);
        userIcon.setIconSize(18);
        userIcon.setStyle("-fx-icon-color: #6B7280;");

        var nameRow = new HBox(8);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        var nameLabel = new Label(userName);
        nameLabel.getStyleClass().add("heading-3");
        nameRow.getChildren().addAll(userIcon, nameLabel);

        var roleLabel = new Label(userRole);
        roleLabel.getStyleClass().add("label-text");

        var logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("logout-btn");
        var logoutIcon = new FontIcon(FontAwesomeSolid.SIGN_OUT_ALT);
        logoutIcon.setIconSize(12);
        logoutIcon.setStyle("-fx-icon-color: #DC2626;");
        logoutBtn.setGraphic(logoutIcon);
        logoutBtn.setOnAction(e -> {
            new AuthService().logout();
            if (onLogout != null) onLogout.run();
        });

        userBox.getChildren().addAll(nameRow, roleLabel, logoutBtn);

        sidebar.getChildren().addAll(headerBox, navContainer, spacer, userBox);
        setLeft(sidebar);

        // --- Content area ---
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(32));
        contentArea.setStyle("-fx-background-color: #F8F9FA;");
        setCenter(contentArea);

        // Navigate to first item
        if (!navItems.isEmpty()) {
            navigateTo(navItems.get(0));
        }
    }

    private void navigateTo(NavItem item) {
        if (activeItem == item) return;

        // Update active state
        if (activeItem != null) activeItem.setActive(false);
        item.setActive(true);
        activeItem = item;

        // Load view with fade transition
        var view = item.getViewFactory().get();
        var fade = new FadeTransition(Duration.millis(250), view);
        fade.setFromValue(0);
        fade.setToValue(1);

        contentArea.getChildren().setAll(view);
        fade.play();
    }
}
