package com.erp.finance;

import com.erp.core.app.BaseApp;
import javafx.scene.Node;

public class FinanceApp extends BaseApp {

    @Override
    protected String getAppTitle() {
        return "💰 Nexus Finance";
    }

    @Override
    protected Node createMainView() {
        return new FinanceMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
