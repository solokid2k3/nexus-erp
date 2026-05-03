package com.erp.desktop.view;

import com.erp.desktop.api.ApiClient;
import com.erp.desktop.auth.SessionContext;
import com.erp.desktop.component.SkeletonPane;
import com.erp.desktop.component.StatCard;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.List;

public class DashboardView extends ScrollPane {

    private final StackPane toastParent;
    private final ApiClient api = ApiClient.getInstance();

    public DashboardView(StackPane toastParent) {
        this.toastParent = toastParent;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        var root = new VBox(24);
        root.setPadding(new Insets(32));

        var session = SessionContext.getInstance();
        var welcome = new Label("👋  Welcome back" + (session.getUserName() != null ? ", " + session.getUserName() : "") + "!");
        welcome.getStyleClass().add("heading-1");

        var subtitle = new Label("Here's what's happening across your ERP system today.");
        subtitle.getStyleClass().add("body-muted");

        var statsRow = new FlowPane(16, 16);
        statsRow.setPadding(new Insets(8, 0, 0, 0));

        var skeleton = new SkeletonPane(4, 600);
        statsRow.getChildren().add(skeleton);

        root.getChildren().addAll(welcome, subtitle, statsRow);
        setContent(root);

        loadStats(statsRow, skeleton);
    }

    private void loadStats(FlowPane statsRow, SkeletonPane skeleton) {
        var invProducts = new StatCard("📦  Products", "—");
        var invAlerts = new StatCard("⚠️  Stock Alerts", "—");
        var ordSummary = new StatCard("🛒  Sales Orders", "—");
        var hrEmployees = new StatCard("👥  Employees", "—");
        var finAccounts = new StatCard("💰  Accounts", "—");
        var allCards = List.of(invProducts, invAlerts, ordSummary, hrEmployees, finAccounts);

        Platform.runLater(() -> {
            statsRow.getChildren().remove(skeleton);
            statsRow.getChildren().addAll(allCards);
            for (int i = 0; i < allCards.size(); i++) {
                var card = allCards.get(i);
                card.setOpacity(0);
                var fade = new FadeTransition(Duration.millis(300), card);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.setDelay(Duration.millis(i * 80));
                fade.play();
            }
        });

        api.get("/inventory/products?page_size=1").thenAccept(resp -> Platform.runLater(() -> {
            if (resp.containsKey("total")) invProducts.setValue(String.valueOf(resp.get("total")));
            else if (resp.containsKey("products")) {
                var list = (List<?>) resp.get("products");
                invProducts.setValue(String.valueOf(list != null ? list.size() : 0));
            }
        }));

        api.get("/inventory/stock/alerts").thenAccept(resp -> Platform.runLater(() -> {
            if (resp.containsKey("alerts")) {
                var list = (List<?>) resp.get("alerts");
                invAlerts.setValue(String.valueOf(list != null ? list.size() : 0));
            }
        }));

        api.get("/orders/summary").thenAccept(resp -> Platform.runLater(() -> {
            if (resp.containsKey("total_orders")) ordSummary.setValue(String.valueOf(resp.get("total_orders")));
        }));

        api.get("/hr/employees").thenAccept(resp -> Platform.runLater(() -> {
            if (resp.containsKey("employees")) {
                var list = (List<?>) resp.get("employees");
                hrEmployees.setValue(String.valueOf(list != null ? list.size() : 0));
            }
        }));

        api.get("/finance/accounts").thenAccept(resp -> Platform.runLater(() -> {
            if (resp.containsKey("accounts")) {
                var list = (List<?>) resp.get("accounts");
                finAccounts.setValue(String.valueOf(list != null ? list.size() : 0));
            }
        }));
    }
}
