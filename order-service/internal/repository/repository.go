package repository

import (
	"context"
	"fmt"
	"time"

	"erp-system/order-service/internal/model"

	"github.com/jackc/pgx/v5/pgxpool"
)

type OrderRepository struct {
	pool *pgxpool.Pool
}

func NewOrderRepository(pool *pgxpool.Pool) *OrderRepository {
	return &OrderRepository{pool: pool}
}

// ============================================================
// Customer Operations
// ============================================================

func (r *OrderRepository) CreateCustomer(ctx context.Context, c *model.Customer) error {
	return r.pool.QueryRow(ctx, `
		INSERT INTO orders.customers (code, company_name, contact_name, email, phone,
			billing_street, billing_city, billing_state, billing_postal, billing_country,
			shipping_street, shipping_city, shipping_state, shipping_postal, shipping_country,
			tax_id, payment_terms, credit_limit_cents, currency, status, tier, discount_percent, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23)
		RETURNING id, created_at`,
		c.Code, c.CompanyName, c.ContactName, c.Email, c.Phone,
		c.BillingStreet, c.BillingCity, c.BillingState, c.BillingPostal, c.BillingCountry,
		c.ShippingStreet, c.ShippingCity, c.ShippingState, c.ShippingPostal, c.ShippingCountry,
		c.TaxID, c.PaymentTerms, c.CreditLimitCents, c.Currency, c.Status, c.Tier, c.DiscountPercent, "system",
	).Scan(&c.ID, &c.CreatedAt)
}

func (r *OrderRepository) GetCustomer(ctx context.Context, id string) (*model.Customer, error) {
	c := &model.Customer{}
	err := r.pool.QueryRow(ctx, `
		SELECT id, code, company_name, contact_name, email, phone,
			billing_street, billing_city, billing_state, billing_postal, billing_country,
			shipping_street, shipping_city, shipping_state, shipping_postal, shipping_country,
			tax_id, payment_terms, credit_limit_cents, outstanding_balance_cents, currency,
			status, tier, discount_percent, created_at
		FROM orders.customers WHERE id = $1`, id,
	).Scan(&c.ID, &c.Code, &c.CompanyName, &c.ContactName, &c.Email, &c.Phone,
		&c.BillingStreet, &c.BillingCity, &c.BillingState, &c.BillingPostal, &c.BillingCountry,
		&c.ShippingStreet, &c.ShippingCity, &c.ShippingState, &c.ShippingPostal, &c.ShippingCountry,
		&c.TaxID, &c.PaymentTerms, &c.CreditLimitCents, &c.OutstandingCents, &c.Currency,
		&c.Status, &c.Tier, &c.DiscountPercent, &c.CreatedAt)
	return c, err
}

// ============================================================
// Sales Order Operations
// ============================================================

func (r *OrderRepository) CreateSalesOrder(ctx context.Context, o *model.SalesOrder) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	// Generate order number
	var seq int
	tx.QueryRow(ctx, `SELECT COALESCE(MAX(CAST(SUBSTRING(order_number FROM 4) AS INTEGER)), 0) + 1 FROM orders.sales_orders`).Scan(&seq)
	o.OrderNumber = fmt.Sprintf("SO-%06d", seq)

	err = tx.QueryRow(ctx, `
		INSERT INTO orders.sales_orders (order_number, customer_id, status, priority,
			shipping_method, subtotal_cents, tax_amount_cents, shipping_cost_cents,
			discount_amount_cents, total_amount_cents, currency, payment_terms, notes,
			warehouse_id, required_date, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16)
		RETURNING id, order_date, created_at`,
		o.OrderNumber, o.CustomerID, o.Status, o.Priority,
		o.ShippingMethod, o.SubtotalCents, o.TaxAmountCents, o.ShippingCostCents,
		o.DiscountCents, o.TotalAmountCents, o.Currency, o.PaymentTerms, o.Notes,
		nullStr(o.WarehouseID), nullTime(o.RequiredDate), o.CreatedBy,
	).Scan(&o.ID, &o.OrderDate, &o.CreatedAt)
	if err != nil {
		return err
	}

	// Insert order lines
	for i, line := range o.Lines {
		line.LineNumber = int32(i + 1)
		err = tx.QueryRow(ctx, `
			INSERT INTO orders.order_lines (order_id, line_number, product_id, product_name, sku,
				quantity_ordered, unit_price_cents, discount_percent, line_total_cents, tax_amount_cents,
				tax_category, currency)
			VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12) RETURNING id`,
			o.ID, line.LineNumber, line.ProductID, line.ProductName, line.SKU,
			line.QuantityOrdered, line.UnitPriceCents, line.DiscountPercent,
			line.LineTotalCents, line.TaxAmountCents, line.TaxCategory, o.Currency,
		).Scan(&line.ID)
		if err != nil {
			return err
		}
	}

	return tx.Commit(ctx)
}

