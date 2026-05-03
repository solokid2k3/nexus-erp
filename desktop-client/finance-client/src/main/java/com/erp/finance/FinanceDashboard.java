package com.erp.finance;

import com.erp.core.api.ApiClient;
import com.erp.core.component.StatCard;
import com.erp.core.component.SkeletonPane;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class FinanceDashboard extends VBox {
    public FinanceDashboard() {
        setSpacing(24);
        var title = new Label("Dashboard"); title.getStyleClass().add("display-heading");
        var statsRow = new HBox(16);
        var accounts = new StatCard("Accounts", "—", FontAwesomeSolid.UNIVERSITY, "#6366F1");
        var journals = new StatCard("Journal Entries", "—", FontAwesomeSolid.BOOK, "#10B981");
        var invoices = new StatCard("Pending Invoices", "—", FontAwesomeSolid.FILE_INVOICE_DOLLAR, "#F59E0B");
        var budgets = new StatCard("Budgets", "—", FontAwesomeSolid.MONEY_CHECK_ALT, "#3B82F6");
        statsRow.getChildren().addAll(accounts, journals, invoices, budgets);
        var skeleton = new SkeletonPane(3);
        getChildren().addAll(title, statsRow, skeleton);

        var api = ApiClient.getInstance();
        api.getRaw("/finance/accounts").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) accounts.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/journal-entries").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) journals.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/invoices").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) invoices.setValue(String.valueOf(d.size())); } catch (Exception ignored) {} }));
        api.getRaw("/finance/budgets").thenAccept(b -> Platform.runLater(() -> {
            try { var d = api.getMapper().readTree(b); if (d.isArray()) budgets.setValue(String.valueOf(d.size())); } catch (Exception ignored) {}
            getChildren().remove(skeleton);
        })).exceptionally(ex -> { Platform.runLater(() -> getChildren().remove(skeleton)); return null; });
    }
}
