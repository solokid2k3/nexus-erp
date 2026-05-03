package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"erp-system/inventory-service/internal/model"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type InventoryRepository struct {
	pool *pgxpool.Pool
}

func NewInventoryRepository(pool *pgxpool.Pool) *InventoryRepository {
	return &InventoryRepository{pool: pool}
}

// ============================================================
// Product Operations
// ============================================================

func (r *InventoryRepository) CreateProduct(ctx context.Context, p *model.Product) error {
	attrsJSON, _ := json.Marshal(p.Attributes)
	return r.pool.QueryRow(ctx, `
		INSERT INTO inventory.products (sku, name, description, category_id, brand, unit_of_measure,
			unit_cost_cents, selling_price_cents, currency, weight_kg, attributes, status,
			reorder_point, reorder_quantity, lead_time_days, supplier_id, tax_category, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18)
		RETURNING id, created_at, updated_at`,
		p.SKU, p.Name, p.Description, nullStr(p.CategoryID), p.Brand, p.UnitOfMeasure,
		p.UnitCostCents, p.SellingPriceCents, p.Currency, p.WeightKg, attrsJSON, p.Status,
		p.ReorderPoint, p.ReorderQuantity, p.LeadTimeDays, nullStr(p.SupplierID), p.TaxCategory, p.CreatedBy,
	).Scan(&p.ID, &p.CreatedAt, &p.UpdatedAt)
}

func (r *InventoryRepository) GetProduct(ctx context.Context, id string) (*model.Product, error) {
	p := &model.Product{}
	var attrsJSON []byte
	var catID, supplierID *string
	err := r.pool.QueryRow(ctx, `
		SELECT id, sku, name, description, COALESCE(category_id::text,''), brand, unit_of_measure,
			unit_cost_cents, selling_price_cents, currency, weight_kg, attributes, status,
			reorder_point, reorder_quantity, lead_time_days, COALESCE(supplier_id::text,''),
			tax_category, created_by, created_at, updated_by, updated_at
		FROM inventory.products WHERE id = $1`, id,
	).Scan(&p.ID, &p.SKU, &p.Name, &p.Description, &catID, &p.Brand, &p.UnitOfMeasure,
		&p.UnitCostCents, &p.SellingPriceCents, &p.Currency, &p.WeightKg, &attrsJSON, &p.Status,
		&p.ReorderPoint, &p.ReorderQuantity, &p.LeadTimeDays, &supplierID,
		&p.TaxCategory, &p.CreatedBy, &p.CreatedAt, &p.UpdatedBy, &p.UpdatedAt)
	if err != nil {
		return nil, err
	}
	if catID != nil {
		p.CategoryID = *catID
	}
	if supplierID != nil {
		p.SupplierID = *supplierID
	}
	json.Unmarshal(attrsJSON, &p.Attributes)
	return p, nil
}

