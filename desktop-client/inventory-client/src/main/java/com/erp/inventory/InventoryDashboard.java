package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class InventoryDashboard extends VBox {

    public InventoryDashboard() {
        setSpacing(24);
        setPadding(new Insets(0));

        var title = new Label("Dashboard");
        title.getStyleClass().add("display-heading");

        var statsRow = new HBox(16);
        var totalProducts = new StatCard("Total Products", "—", FontAwesomeSolid.TAG, "#6366F1");
        var lowStock = new StatCard("Low Stock Alerts", "—", FontAwesomeSolid.EXCLAMATION_TRIANGLE, "#F59E0B");
        var movements = new StatCard("Movements Today", "—", FontAwesomeSolid.EXCHANGE_ALT, "#10B981");
        var warehouses = new StatCard("Warehouses", "—", FontAwesomeSolid.BUILDING, "#3B82F6");
        statsRow.getChildren().addAll(totalProducts, lowStock, movements, warehouses);

        var skeleton = new SkeletonPane(4);
        getChildren().addAll(title, statsRow, skeleton);

        // Fetch real data
        var api = ApiClient.getInstance();
        api.getRaw("/inventory/products").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                if (data.isArray()) totalProducts.setValue(String.valueOf(data.size()));
                else if (data.has("total")) totalProducts.setValue(String.valueOf(data.get("total").asInt()));
                else totalProducts.setValue(String.valueOf(data.size()));
            } catch (Exception e) { totalProducts.setValue("—"); }
        })).exceptionally(ex -> { Platform.runLater(() -> totalProducts.setValue("—")); return null; });

        api.getRaw("/inventory/stock/alerts").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                if (data.isArray()) lowStock.setValue(String.valueOf(data.size()));
                else lowStock.setValue("0");
            } catch (Exception e) { lowStock.setValue("0"); }
        })).exceptionally(ex -> { Platform.runLater(() -> lowStock.setValue("0")); return null; });

        api.getRaw("/inventory/stock/movements").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                if (data.isArray()) movements.setValue(String.valueOf(data.size()));
                else movements.setValue("0");
            } catch (Exception e) { movements.setValue("0"); }
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> { movements.setValue("0"); getChildren().remove(skeleton); }); return null; });

        warehouses.setValue("—");
    }
}
