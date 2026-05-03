package com.erp.inventory;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularAL;
import org.kordamp.ikonli.bytedance.BytedanceIconsRegularMZ;

import java.util.List;

public class InventoryMainView extends MainShell {

    public InventoryMainView() {
        super("Inventory", BytedanceIconsRegularMZ.WAREHOUSING, List.of(
                new NavItem("Dashboard", BytedanceIconsRegularAL.DASHBOARD, InventoryDashboard::new),
                new NavItem("Products", BytedanceIconsRegularMZ.TAG_ONE, ProductsView::new),
                new NavItem("Stock", BytedanceIconsRegularAL.BOX, StockView::new),
                new NavItem("Categories", BytedanceIconsRegularAL.FOLDER_CLOSE, CategoriesView::new),
                new NavItem("Warehouses", BytedanceIconsRegularMZ.WAREHOUSING, WarehousesView::new)
        ), null);
    }
}
