package model

import "time"

type Customer struct {
	ID                   string    `json:"id"`
	Code                 string    `json:"code"`
	CompanyName          string    `json:"company_name"`
	ContactName          string    `json:"contact_name"`
	Email                string    `json:"email"`
	Phone                string    `json:"phone"`
	BillingStreet        string    `json:"billing_street"`
	BillingCity          string    `json:"billing_city"`
	BillingState         string    `json:"billing_state"`
	BillingPostal        string    `json:"billing_postal"`
	BillingCountry       string    `json:"billing_country"`
	ShippingStreet       string    `json:"shipping_street"`
	ShippingCity         string    `json:"shipping_city"`
	ShippingState        string    `json:"shipping_state"`
	ShippingPostal       string    `json:"shipping_postal"`
	ShippingCountry      string    `json:"shipping_country"`
	TaxID                string    `json:"tax_id"`
	PaymentTerms         string    `json:"payment_terms"`
	CreditLimitCents     int64     `json:"credit_limit_cents"`
	OutstandingCents     int64     `json:"outstanding_balance_cents"`
	Currency             string    `json:"currency"`
	Status               string    `json:"status"`
	Tier                 string    `json:"tier"`
	DiscountPercent      float64   `json:"discount_percent"`
	CreatedAt            time.Time `json:"created_at"`
}

type Supplier struct {
	ID           string    `json:"id"`
	Code         string    `json:"code"`
	CompanyName  string    `json:"company_name"`
	ContactName  string    `json:"contact_name"`
	Email        string    `json:"email"`
	Phone        string    `json:"phone"`
	TaxID        string    `json:"tax_id"`
	PaymentTerms string    `json:"payment_terms"`
	Rating       string    `json:"rating"`
	Status       string    `json:"status"`
	CreatedAt    time.Time `json:"created_at"`
}

type SalesOrder struct {
	ID                string       `json:"id"`
	OrderNumber       string       `json:"order_number"`
	CustomerID        string       `json:"customer_id"`
	CustomerName      string       `json:"customer_name"`
	Status            string       `json:"status"`
	Priority          string       `json:"priority"`
	Lines             []*OrderLine `json:"lines"`
	ShippingMethod    string       `json:"shipping_method"`
	SubtotalCents     int64        `json:"subtotal_cents"`
	TaxAmountCents    int64        `json:"tax_amount_cents"`
	ShippingCostCents int64        `json:"shipping_cost_cents"`
	DiscountCents     int64        `json:"discount_amount_cents"`
	TotalAmountCents  int64        `json:"total_amount_cents"`
	Currency          string       `json:"currency"`
	PaymentTerms      string       `json:"payment_terms"`
	Notes             string       `json:"notes"`
	WarehouseID       string       `json:"warehouse_id"`
	OrderDate         time.Time    `json:"order_date"`
	RequiredDate      time.Time    `json:"required_date"`
	ShippedDate       time.Time    `json:"shipped_date"`
	InvoiceID         string       `json:"invoice_id"`
	CreatedBy         string       `json:"created_by"`
	CreatedAt         time.Time    `json:"created_at"`
}

type OrderLine struct {
	ID               string  `json:"id"`
	LineNumber       int32   `json:"line_number"`
	ProductID        string  `json:"product_id"`
	ProductName      string  `json:"product_name"`
	SKU              string  `json:"sku"`
	QuantityOrdered  int32   `json:"quantity_ordered"`
	QuantityShipped  int32   `json:"quantity_shipped"`
	UnitPriceCents   int64   `json:"unit_price_cents"`
	DiscountPercent  float64 `json:"discount_percent"`
	LineTotalCents   int64   `json:"line_total_cents"`
	TaxAmountCents   int64   `json:"tax_amount_cents"`
	TaxCategory      string  `json:"tax_category"`
}

type PurchaseOrder struct {
	ID               string               `json:"id"`
	PONumber         string               `json:"po_number"`
	SupplierID       string               `json:"supplier_id"`
	SupplierName     string               `json:"supplier_name"`
	Status           string               `json:"status"`
	Lines            []*PurchaseOrderLine  `json:"lines"`
	SubtotalCents    int64                `json:"subtotal_cents"`
	TaxAmountCents   int64                `json:"tax_amount_cents"`
	TotalAmountCents int64                `json:"total_amount_cents"`
	Currency         string               `json:"currency"`
	PaymentTerms     string               `json:"payment_terms"`
	DeliveryTerms    string               `json:"delivery_terms"`
	WarehouseID      string               `json:"warehouse_id"`
	OrderDate        time.Time            `json:"order_date"`
	ExpectedDelivery time.Time            `json:"expected_delivery"`
	Notes            string               `json:"notes"`
	CreatedBy        string               `json:"created_by"`
	CreatedAt        time.Time            `json:"created_at"`
}

type PurchaseOrderLine struct {
	ID               string `json:"id"`
	LineNumber       int32  `json:"line_number"`
	ProductID        string `json:"product_id"`
	ProductName      string `json:"product_name"`
	QuantityOrdered  int32  `json:"quantity_ordered"`
	QuantityReceived int32  `json:"quantity_received"`
	UnitCostCents    int64  `json:"unit_cost_cents"`
	LineTotalCents   int64  `json:"line_total_cents"`
}

type OrderSummary struct {
	TotalOrders      int32 `json:"total_orders"`
	PendingOrders    int32 `json:"pending_orders"`
	ProcessingOrders int32 `json:"processing_orders"`
	ShippedOrders    int32 `json:"shipped_orders"`
	CancelledOrders  int32 `json:"cancelled_orders"`
	TotalRevenueCents int64 `json:"total_revenue_cents"`
	AvgOrderCents    int64 `json:"avg_order_value_cents"`
	FulfillmentRate  float64 `json:"fulfillment_rate"`
}
