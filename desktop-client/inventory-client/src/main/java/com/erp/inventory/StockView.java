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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;
import java.util.Map;

public class StockView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public StockView() {
        setSpacing(16);
        var title = new Label("Stock Movements"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("New Movement"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setStyle("-fx-icon-color:#FFF;"); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search stock..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var prodCol = new TableColumn<Map<String, Object>, String>("Product"); prodCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "product_name")));
        var typeCol = new TableColumn<Map<String, Object>, String>("Type"); typeCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "movement_type")));
        typeCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        var qtyCol = new TableColumn<Map<String, Object>, String>("Quantity"); qtyCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "quantity")));
        var whCol = new TableColumn<Map<String, Object>, String>("Warehouse"); whCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "warehouse_name")));
        var dateCol = new TableColumn<Map<String, Object>, String>("Date"); dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "created_at")));
        table.getColumns().addAll(prodCol, typeCol, qtyCol, whCol, dateCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.TRANSFER, "No stock movements", "Record a movement", "New Movement", this::showCreate));
        getChildren().addAll(title, toolbar, table); loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/inventory/stock").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }

    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("New Stock Movement");
        var pf = new TextField(); pf.setPromptText("Product ID"); var qf = new TextField(); qf.setPromptText("Quantity"); var tf = new TextField(); tf.setPromptText("Type (in/out/transfer)");
        var form = new VBox(12, new FormField("Product ID", pf), new FormField("Quantity", qf), new FormField("Type", tf));
        form.setPadding(new Insets(16)); dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK) { ApiClient.getInstance().post("/inventory/stock",
            Map.of("product_id", pf.getText(), "quantity", qf.getText(), "movement_type", tf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData)); } return null; }); dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