func (r *InventoryRepository) ListProducts(ctx context.Context, categoryID, status, search string, offset, limit int) ([]*model.Product, int, error) {
	query := `SELECT id, sku, name, description, brand, unit_of_measure, unit_cost_cents,
		selling_price_cents, currency, status, reorder_point, created_at
		FROM inventory.products WHERE 1=1`
	countQuery := `SELECT COUNT(*) FROM inventory.products WHERE 1=1`
	args := []interface{}{}
	idx := 1

	if categoryID != "" {
		query += fmt.Sprintf(" AND category_id = $%d", idx)
		countQuery += fmt.Sprintf(" AND category_id = $%d", idx)
		args = append(args, categoryID)
		idx++
	}
	if status != "" {
		query += fmt.Sprintf(" AND status = $%d", idx)
		countQuery += fmt.Sprintf(" AND status = $%d", idx)
		args = append(args, status)
		idx++
	}
	if search != "" {
		query += fmt.Sprintf(" AND (name ILIKE $%d OR sku ILIKE $%d)", idx, idx)
		countQuery += fmt.Sprintf(" AND (name ILIKE $%d OR sku ILIKE $%d)", idx, idx)
		args = append(args, "%"+search+"%")
		idx++
	}

	var total int
	r.pool.QueryRow(ctx, countQuery, args...).Scan(&total)

	query += fmt.Sprintf(" ORDER BY created_at DESC LIMIT $%d OFFSET $%d", idx, idx+1)
	args = append(args, limit, offset)

	rows, err := r.pool.Query(ctx, query, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var products []*model.Product
	for rows.Next() {
		p := &model.Product{}
		rows.Scan(&p.ID, &p.SKU, &p.Name, &p.Description, &p.Brand, &p.UnitOfMeasure,
			&p.UnitCostCents, &p.SellingPriceCents, &p.Currency, &p.Status, &p.ReorderPoint, &p.CreatedAt)
		products = append(products, p)
	}
	return products, total, nil
}

func (r *InventoryRepository) UpdateProduct(ctx context.Context, p *model.Product) error {
	attrsJSON, _ := json.Marshal(p.Attributes)
	_, err := r.pool.Exec(ctx, `
		UPDATE inventory.products SET name=$2, description=$3, unit_cost_cents=$4,
			selling_price_cents=$5, reorder_point=$6, reorder_quantity=$7, lead_time_days=$8,
			status=$9, attributes=$10, updated_by=$11, updated_at=NOW()
		WHERE id=$1`,
		p.ID, p.Name, p.Description, p.UnitCostCents, p.SellingPriceCents,
		p.ReorderPoint, p.ReorderQuantity, p.LeadTimeDays, p.Status, attrsJSON, p.UpdatedBy)
	return err
}

// ============================================================
// Category Operations
// ============================================================

func (r *InventoryRepository) CreateCategory(ctx context.Context, c *model.Category) error {
	return r.pool.QueryRow(ctx, `
		INSERT INTO inventory.categories (name, description, parent_id)
		VALUES ($1, $2, $3) RETURNING id, created_at`,
		c.Name, c.Description, nullStr(c.ParentID),
	).Scan(&c.ID, &c.CreatedAt)
}

func (r *InventoryRepository) ListCategories(ctx context.Context, parentID string) ([]*model.Category, error) {
	query := `SELECT c.id, c.name, c.description, COALESCE(c.parent_id::text,''),
		(SELECT COUNT(*) FROM inventory.products p WHERE p.category_id = c.id) as product_count
		FROM inventory.categories c`
	args := []interface{}{}
	if parentID != "" {
		query += " WHERE c.parent_id = $1"
		args = append(args, parentID)
	}
	query += " ORDER BY c.name"

	rows, err := r.pool.Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var categories []*model.Category
	for rows.Next() {
		c := &model.Category{}
		rows.Scan(&c.ID, &c.Name, &c.Description, &c.ParentID, &c.ProductCount)
		categories = append(categories, c)
	}
	return categories, nil
}

// ============================================================
// Stock Operations
// ============================================================

func (r *InventoryRepository) GetStockLevel(ctx context.Context, productID, warehouseID string) (*model.StockLevel, error) {
	s := &model.StockLevel{}
	err := r.pool.QueryRow(ctx, `
		SELECT product_id, warehouse_id, quantity_on_hand, quantity_reserved,
			quantity_on_hand - quantity_reserved as quantity_available,
			quantity_incoming, bin_location, COALESCE(last_counted, NOW())
		FROM inventory.stock_levels
		WHERE product_id = $1 AND warehouse_id = $2`, productID, warehouseID,
	).Scan(&s.ProductID, &s.WarehouseID, &s.QuantityOnHand, &s.QuantityReserved,
		&s.QuantityAvailable, &s.QuantityIncoming, &s.BinLocation, &s.LastCounted)
	return s, err
}

func (r *InventoryRepository) GetStockLevelsForProduct(ctx context.Context, productID string) ([]*model.StockLevel, error) {
	rows, err := r.pool.Query(ctx, `
		SELECT product_id, warehouse_id, quantity_on_hand, quantity_reserved,
			quantity_on_hand - quantity_reserved, quantity_incoming, bin_location, COALESCE(last_counted, NOW())
		FROM inventory.stock_levels WHERE product_id = $1`, productID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var levels []*model.StockLevel
	for rows.Next() {
		s := &model.StockLevel{}
		rows.Scan(&s.ProductID, &s.WarehouseID, &s.QuantityOnHand, &s.QuantityReserved,
			&s.QuantityAvailable, &s.QuantityIncoming, &s.BinLocation, &s.LastCounted)
		levels = append(levels, s)
	}
	return levels, nil
}

// AdjustStock atomically adjusts stock and records movement in a transaction
func (r *InventoryRepository) AdjustStock(ctx context.Context, productID, warehouseID string,
	quantityChange int32, reason, referenceID, notes, adjustedBy string) error {

	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	// Get current quantity with row lock
	var currentQty int32
	err = tx.QueryRow(ctx, `
		SELECT quantity_on_hand FROM inventory.stock_levels
		WHERE product_id = $1 AND warehouse_id = $2
		FOR UPDATE`, productID, warehouseID).Scan(&currentQty)

	if err == pgx.ErrNoRows {
		// Create stock level if not exists
		_, err = tx.Exec(ctx, `
			INSERT INTO inventory.stock_levels (product_id, warehouse_id, quantity_on_hand)
			VALUES ($1, $2, 0)`, productID, warehouseID)
		if err != nil {
			return err
		}
		currentQty = 0
	} else if err != nil {
		return err
	}

	newQty := currentQty + quantityChange
	if newQty < 0 {
		return fmt.Errorf("insufficient stock: current=%d, change=%d", currentQty, quantityChange)
	}

	// Update stock
	_, err = tx.Exec(ctx, `
		UPDATE inventory.stock_levels SET quantity_on_hand = $3
		WHERE product_id = $1 AND warehouse_id = $2`, productID, warehouseID, newQty)
	if err != nil {
		return err
	}

	// Record movement
	_, err = tx.Exec(ctx, `
		INSERT INTO inventory.stock_movements (product_id, warehouse_id, reason, quantity_change,
			quantity_before, quantity_after, reference_id, notes, created_by)
		VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)`,
		productID, warehouseID, reason, quantityChange, currentQty, newQty, referenceID, notes, adjustedBy)
	if err != nil {
		return err
	}

	return tx.Commit(ctx)
}

// TransferStock moves stock between warehouses atomically
func (r *InventoryRepository) TransferStock(ctx context.Context, productID, fromWarehouse, toWarehouse string,
	quantity int32, notes, transferredBy string) error {

	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	// Lock source stock
	var srcQty int32
	err = tx.QueryRow(ctx, `
		SELECT quantity_on_hand FROM inventory.stock_levels
		WHERE product_id=$1 AND warehouse_id=$2 FOR UPDATE`, productID, fromWarehouse).Scan(&srcQty)
	if err != nil {
		return fmt.Errorf("source warehouse stock not found: %w", err)
	}

	if srcQty < quantity {
		return fmt.Errorf("insufficient stock for transfer: available=%d, requested=%d", srcQty, quantity)
	}

	// Deduct from source
	_, err = tx.Exec(ctx, `
		UPDATE inventory.stock_levels SET quantity_on_hand = quantity_on_hand - $3
		WHERE product_id=$1 AND warehouse_id=$2`, productID, fromWarehouse, quantity)
	if err != nil {
		return err
	}

	// Add to destination (upsert)
	_, err = tx.Exec(ctx, `
		INSERT INTO inventory.stock_levels (product_id, warehouse_id, quantity_on_hand)
		VALUES ($1, $2, $3)
		ON CONFLICT (product_id, warehouse_id)
		DO UPDATE SET quantity_on_hand = inventory.stock_levels.quantity_on_hand + $3`,
		productID, toWarehouse, quantity)
	if err != nil {
		return err
	}

	// Record movements for both warehouses
	refID := fmt.Sprintf("TRANSFER-%s-%s", fromWarehouse[:8], toWarehouse[:8])
	for _, m := range []struct {
		whID   string
		change int32
		before int32
	}{
		{fromWarehouse, -quantity, srcQty},
		{toWarehouse, quantity, 0}, // approximate
	} {
		_, err = tx.Exec(ctx, `
			INSERT INTO inventory.stock_movements (product_id, warehouse_id, reason, quantity_change,
				quantity_before, quantity_after, reference_id, notes, created_by)
			VALUES ($1,$2,'TRANSFER',$3,$4,$5,$6,$7,$8)`,
			productID, m.whID, m.change, m.before, m.before+m.change, refID, notes, transferredBy)
		if err != nil {
			return err
		}
	}

	return tx.Commit(ctx)
}

// ReserveStock reserves stock for an order
func (r *InventoryRepository) ReserveStock(ctx context.Context, productID, warehouseID string,
	quantity int32, orderID, reservedBy string) (string, error) {

	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return "", err
	}
	defer tx.Rollback(ctx)

	// Check available stock
	var available int32
	err = tx.QueryRow(ctx, `
		SELECT quantity_on_hand - quantity_reserved
		FROM inventory.stock_levels
		WHERE product_id=$1 AND warehouse_id=$2 FOR UPDATE`, productID, warehouseID).Scan(&available)
	if err != nil {
		return "", err
	}
	if available < quantity {
		return "", fmt.Errorf("insufficient available stock: available=%d, requested=%d", available, quantity)
	}

	// Update reserved quantity
	_, err = tx.Exec(ctx, `
		UPDATE inventory.stock_levels SET quantity_reserved = quantity_reserved + $3
		WHERE product_id=$1 AND warehouse_id=$2`, productID, warehouseID, quantity)
	if err != nil {
		return "", err
	}

	// Create reservation record
	var reservationID string
	err = tx.QueryRow(ctx, `
		INSERT INTO inventory.stock_reservations (product_id, warehouse_id, order_id, quantity, reserved_by)
		VALUES ($1,$2,$3,$4,$5) RETURNING id`,
		productID, warehouseID, orderID, quantity, reservedBy).Scan(&reservationID)
	if err != nil {
		return "", err
	}

	return reservationID, tx.Commit(ctx)
}

// ReleaseReservation releases a stock reservation
func (r *InventoryRepository) ReleaseReservation(ctx context.Context, reservationID string) error {
	tx, err := r.pool.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	var productID, warehouseID string
	var qty int32
	err = tx.QueryRow(ctx, `
		SELECT product_id, warehouse_id, quantity FROM inventory.stock_reservations
		WHERE id=$1 AND status='ACTIVE' FOR UPDATE`, reservationID,
	).Scan(&productID, &warehouseID, &qty)
	if err != nil {
		return fmt.Errorf("reservation not found or already released: %w", err)
	}

	_, err = tx.Exec(ctx, `
		UPDATE inventory.stock_levels SET quantity_reserved = quantity_reserved - $3
		WHERE product_id=$1 AND warehouse_id=$2`, productID, warehouseID, qty)
	if err != nil {
		return err
	}

	_, err = tx.Exec(ctx, `
		UPDATE inventory.stock_reservations SET status='RELEASED', released_at=NOW()
		WHERE id=$1`, reservationID)
	if err != nil {
		return err
	}

	return tx.Commit(ctx)
}

// ListStockMovements returns stock movements with filtering
func (r *InventoryRepository) ListStockMovements(ctx context.Context, productID, warehouseID, reason string,
	startDate, endDate time.Time, offset, limit int) ([]*model.StockMovement, int, error) {

	query := `SELECT id, product_id, warehouse_id, reason, quantity_change, quantity_before,
		quantity_after, reference_id, notes, created_by, created_at
		FROM inventory.stock_movements WHERE 1=1`
	countQuery := `SELECT COUNT(*) FROM inventory.stock_movements WHERE 1=1`
	args := []interface{}{}
	idx := 1

	if productID != "" {
		f := fmt.Sprintf(" AND product_id = $%d", idx)
		query += f
		countQuery += f
		args = append(args, productID)
		idx++
	}
	if warehouseID != "" {
		f := fmt.Sprintf(" AND warehouse_id = $%d", idx)
		query += f
		countQuery += f
		args = append(args, warehouseID)
		idx++
	}
	if reason != "" {
		f := fmt.Sprintf(" AND reason = $%d", idx)
		query += f
		countQuery += f
		args = append(args, reason)
		idx++
	}
	if !startDate.IsZero() {
		f := fmt.Sprintf(" AND created_at >= $%d", idx)
		query += f
		countQuery += f
		args = append(args, startDate)
		idx++
	}
	if !endDate.IsZero() {
		f := fmt.Sprintf(" AND created_at <= $%d", idx)
		query += f
		countQuery += f
		args = append(args, endDate)
		idx++
	}

	var total int
	r.pool.QueryRow(ctx, countQuery, args...).Scan(&total)

	query += fmt.Sprintf(" ORDER BY created_at DESC LIMIT $%d OFFSET $%d", idx, idx+1)
	args = append(args, limit, offset)

	rows, err := r.pool.Query(ctx, query, args...)
	if err != nil {
		return nil, 0, err
	}
	defer rows.Close()

	var movements []*model.StockMovement
	for rows.Next() {
		m := &model.StockMovement{}
		rows.Scan(&m.ID, &m.ProductID, &m.WarehouseID, &m.Reason, &m.QuantityChange,
			&m.QuantityBefore, &m.QuantityAfter, &m.ReferenceID, &m.Notes, &m.CreatedBy, &m.CreatedAt)
		movements = append(movements, m)
	}
	return movements, total, nil
}

// GetLowStockAlerts finds products below reorder point
func (r *InventoryRepository) GetLowStockAlerts(ctx context.Context, warehouseID string) ([]*model.LowStockAlert, error) {
	query := `
		SELECT p.id, p.name, p.sku, w.id, w.name,
			sl.quantity_on_hand, p.reorder_point, p.reorder_quantity,
			CASE WHEN sl.quantity_on_hand <= p.reorder_point / 2 THEN 'CRITICAL' ELSE 'WARNING' END
		FROM inventory.products p
		JOIN inventory.stock_levels sl ON p.id = sl.product_id
		JOIN inventory.warehouses w ON sl.warehouse_id = w.id
		WHERE p.status = 'ACTIVE'
			AND p.reorder_point > 0
			AND sl.quantity_on_hand <= p.reorder_point`

	args := []interface{}{}
	if warehouseID != "" {
		query += " AND sl.warehouse_id = $1"
		args = append(args, warehouseID)
	}
	query += " ORDER BY sl.quantity_on_hand ASC"

	rows, err := r.pool.Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var alerts []*model.LowStockAlert
	for rows.Next() {
		a := &model.LowStockAlert{}
		rows.Scan(&a.ProductID, &a.ProductName, &a.SKU, &a.WarehouseID, &a.WarehouseName,
			&a.CurrentQty, &a.ReorderPoint, &a.ReorderQty, &a.Severity)
		alerts = append(alerts, a)
	}
	return alerts, nil
}

// Warehouse operations
func (r *InventoryRepository) CreateWarehouse(ctx context.Context, w *model.Warehouse) error {
	return r.pool.QueryRow(ctx, `
		INSERT INTO inventory.warehouses (name, code, city, country, capacity_units)
		VALUES ($1,$2,$3,$4,$5) RETURNING id, created_at`,
		w.Name, w.Code, w.City, w.Country, w.CapacityUnits,
	).Scan(&w.ID, &w.CreatedAt)
}

func nullStr(s string) interface{} {
	if s == "" {
		return nil
	}
	return s
}
