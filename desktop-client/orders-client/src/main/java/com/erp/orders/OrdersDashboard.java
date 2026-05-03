package com.erp.orders;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class OrdersDashboard extends VBox {
    public OrdersDashboard() {
        setSpacing(24);
        var title = new Label("Dashboard");
        title.getStyleClass().add("display-heading");

        var statsRow = new HBox(16);
        var salesOrders = new StatCard("Sales Orders", "—", FontAwesomeSolid.FILE_INVOICE_DOLLAR, "#6366F1");
        var purchaseOrders = new StatCard("Purchase Orders", "—", FontAwesomeSolid.TRUCK, "#10B981");
        var customers = new StatCard("Customers", "—", FontAwesomeSolid.USERS, "#3B82F6");
        var suppliers = new StatCard("Suppliers", "—", FontAwesomeSolid.INDUSTRY, "#F59E0B");
        statsRow.getChildren().addAll(salesOrders, purchaseOrders, customers, suppliers);

        var skeleton = new SkeletonPane(3);
        getChildren().addAll(title, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/orders/summary").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                if (data.has("total_sales")) salesOrders.setValue(String.valueOf(data.get("total_sales").asInt()));
                if (data.has("total_purchase")) purchaseOrders.setValue(String.valueOf(data.get("total_purchase").asInt()));
                if (data.has("total_customers")) customers.setValue(String.valueOf(data.get("total_customers").asInt()));
                if (data.has("total_suppliers")) suppliers.setValue(String.valueOf(data.get("total_suppliers").asInt()));
            } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
