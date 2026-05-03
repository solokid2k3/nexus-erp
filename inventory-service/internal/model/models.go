package model

import "time"

type Product struct {
	ID              string            `json:"id"`
	SKU             string            `json:"sku"`
	Name            string            `json:"name"`
	Description     string            `json:"description"`
	CategoryID      string            `json:"category_id"`
	Brand           string            `json:"brand"`
	UnitOfMeasure   string            `json:"unit_of_measure"`
	UnitCostCents   int64             `json:"unit_cost_cents"`
	SellingPriceCents int64           `json:"selling_price_cents"`
	Currency        string            `json:"currency"`
	WeightKg        float64           `json:"weight_kg"`
	Attributes      map[string]string `json:"attributes"`
	Status          string            `json:"status"`
	ReorderPoint    int32             `json:"reorder_point"`
	ReorderQuantity int32             `json:"reorder_quantity"`
	LeadTimeDays    int32             `json:"lead_time_days"`
	SupplierID      string            `json:"supplier_id"`
	TaxCategory     string            `json:"tax_category"`
	CreatedBy       string            `json:"created_by"`
	CreatedAt       time.Time         `json:"created_at"`
	UpdatedBy       string            `json:"updated_by"`
	UpdatedAt       time.Time         `json:"updated_at"`
}

type Category struct {
	ID           string    `json:"id"`
	Name         string    `json:"name"`
	Description  string    `json:"description"`
	ParentID     string    `json:"parent_id"`
	ProductCount int32     `json:"product_count"`
	CreatedAt    time.Time `json:"created_at"`
}

type Warehouse struct {
	ID            string    `json:"id"`
	Name          string    `json:"name"`
	Code          string    `json:"code"`
	City          string    `json:"city"`
	Country       string    `json:"country"`
	Status        string    `json:"status"`
	CapacityUnits int64     `json:"capacity_units"`
	UsedUnits     int64     `json:"used_units"`
	CreatedAt     time.Time `json:"created_at"`
}

type StockLevel struct {
	ProductID        string    `json:"product_id"`
	WarehouseID      string    `json:"warehouse_id"`
	QuantityOnHand   int32     `json:"quantity_on_hand"`
	QuantityReserved int32     `json:"quantity_reserved"`
	QuantityAvailable int32   `json:"quantity_available"`
	QuantityIncoming int32     `json:"quantity_incoming"`
	BinLocation      string    `json:"bin_location"`
	LastCounted      time.Time `json:"last_counted"`
}

type StockMovement struct {
	ID             string    `json:"id"`
	ProductID      string    `json:"product_id"`
	WarehouseID    string    `json:"warehouse_id"`
	Reason         string    `json:"reason"`
	QuantityChange int32     `json:"quantity_change"`
	QuantityBefore int32     `json:"quantity_before"`
	QuantityAfter  int32     `json:"quantity_after"`
	ReferenceID    string    `json:"reference_id"`
	Notes          string    `json:"notes"`
	CreatedBy      string    `json:"created_by"`
	CreatedAt      time.Time `json:"created_at"`
}

type StockReservation struct {
	ID          string    `json:"id"`
	ProductID   string    `json:"product_id"`
	WarehouseID string    `json:"warehouse_id"`
	OrderID     string    `json:"order_id"`
	Quantity    int32     `json:"quantity"`
	Status      string    `json:"status"`
	ReservedBy  string    `json:"reserved_by"`
	CreatedAt   time.Time `json:"created_at"`
}

type LowStockAlert struct {
	ProductID     string `json:"product_id"`
	ProductName   string `json:"product_name"`
	SKU           string `json:"sku"`
	WarehouseID   string `json:"warehouse_id"`
	WarehouseName string `json:"warehouse_name"`
	CurrentQty    int32  `json:"current_quantity"`
	ReorderPoint  int32  `json:"reorder_point"`
	ReorderQty    int32  `json:"reorder_quantity"`
	Severity      string `json:"severity"`
}