func (r *OrderRepository) GetSalesOrder(ctx context.Context, id string) (*model.SalesOrder, error) {
	o := &model.SalesOrder{}
	err := r.pool.QueryRow(ctx, `
		SELECT so.id, so.order_number, so.customer_id, c.company_name, so.status, so.priority,
			so.shipping_method, so.subtotal_cents, so.tax_amount_cents, so.shipping_cost_cents,
			so.discount_amount_cents, so.total_amount_cents, so.currency, so.payment_terms,
			so.notes, COALESCE(so.warehouse_id::text,''), so.order_date,
			so.created_by, so.created_at
		FROM orders.sales_orders so
		JOIN orders.customers c ON so.customer_id = c.id
		WHERE so.id = $1`, id,
	).Scan(&o.ID, &o.OrderNumber, &o.CustomerID, &o.CustomerName, &o.Status, &o.Priority,
		&o.ShippingMethod, &o.SubtotalCents, &o.TaxAmountCents, &o.ShippingCostCents,
		&o.DiscountCents, &o.TotalAmountCents, &o.Currency, &o.PaymentTerms,
		&o.Notes, &o.WarehouseID, &o.OrderDate, &o.CreatedBy, &o.CreatedAt)
	if err != nil {
		return nil, err
	}

	// Fetch lines
	rows, err := r.pool.Query(ctx, `
		SELECT id, line_number, product_id, product_name, sku, quantity_ordered, quantity_shipped,
			unit_price_cents, discount_percent, line_total_cents, tax_amount_cents, tax_category
		FROM orders.order_lines WHERE order_id = $1 ORDER BY line_number`, id)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	for rows.Next() {
		l := &model.OrderLine{}
		rows.Scan(&l.ID, &l.LineNumber, &l.ProductID, &l.ProductName, &l.SKU,
			&l.QuantityOrdered, &l.QuantityShipped, &l.UnitPriceCents, &l.DiscountPercent,
			&l.LineTotalCents, &l.TaxAmountCents, &l.TaxCategory)
		o.Lines = append(o.Lines, l)
	}
	return o, nil
}

func (r *OrderRepository) UpdateOrderStatus(ctx context.Context, id, status, notes, updatedBy string) error {
	_, err := r.pool.Exec(ctx, `
		UPDATE orders.sales_orders SET status=$2, notes=COALESCE(notes||E'\n','')||$3,
			updated_by=$4, updated_at=NOW()
		WHERE id=$1`, id, status, notes, updatedBy)
	return err
}

