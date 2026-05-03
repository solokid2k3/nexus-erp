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

public class CategoriesView extends VBox {

    private final ObservableList<Map<String, Object>> categories = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public CategoriesView() {
        setSpacing(16);
        var title = new Label("Categories");
        title.getStyleClass().add("display-heading");

        var addBtn = new Button("Add Category");
        addBtn.getStyleClass().add("button-primary");
        addBtn.setGraphic(createIcon(FontAwesomeSolid.PLUS, "#FFFFFF"));
        addBtn.setOnAction(e -> showCreateDialog());

        var toolbar = new DataToolbar(new SearchField("Search categories..."), addBtn);

        var table = new TableView<>(categories);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var nameCol = new TableColumn<Map<String, Object>, String>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "name")));
        var descCol = new TableColumn<Map<String, Object>, String>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue(), "description")));
        table.getColumns().addAll(nameCol, descCol);

        table.setPlaceholder(new EmptyState(FontAwesomeSolid.FOLDER, "No categories", "Create your first category", "Add Category", this::showCreateDialog));

        getChildren().addAll(title, toolbar, table);
        loadData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        ApiClient.getInstance().getRaw("/inventory/categories").thenAccept(body -> Platform.runLater(() -> {
            try {
                var api = ApiClient.getInstance();
                var data = api.getMapper().readTree(body);
                categories.clear();
                if (data.isArray()) for (var n : data) categories.add(api.getMapper().convertValue(n, Map.class));
            } catch (Exception ignored) {}
        }));
    }

    private void showCreateDialog() {
        var dialog = new Dialog<Void>();
        dialog.setTitle("Add Category");
        var nameField = new TextField(); nameField.setPromptText("Category name");
        var descField = new TextField(); descField.setPromptText("Description");
        var form = new VBox(12, new FormField("Name", nameField), new FormField("Description", descField));
        form.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK && !nameField.getText().isBlank()) {
                ApiClient.getInstance().post("/inventory/categories", Map.of("name", nameField.getText(), "description", descField.getText()))
                        .thenAccept(r -> Platform.runLater(this::loadData));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private FontIcon createIcon(FontAwesomeSolid icon, String color) { var fi = new FontIcon(icon); fi.setIconSize(12); fi.setStyle("-fx-icon-color: " + color + ";"); return fi; }
    private String str(Map<String, Object> m, String k) { var v = m.get(k); return v != null ? v.toString() : "—"; }
}
