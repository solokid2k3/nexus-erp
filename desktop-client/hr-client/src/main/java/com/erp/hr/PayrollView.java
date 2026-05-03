package com.erp.hr;

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

public class PayrollView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public PayrollView() {
        setSpacing(16);
        var title = new Label("Payroll");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("New Run");
        addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD);
        ai.setIconSize(14); ai.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF"));
        addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());

        var toolbar = new DataToolbar(null, addBtn);
        var table = new TableView<>(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var idCol = new TableColumn<Map<String, Object>, String>("Run ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "id")));
        var periodCol = new TableColumn<Map<String, Object>, String>("Period");
        periodCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "period")));
        var totalCol = new TableColumn<Map<String, Object>, String>("Total");
        totalCol.setCellValueFactory(c -> {
            var v = c.getValue().get("total_amount");
            return new SimpleStringProperty(v != null ? "$" + v : "—");
        });
        var statusCol = new TableColumn<Map<String, Object>, String>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "status")));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); setText(null); return; }
                setGraphic(StatusBadge.forStatus(item)); setText(null);
            }
        });

        table.getColumns().addAll(idCol, periodCol, totalCol, statusCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularMZ.WALLET, "No payroll runs", "Create your first payroll run"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/hr/payroll").thenAccept(body -> Platform.runLater(() -> {
            try {
                var api = ApiClient.getInstance();
                var d = api.getMapper().readTree(body);
                items.clear();
                if (d.isArray()) for (var n : d) items.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
    }

    private void showCreate() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("New Payroll Run");
        var pf = new TextField(); pf.setPromptText("Period (e.g. 2026-05)");
        var form = new VBox(12, new FormField("Period", pf));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !pf.getText().isBlank()) {
                ApiClient.getInstance().post("/hr/payroll", Map.of("period", pf.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private String str(Map<String, Object> m, String k) {
        var v = m.get(k); return v != null ? v.toString() : "—";
    }
}
