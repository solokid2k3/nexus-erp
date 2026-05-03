package com.erp.orders;

import com.erp.core.app.BaseApp;
import javafx.scene.Node;

public class OrdersApp extends BaseApp {

    @Override
    protected String getAppTitle() {
        return "🛒 Nexus Orders";
    }

    @Override
    protected Node createMainView() {
        return new OrdersMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
