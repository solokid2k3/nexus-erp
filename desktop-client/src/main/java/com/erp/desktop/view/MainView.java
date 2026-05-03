package com.erp.desktop.view;

import com.erp.desktop.auth.AuthService;
import com.erp.desktop.auth.SessionContext;
import com.erp.desktop.view.inventory.InventoryView;
import com.erp.desktop.view.orders.OrdersView;
import com.erp.desktop.view.finance.FinanceView;
import com.erp.desktop.view.hr.HRView;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.function.Consumer;

public class MainView extends BorderPane {

    private final StackPane contentArea = new StackPane();
    private final Consumer<Void> onLogout;
    private Button activeNavButton;

    public MainView(Consumer<Void> onLogout) {
        this.onLogout = onLogout;
        getStyleClass().add("app-wrapper");
        setLeft(buildSidebar());
        setCenter(contentArea);
        contentArea.setStyle("-fx-background-color: #FFFFFF;");
        navigateTo("Dashboard");
    }

    private VBox buildSidebar() {
        var sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);

        var logo = new Label("⚡ Nexus ERP");
        logo.getStyleClass().add("heading-3");
        logo.setPadding(new Insets(16, 20, 24, 20));

        var session = SessionContext.getInstance();
        var userRow = new HBox(8);
        userRow.setPadding(new Insets(0, 20, 0, 20));
        userRow.setAlignment(Pos.CENTER_LEFT);
        var avatar = new Label("👤");
        avatar.setStyle("-fx-font-size: 18px;");
        var userInfo = new VBox(2);
        var userName = new Label(session.getUserName() != null ? session.getUserName() : "User");
        userName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111111;");
        var userRole = new Label(session.getUserRole() != null ? session.getUserRole() : "—");
        userRole.getStyleClass().add("label-text");
        userInfo.getChildren().addAll(userName, userRole);
        userRow.getChildren().addAll(avatar, userInfo);

        var sep = new Separator();
        sep.setPadding(new Insets(12, 0, 4, 0));

        var navSection = new VBox(2);
        navSection.setPadding(new Insets(0));

        addNavSection(navSection, "MAIN");
        var dashBtn = addNavItem(navSection, "📊  Dashboard", "Dashboard");

        addNavSection(navSection, "MODULES");
        addNavItem(navSection, "📦  Inventory", "Inventory");
        addNavItem(navSection, "🛒  Orders", "Orders");
        addNavItem(navSection, "💰  Finance", "Finance");
        addNavItem(navSection, "👥  HR", "HR");

        var spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        var logoutBtn = new Button("🚪  Sign Out");
        logoutBtn.getStyleClass().add("button-ghost");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            new AuthService().logout();
            onLogout.accept(null);
        });
        var logoutWrap = new HBox(logoutBtn);
        logoutWrap.setPadding(new Insets(12, 16, 16, 16));
        HBox.setHgrow(logoutBtn, Priority.ALWAYS);

        sidebar.getChildren().addAll(logo, userRow, sep, navSection, spacer, logoutWrap);
        setActiveNav(dashBtn);
        return sidebar;
    }

    private void addNavSection(VBox container, String title) {
        var label = new Label(title);
        label.getStyleClass().add("nav-section-label");
        container.getChildren().add(label);
    }

    private Button addNavItem(VBox container, String text, String target) {
        var btn = new Button(text);
        btn.getStyleClass().add("nav-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> {
            setActiveNav(btn);
            navigateTo(target);
        });
        container.getChildren().add(btn);
        return btn;
    }

    private void setActiveNav(Button btn) {
        if (activeNavButton != null) activeNavButton.getStyleClass().remove("nav-item-active");
        btn.getStyleClass().add("nav-item-active");
        activeNavButton = btn;
    }

    private void navigateTo(String target) {
        var view = switch (target) {
            case "Inventory" -> new InventoryView(contentArea);
            case "Orders" -> new OrdersView(contentArea);
            case "Finance" -> new FinanceView(contentArea);
            case "HR" -> new HRView(contentArea);
            default -> new DashboardView(contentArea);
        };

        view.setOpacity(0);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);

        var fade = new FadeTransition(Duration.millis(200), view);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}
