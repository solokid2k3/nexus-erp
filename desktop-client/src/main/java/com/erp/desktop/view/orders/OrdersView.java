package com.erp.desktop.view.orders;

import com.erp.desktop.api.ApiClient;
import com.erp.desktop.component.LoadingSpinner;
import com.erp.desktop.util.CurrencyFormatter;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.*;

public class OrdersView extends ScrollPane {
    private final StackPane toastParent;
    private final ApiClient api = ApiClient.getInstance();
    private final VBox contentRoot = new VBox(20);

    public OrdersView(StackPane toastParent) {
        this.toastParent = toastParent;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentRoot.setPadding(new Insets(32));

        var title = new Label("🛒  Orders");
        title.getStyleClass().add("heading-1");

        var tabBar = new HBox(6);
        var tabs = List.of("📝  Sales Orders", "📥  Purchase Orders", "🏢  Customers", "🏭  Suppliers");
        var buttons = new ArrayList<Button>();
        for (int i = 0; i < tabs.size(); i++) {
            var btn = new Button(tabs.get(i)); btn.getStyleClass().add("badge");
            if (i == 0) btn.getStyleClass().add("badge-info");
            int idx = i;
            btn.setOnAction(e -> { buttons.forEach(b -> b.getStyleClass().remove("badge-info")); btn.getStyleClass().add("badge-info"); showTab(idx); });
            buttons.add(btn); tabBar.getChildren().add(btn);
        }
        contentRoot.getChildren().addAll(title, tabBar);
        setContent(contentRoot);
        showTab(0);
    }

    @SuppressWarnings("unchecked")
    private void showTab(int idx) {
        while (contentRoot.getChildren().size() > 2) contentRoot.getChildren().removeLast();
        switch (idx) {
            case 0 -> loadTable("/orders/sales", "sales_orders",
                    List.of("order_number","customer_id","status","created_at"), List.of("Order #","Customer","Status","Created"), "total_amount_cents");
            case 1 -> loadTable("/orders/purchase", "purchase_orders",
                    List.of("po_number","supplier_id","status","created_at"), List.of("PO #","Supplier","Status","Created"), "total_amount_cents");
            case 2 -> loadTable("/orders/customers", "customers",
                    List.of("name","email","phone","status"), List.of("Name","Email","Phone","Status"), null);
            case 3 -> loadTable("/orders/suppliers", "suppliers",
                    List.of("name","contact_email","phone","status"), List.of("Name","Email","Phone","Status"), null);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadTable(String path, String key, List<String> fields, List<String> headers, String currencyField) {
        var spinner = new LoadingSpinner("Loading...");
        contentRoot.getChildren().add(spinner);

        var table = new TableView<Map<String, Object>>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(500);
        for (int i = 0; i < fields.size(); i++) {
            var f = fields.get(i);
            var c = new TableColumn<Map<String, Object>, String>(headers.get(i));
            c.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get(f) != null ? cd.getValue().get(f).toString() : "—"));
            table.getColumns().add(c);
        }
        if (currencyField != null) {
            var cc = new TableColumn<Map<String, Object>, String>("Amount");
            cc.setCellValueFactory(cd -> { var v = cd.getValue().get(currencyField);
                return v instanceof Number n ? new SimpleStringProperty(CurrencyFormatter.fromCents(n.longValue())) : new SimpleStringProperty("—"); });
            table.getColumns().add(cc);
        }
        table.setPlaceholder(new Label("📭  No records found"));

        api.get(path).thenAccept(resp -> Platform.runLater(() -> {
            contentRoot.getChildren().remove(spinner);
            if (resp.containsKey(key)) {
                var list = (List<Map<String, Object>>) resp.get(key);
                if (list != null) table.getItems().addAll(list);
            }
            var card = new VBox(table); card.getStyleClass().add("card"); card.setPadding(new Insets(0));
            card.setOpacity(0);
            contentRoot.getChildren().add(card);
            var fade = new FadeTransition(Duration.millis(250), card); fade.setFromValue(0); fade.setToValue(1); fade.play();
        }));
    }
}
