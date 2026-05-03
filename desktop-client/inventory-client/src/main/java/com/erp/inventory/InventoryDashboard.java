package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

public class InventoryDashboard extends VBox {
    public InventoryDashboard() {
        setSpacing(24);
        var title = new Label("Dashboard"); title.getStyleClass().add("display-heading");
        var statsRow = new HBox(16);
        var products = new StatCard("Products", "—", BytedanceIconsRegularMZ.TAG_ONE, "#6366F1");
        var stock = new StatCard("Stock Items", "—", BytedanceIconsRegularAL.BOX, "#10B981");
        var categories = new StatCard("Categories", "—", BytedanceIconsRegularAL.FOLDER_CLOSE, "#F59E0B");
        var warehouses = new StatCard("Warehouses", "—", BytedanceIconsRegularMZ.WAREHOUSING, "#3B82F6");
        statsRow.getChildren().addAll(products, stock, categories, warehouses);
        var skeleton = new SkeletonPane(3);
        getChildren().addAll(title, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/inventory/products").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) products.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/stock").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) stock.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/categories").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) categories.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/inventory/warehouses").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) warehouses.setValue(String.valueOf(d.size())); } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
