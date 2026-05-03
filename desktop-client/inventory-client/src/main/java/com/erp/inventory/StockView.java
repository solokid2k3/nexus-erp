package com.erp.inventory;

import com.erp.core.api.ApiClient;
import com.erp.core.component.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

public class StockView extends VBox {

    private final ObservableList<Map<String, Object>> alerts = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> movements = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public StockView() {
        setSpacing(16);

        var title = new Label("Stock Management");
        title.getStyleClass().add("display-heading");

        // Action buttons
        var adjustBtn = new Button("Adjust Stock");
        adjustBtn.getStyleClass().add("button-secondary");
        adjustBtn.setGraphic(createIcon(FontAwesomeSolid.SLIDERS_H, "#374151"));
        adjustBtn.setOnAction(e -> showAdjustDialog());

        var transferBtn = new Button("Transfer");
        transferBtn.getStyleClass().add("button-secondary");
        transferBtn.setGraphic(createIcon(FontAwesomeSolid.EXCHANGE_ALT, "#374151"));
        transferBtn.setOnAction(e -> showTransferDialog());

        var toolbar = new DataToolbar(null, adjustBtn, transferBtn);

        // Alerts section
        var alertsLabel = new Label("Low Stock Alerts");
        alertsLabel.getStyleClass().add("heading-2");

        var alertsTable = new TableView<>(alerts);
        alertsTable.setMaxHeight(250);
        var aNameCol = new TableColumn<Map<String, Object>, String>("Product");
        aNameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "product_name")));
        var aQtyCol = new TableColumn<Map<String, Object>, String>("Current Stock");
        aQtyCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "quantity")));
        var aMinCol = new TableColumn<Map<String, Object>, String>("Min Threshold");
        aMinCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "min_stock")));
        alertsTable.getColumns().addAll(aNameCol, aQtyCol, aMinCol);
        alertsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        alertsTable.setPlaceholder(new EmptyState(FontAwesomeSolid.CHECK_CIRCLE, "All good!", "No low stock alerts"));

        // Movements section
        var movLabel = new Label("Stock Movements");
        movLabel.getStyleClass().add("heading-2");

        var movTable = new TableView<>(movements);
        VBox.setVgrow(movTable, Priority.ALWAYS);
        var mTypeCol = new TableColumn<Map<String, Object>, String>("Type");
        mTypeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "type")));
        var mProductCol = new TableColumn<Map<String, Object>, String>("Product");
        mProductCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "product_name")));
        var mQtyCol = new TableColumn<Map<String, Object>, String>("Quantity");
        mQtyCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "quantity")));
        var mReasonCol = new TableColumn<Map<String, Object>, String>("Reason");
        mReasonCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "reason")));
        movTable.getColumns().addAll(mTypeCol, mProductCol, mQtyCol, mReasonCol);
        movTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        movTable.setPlaceholder(new EmptyState(FontAwesomeSolid.EXCHANGE_ALT, "No movements", "Stock movements will appear here"));

        getChildren().addAll(title, toolbar, alertsLabel, alertsTable, movLabel, movTable);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        var api = ApiClient.getInstance();
        api.getRaw("/inventory/stock/alerts").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                alerts.clear();
                if (data.isArray()) for (var n : data) alerts.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
        api.getRaw("/inventory/stock/movements").thenAccept(body -> Platform.runLater(() -> {
            try {
                var data = api.getMapper().readTree(body);
                movements.clear();
                if (data.isArray()) for (var n : data) movements.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
    }

    private void showAdjustDialog() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Adjust Stock");
        var pidField = new TextField(); pidField.setPromptText("Product ID");
        var widField = new TextField(); widField.setPromptText("Warehouse ID");
        var qtyField = new TextField(); qtyField.setPromptText("Quantity (+/-)");
        var reasonField = new TextField(); reasonField.setPromptText("Reason");
        var form = new VBox(12, new FormField("Product ID", pidField), new FormField("Warehouse ID", widField),
                new FormField("Quantity", qtyField), new FormField("Reason", reasonField));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> null);
        dialog.showAndWait();
    }

    private void showTransferDialog() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Transfer Stock");
        var pidField = new TextField(); pidField.setPromptText("Product ID");
        var fromField = new TextField(); fromField.setPromptText("From Warehouse");
        var toField = new TextField(); toField.setPromptText("To Warehouse");
        var qtyField = new TextField(); qtyField.setPromptText("Quantity");
        var form = new VBox(12, new FormField("Product ID", pidField), new FormField("From", fromField),
                new FormField("To", toField), new FormField("Quantity", qtyField));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> null);
        dialog.showAndWait();
    }

    private FontIcon createIcon(FontAwesomeSolid icon, String color) {
        var fi = new FontIcon(icon); fi.setIconSize(13); fi.setStyle("-fx-icon-color: " + color + ";"); return fi;
    }
    private String str(Map<String, Object> map, String key) { var v = map.get(key); return v != null ? v.toString() : "—"; }
}