func (r *OrderRepository) ListSalesOrders(ctx context.Context, customerID, status, priority string,
	startDate, endDate time.Time, offset, limit int) ([]*model.SalesOrder, int, error) {

	query := `SELECT so.id, so.order_number, so.customer_id, c.company_name, so.status,
		so.priority, so.total_amount_cents, so.currency, so.order_date, so.created_at
		FROM orders.sales_orders so
		JOIN orders.customers c ON so.customer_id = c.id WHERE 1=1`
	countQuery := `SELECT COUNT(*) FROM orders.sales_orders so WHERE 1=1`
	args := []interface{}{}
	idx := 1

	if customerID != "" {
		f := fmt.Sprintf(" AND so.customer_id = $%d", idx)
		query += f; countQuery += f
		args = append(args, customerID); idx++
	}
	if status != "" {
		f := fmt.Sprintf(" AND so.status = $%d", idx)
		query += f; countQuery += f
		args = append(args, status); idx++
	}
	if priority != "" {
		f := fmt.Sprintf(" AND so.priority = $%d", idx)
		query += f; countQuery += f
		args = append(args, priority); idx++
	}

	var total int
	r.pool.QueryRow(ctx, countQuery, args...).Scan(&total)

	query += fmt.Sprintf(" ORDER BY so.created_at DESC LIMIT $%d OFFSET $%d", idx, idx+1)
	args = append(args, limit, offset)

	rows, err := r.pool.Query(ctx, query, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var orders []*model.SalesOrder
	for rows.Next() {
		o := &model.SalesOrder{}
		rows.Scan(&o.ID, &o.OrderNumber, &o.CustomerID, &o.CustomerName, &o.Status,
			&o.Priority, &o.TotalAmountCents, &o.Currency, &o.OrderDate, &o.CreatedAt)
		orders = append(orders, o)
	}
	return orders, total, nil
}

// ============================================================
// Purchase Order Operations
// ============================================================

func (r *OrderRepository) CreatePurchaseOrder(ctx context.Context, po *model.PurchaseOrder) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	var seq int
	tx.QueryRow(ctx, `SELECT COALESCE(MAX(CAST(SUBSTRING(po_number FROM 4) AS INTEGER)), 0) + 1 FROM orders.purchase_orders`).Scan(&seq)
	po.PONumber = fmt.Sprintf("PO-%06d", seq)

	err = tx.QueryRow(ctx, `
		INSERT INTO orders.purchase_orders (po_number, supplier_id, status, subtotal_cents,
			tax_amount_cents, total_amount_cents, currency, payment_terms, delivery_terms,
			warehouse_id, expected_delivery, notes, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13)
		RETURNING id, order_date, created_at`,
		po.PONumber, po.SupplierID, po.Status, po.SubtotalCents,
		po.TaxAmountCents, po.TotalAmountCents, po.Currency, po.PaymentTerms,
		po.DeliveryTerms, nullStr(po.WarehouseID), nullTime(po.ExpectedDelivery), po.Notes, po.CreatedBy,
	).Scan(&po.ID, &po.OrderDate, &po.CreatedAt)
	if err != nil {
		return err
	}

	for i, line := range po.Lines {
		line.LineNumber = int32(i + 1)
		err = tx.QueryRow(ctx, `
			INSERT INTO orders.purchase_order_lines (po_id, line_number, product_id, product_name,
				quantity_ordered, unit_cost_cents, line_total_cents, currency)
			VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING id`,
			po.ID, line.LineNumber, line.ProductID, line.ProductName,
			line.QuantityOrdered, line.UnitCostCents, line.LineTotalCents, po.Currency,
		).Scan(&line.ID)
		if err != nil {
			return err
		}
	}

	return tx.Commit(ctx)
}

func (r *OrderRepository) GetOrderSummary(ctx context.Context, customerID string,
	startDate, endDate time.Time) (*model.OrderSummary, error) {

	s := &model.OrderSummary{}
	query := `SELECT
		COUNT(*),
		COUNT(*) FILTER (WHERE status IN ('DRAFT','PENDING_APPROVAL')),
		COUNT(*) FILTER (WHERE status IN ('APPROVED','PROCESSING','PICKING','PACKED')),
		COUNT(*) FILTER (WHERE status IN ('SHIPPED','DELIVERED')),
		COUNT(*) FILTER (WHERE status = 'CANCELLED'),
		COALESCE(SUM(total_amount_cents) FILTER (WHERE status NOT IN ('CANCELLED','RETURNED')), 0),
		COALESCE(AVG(total_amount_cents) FILTER (WHERE status NOT IN ('CANCELLED','RETURNED')), 0)
		FROM orders.sales_orders WHERE 1=1`

	args := []interface{}{}
	idx := 1

	if customerID != "" {
		query += fmt.Sprintf(" AND customer_id = $%d", idx)
		args = append(args, customerID); idx++
	}
	if !startDate.IsZero() {
		query += fmt.Sprintf(" AND order_date >= $%d", idx)
		args = append(args, startDate); idx++
	}
	if !endDate.IsZero() {
		query += fmt.Sprintf(" AND order_date <= $%d", idx)
		args = append(args, endDate); idx++
	}

	err := r.pool.QueryRow(ctx, query, args...).Scan(
		&s.TotalOrders, &s.PendingOrders, &s.ProcessingOrders,
		&s.ShippedOrders, &s.CancelledOrders, &s.TotalRevenueCents, &s.AvgOrderCents)

	if s.TotalOrders > 0 {
		s.FulfillmentRate = float64(s.ShippedOrders) / float64(s.TotalOrders) * 100
	}
	return s, err
}

// Supplier operations
func (r *OrderRepository) CreateSupplier(ctx context.Context, s *model.Supplier) error {
	return r.pool.QueryRow(ctx, `
		INSERT INTO orders.suppliers (code, company_name, contact_name, email, phone,
			tax_id, payment_terms, status, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9) RETURNING id, created_at`,
		s.Code, s.CompanyName, s.ContactName, s.Email, s.Phone,
		s.TaxID, s.PaymentTerms, s.Status, "system",
	).Scan(&s.ID, &s.CreatedAt)
}

func nullStr(s string) interface{} {
	if s == "" { return nil }
	return s
}

func nullTime(t time.Time) interface{} {
	if t.IsZero() { return nil }
	return t
}
