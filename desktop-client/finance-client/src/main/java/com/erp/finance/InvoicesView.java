package com.erp.finance;

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
import java.util.Map;

public class InvoicesView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public InvoicesView() {
        setSpacing(16);
        var title = new Label("Invoices"); title.getStyleClass().add("display-heading");
        var addBtn = new Button("New Invoice"); addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD); ai.setIconSize(14); ai.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF")); addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());
        var toolbar = new DataToolbar(new SearchField("Search invoices..."), addBtn);
        var table = new TableView<>(items); VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        var idCol = new TableColumn<Map<String, Object>, String>("Invoice #"); idCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "id")));
        var custCol = new TableColumn<Map<String, Object>, String>("Customer"); custCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "customer_name")));
        var dateCol = new TableColumn<Map<String, Object>, String>("Date"); dateCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "date")));
        var amtCol = new TableColumn<Map<String, Object>, String>("Amount"); amtCol.setCellValueFactory(c -> { var v = c.getValue().get("amount"); return new SimpleStringProperty(v != null ? "$" + v : "—"); });
        var statusCol = new TableColumn<Map<String, Object>, String>("Status"); statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() { @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty); if (empty || item == null) { setGraphic(null); setText(null); return; } setGraphic(StatusBadge.forStatus(item)); setText(null); }});
        table.getColumns().addAll(idCol, custCol, dateCol, amtCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.BILL, "No invoices", "Create your first invoice"));
        getChildren().addAll(title, toolbar, table); loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() { ApiClient.getInstance().getRaw("/finance/invoices").thenAccept(body -> Platform.runLater(() -> {
        try { var api = ApiClient.getInstance(); var d = api.getMapper().readTree(body); items.clear();
            if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class)); } catch (Exception ignored) {} })); }

    private void showCreate() {
        var dialog = new Dialog<Void>(); dialog.setTitle("New Invoice");
        var cf = new TextField(); cf.setPromptText("Customer name"); var af = new TextField(); af.setPromptText("Amount");
        var form = new VBox(12, new FormField("Customer", cf), new FormField("Amount", af)); form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> { if (bt == ButtonType.OK) { ApiClient.getInstance().post("/finance/invoices",
            Map.of("customer_name", cf.getText(), "amount", af.getText())).thenAccept(r -> Platform.runLater(this::loadData)); } return null; });
        dialog.showAndWait(); }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
