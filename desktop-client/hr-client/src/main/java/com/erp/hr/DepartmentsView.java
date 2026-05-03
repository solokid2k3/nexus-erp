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
import java.util.Map;

public class DepartmentsView extends VBox {
    private final ObservableList<Map<String, Object>> items = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public DepartmentsView() {
        setSpacing(16);
        var title = new Label("Departments");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("Add Department");
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
        var descCol = new TableColumn<Map<String, Object>, String>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "description")));

        table.getColumns().addAll(nameCol, descCol);
        table.setPlaceholder(new EmptyState(BytedanceIconsRegularAL.BUILDING_ONE, "No departments", "Create your first department"));
        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/hr/departments").thenAccept(body -> Platform.runLater(() -> {
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
        dialog.setTitle("Add Department");
        var nf = new TextField(); nf.setPromptText("Department name");
        var df = new TextField(); df.setPromptText("Description");
        var form = new VBox(12, new FormField("Name", nf), new FormField("Description", df));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !nf.getText().isBlank()) {
                ApiClient.getInstance().post("/hr/departments",
                    Map.of("name", nf.getText(), "description", df.getText()))
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
