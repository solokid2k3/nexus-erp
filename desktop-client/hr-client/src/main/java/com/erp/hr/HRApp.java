package com.erp.hr;

import com.erp.core.app.BaseApp;
import javafx.scene.Node;

public class HRApp extends BaseApp {

    @Override
    protected String getAppTitle() {
        return "👥 Nexus HR";
    }

    @Override
    protected Node createMainView() {
        return new HRMainView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
