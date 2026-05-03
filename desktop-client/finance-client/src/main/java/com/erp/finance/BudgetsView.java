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

public class BudgetsView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public BudgetsView() {
        setSpacing(16);
        var title = new Label("Budgets");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("New Budget");
        addBtn.getStyleClass().add("button-primary");
        var ai = new FontIcon(BytedanceIconsRegularAL.ADD);
        ai.setIconSize(14); ai.setIconColor(javafx.scene.paint.Paint.valueOf("#FFFFFF"));
        addBtn.setGraphic(ai);
        addBtn.setOnAction(e -> showCreate());

        var toolbar = new DataToolbar(null, addBtn);
        var table = new TableView<>(items);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var nameCol = new TableColumn<Map<String, Object>, String>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var periodCol = new TableColumn<Map<String, Object>, String>("Period");
        periodCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "period")));
        var amtCol = new TableColumn<Map<String, Object>, String>("Amount");
        amtCol.setCellValueFactory(c -> {
            var v = c.getValue().get("amount");
            return new SimpleStringProperty(v != null ? "$" + v : "—");
        });

        table.getColumns().addAll(nameCol, periodCol, amtCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.FUNDS, "No budgets", "Create your first budget"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/finance/budgets").thenAccept(body -> Platform.runLater(() -> {
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
        dialog.setTitle("New Budget");
        var nf = new TextField(); nf.setPromptText("Budget name");
        var pf = new TextField(); pf.setPromptText("Period");
        var af = new TextField(); af.setPromptText("Amount");
        var form = new VBox(12, new FormField("Name", nf), new FormField("Period", pf), new FormField("Amount", af));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !nf.getText().isBlank()) {
                ApiClient.getInstance().post("/finance/budgets",
                    Map.of("name", nf.getText(), "period", pf.getText(), "amount", af.getText()))
                    .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private String str(Map<String, Object> m, String k) {
        var v = m.get(k);
        return v != null ? v.toString() : "—";
    }
}
