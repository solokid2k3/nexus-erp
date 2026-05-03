package com.erp.inventory;

import com.erp.core.component.NavItem;
import com.erp.core.view.MainShell;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.util.List;

public class InventoryMainView extends MainShell {

    public InventoryMainView() {
        super("Inventory", FontAwesomeSolid.BOXES, List.of(
                new NavItem("Dashboard", FontAwesomeSolid.TACHOMETER_ALT, InventoryDashboard::new),
                new NavItem("Products", FontAwesomeSolid.TAG, ProductsView::new),
                new NavItem("Stock", FontAwesomeSolid.WAREHOUSE, StockView::new),
                new NavItem("Categories", FontAwesomeSolid.FOLDER, CategoriesView::new),
                new NavItem("Warehouses", FontAwesomeSolid.BUILDING, WarehousesView::new)
        ), null);
    }
}
