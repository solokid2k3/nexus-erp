package com.erp.inventory;

import com.erp.core.app.BaseApp;
import javafx.scene.Node;

public class InventoryApp extends BaseApp {

    @Override
    protected String getAppTitle() {
        return "📦 Nexus Inventory";
    }

    @Override
    protected Node createMainView() {
        return new InventoryMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
