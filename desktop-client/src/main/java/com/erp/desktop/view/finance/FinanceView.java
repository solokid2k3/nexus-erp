package com.erp.desktop.view.finance;

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

public class FinanceView extends ScrollPane {
    private final StackPane toastParent;
    private final ApiClient api = ApiClient.getInstance();
    private final VBox contentRoot = new VBox(20);

    public FinanceView(StackPane toastParent) {
        this.toastParent = toastParent;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        contentRoot.setPadding(new Insets(32));

        var title = new Label("💰  Finance");
        title.getStyleClass().add("heading-1");

        var tabBar = new HBox(6);
        var tabs = List.of("📊  Accounts", "📒  Journals", "🧾  Invoices", "📈  Reports", "💵  Budgets");
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
            case 0 -> loadTable("/finance/accounts", "accounts",
                    List.of("account_number","name","type","sub_type"), List.of("Acct #","Name","Type","Sub Type"), "balance_cents");
            case 1 -> loadTable("/finance/journal-entries", "journal_entries",
                    List.of("entry_number","date","description","status"), List.of("Entry #","Date","Description","Status"), "total_debit_cents");
            case 2 -> loadTable("/finance/invoices", "invoices",
                    List.of("invoice_number","customer_id","due_date","status"), List.of("Invoice #","Customer","Due Date","Status"), "total_cents");
            case 3 -> showReports();
            case 4 -> loadTable("/finance/budgets", "budgets",
                    List.of("name","fiscal_year","status"), List.of("Name","Fiscal Year","Status"), "total_amount_cents");
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

    private void showReports() {
        var grid = new FlowPane(12, 12);

        var reports = List.of(
                Map.entry("📊  Trial Balance", "/finance/reports/trial-balance"),
                Map.entry("💹  Profit & Loss", "/finance/reports/profit-loss"),
                Map.entry("🏦  Balance Sheet", "/finance/reports/balance-sheet"),
                Map.entry("📋  AR Aging", "/finance/reports/ar-aging")
        );

        for (var r : reports) {
            var btn = new Button(r.getKey());
            btn.getStyleClass().add("button-secondary");
            btn.setPrefWidth(200);
            btn.setPrefHeight(80);
            btn.setOnAction(e -> loadReport(r.getValue(), r.getKey()));
            grid.getChildren().add(btn);
        }

        var card = new VBox(12, new Label("Select a report to view"), grid);
        card.getStyleClass().add("card");
        contentRoot.getChildren().add(card);
        fadeIn(card);
    }

    private void loadReport(String path, String name) {
        while (contentRoot.getChildren().size() > 2) contentRoot.getChildren().removeLast();
        var spinner = new LoadingSpinner("Generating " + name + "...");
        contentRoot.getChildren().add(spinner);

        api.getRaw(path).thenAccept(json -> Platform.runLater(() -> {
            contentRoot.getChildren().remove(spinner);
            var ta = new TextArea(json); ta.setEditable(false); ta.setPrefHeight(500); ta.setWrapText(true);
            ta.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
            var card = new VBox(new Label(name), ta); card.getStyleClass().add("card"); card.setSpacing(8);
            contentRoot.getChildren().add(card);
            fadeIn(card);
        }));
    }

    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        var f = new FadeTransition(Duration.millis(250), node); f.setFromValue(0); f.setToValue(1); f.play();
    }
}
