package com.erp.orders;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

public class OrdersDashboard extends VBox {
    public OrdersDashboard() {
        setSpacing(24);
        var title = new Label("Dashboard"); title.getStyleClass().add("display-heading");
        var statsRow = new HBox(16);
        var sales = new StatCard("Sales Orders", "—", BytedanceIconsRegularMZ.SHOPPING_CART, "#6366F1");
        var purchases = new StatCard("Purchase Orders", "—", BytedanceIconsRegularMZ.TRANSACTION, "#10B981");
        var customers = new StatCard("Customers", "—", BytedanceIconsRegularMZ.PEOPLES, "#F59E0B");
        var suppliers = new StatCard("Suppliers", "—", BytedanceIconsRegularAL.FACTORY_BUILDING, "#3B82F6");
        statsRow.getChildren().addAll(sales, purchases, customers, suppliers);
        var skeleton = new SkeletonPane(3);
        getChildren().addAll(title, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/orders/sales").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) sales.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/orders/purchases").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) purchases.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/orders/customers").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) customers.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/orders/suppliers").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) suppliers.setValue(String.valueOf(d.size())); } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
