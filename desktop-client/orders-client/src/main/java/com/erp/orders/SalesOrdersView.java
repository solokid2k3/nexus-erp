package com.erp.orders;

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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;
import java.util.Map;

public class SalesOrdersView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();
    @SuppressWarnings("unchecked")
    public SalesOrdersView() {
        setSpacing(16);
        var title = new Label("Sales Orders"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("New Order"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setStyle("-fx-icon-color:#FFF;"); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search orders..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var idCol = new TableColumn<Map<String, Object>, String>("Order #"); idCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "id")));
        var custCol = new TableColumn<Map<String, Object>, String>("Customer"); custCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "customer_name")));
        var dateCol = new TableColumn<Map<String, Object>, String>("Date"); dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "order_date")));
        var totalCol = new TableColumn<Map<String, Object>, String>("Total"); totalCol.setCellValueFactory(c -> { var v = c.getValue().get("total"); return new SimpleStringProperty(v != null ? "$" + v : "—"); });
        var statusCol = new TableColumn<Map<String, Object>, String>("Status"); statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        table.getColumns().addAll(idCol, custCol, dateCol, totalCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.SHOPPING_CART, "No sales orders", "Create your first order"));
        getChildren().addAll(title, toolbar, table); loadData();
    }
    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/orders/sales").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }
    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("New Sales Order");
        var cf = new TextField(); cf.setPromptText("Customer name"); var tf = new TextField(); tf.setPromptText("Total");
        var form = new VBox(12, new FormField("Customer", cf), new FormField("Total", tf)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK) { ApiClient.getInstance().post("/orders/sales",
            Map.of("customer_name", cf.getText(), "total", tf.getText())).thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
