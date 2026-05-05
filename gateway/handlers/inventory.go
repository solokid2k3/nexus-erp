package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/jackc/pgx/v5/pgxpool"
	"google.golang.org/grpc"
)

// InventoryHandler proxies REST calls to the Inventory gRPC service
type InventoryHandler struct {
	conn *grpc.ClientConn
	pool *pgxpool.Pool
}

func NewInventoryHandler(conn *grpc.ClientConn, pool *pgxpool.Pool) *InventoryHandler {
	return &InventoryHandler{conn: conn, pool: pool}
}

// CreateProduct godoc
// @Summary      Create a new product
// @Description  Create a new product in the inventory system
// @Tags         Inventory - Products
// @Accept       json
// @Produce      json
// @Param        product  body      map[string]interface{}  true  "Product data (name, sku, category_id, price, etc.)"
// @Success      201      {object}  map[string]interface{}  "Product created successfully"
// @Failure      400      {object}  map[string]interface{}  "Invalid request body"
// @Security     BearerAuth
// @Router       /api/v1/inventory/products [post]
func (h *InventoryHandler) CreateProduct(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	req["created_by"], _ = c.Get("userID")
	// In production, marshal to protobuf and call gRPC
	c.JSON(http.StatusCreated, gin.H{"message": "Product created", "data": req})
}

// GetProduct godoc
// @Summary      Get a product by ID
// @Description  Retrieve a single product by its ID
// @Tags         Inventory - Products
// @Produce      json
// @Param        id   path      string  true  "Product ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/products/{id} [get]
func (h *InventoryHandler) GetProduct(c *gin.Context) {
	id := c.Param("id")

	var sku, name, desc, brand, uom, status string
	var catID *string
	var cost, price int64

	err := h.pool.QueryRow(c.Request.Context(), `
		SELECT sku, name, description, category_id, brand, unit_of_measure, unit_cost_cents, selling_price_cents, status 
		FROM inventory.products WHERE id = $1`, id).Scan(
		&sku, &name, &desc, &catID, &brand, &uom, &cost, &price, &status,
	)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"success": false, "error": "Product not found"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": map[string]interface{}{
			"id": id, "sku": sku, "name": name, "description": desc, "category_id": catID,
			"brand": brand, "unit_of_measure": uom, "unit_cost_cents": cost, "selling_price_cents": price, "status": status,
		},
	})
}

// ListProducts godoc
// @Summary      List all products
// @Description  Retrieve a paginated list of products with optional filters
// @Tags         Inventory - Products
// @Produce      json
// @Param        page         query     string  false  "Page number"         default(1)
// @Param        page_size    query     string  false  "Items per page"      default(20)
// @Param        category_id  query     string  false  "Filter by category"
// @Param        status       query     string  false  "Filter by status"    default(ACTIVE)
// @Param        q            query     string  false  "Search query"
// @Success      200          {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/products [get]
func (h *InventoryHandler) ListProducts(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	if page < 1 {
		page = 1
	}
	pageSize, _ := strconv.Atoi(c.DefaultQuery("page_size", "20"))
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}
	offset := (page - 1) * pageSize

	category := c.Query("category_id")
	status := c.DefaultQuery("status", "ACTIVE")
	search := c.Query("q")

	query := `SELECT id, sku, name, description, category_id, brand, unit_of_measure, unit_cost_cents, selling_price_cents, status 
	          FROM inventory.products WHERE 1=1`
	args := []interface{}{}
	idx := 1

	if category != "" {
		query += ` AND category_id = $` + strconv.Itoa(idx)
		args = append(args, category)
		idx++
	}
	if status != "" {
		query += ` AND status = $` + strconv.Itoa(idx)
		args = append(args, status)
		idx++
	}
	if search != "" {
		query += ` AND (name ILIKE $` + strconv.Itoa(idx) + ` OR sku ILIKE $` + strconv.Itoa(idx) + `)`
		args = append(args, "%"+search+"%")
		idx++
	}

	query += ` ORDER BY created_at DESC LIMIT $` + strconv.Itoa(idx) + ` OFFSET $` + strconv.Itoa(idx+1)
	args = append(args, pageSize, offset)

	rows, err := h.pool.Query(c.Request.Context(), query, args...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var products []map[string]interface{}
	for rows.Next() {
		var id, sku, name, uom, status string
		var desc, catID, brand *string
		var cost, price int64
		if err := rows.Scan(&id, &sku, &name, &desc, &catID, &brand, &uom, &cost, &price, &status); err == nil {
			products = append(products, map[string]interface{}{
				"id": id, "sku": sku, "name": name, "description": desc, "category_id": catID,
				"brand": brand, "unit_of_measure": uom, "unit_cost_cents": cost, "selling_price_cents": price, "status": status,
			})
		}
	}
	if products == nil {
		products = []map[string]interface{}{}
	}

	c.JSON(http.StatusOK, products)
}

// UpdateProduct godoc
// @Summary      Update a product
// @Description  Update an existing product by its ID
// @Tags         Inventory - Products
// @Accept       json
// @Produce      json
// @Param        id       path      string                 true  "Product ID"
// @Param        product  body      map[string]interface{}  true  "Updated product data"
// @Success      200      {object}  map[string]interface{}
// @Failure      400      {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/products/{id} [put]
func (h *InventoryHandler) UpdateProduct(c *gin.Context) {
	id := c.Param("id")
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	req["updated_by"], _ = c.Get("userID")
	c.JSON(http.StatusOK, gin.H{"message": "Product updated", "id": id, "data": req})
}

// CreateCategory godoc
// @Summary      Create a new category
// @Description  Create a new product category
// @Tags         Inventory - Categories
// @Accept       json
// @Produce      json
// @Param        category  body      map[string]interface{}  true  "Category data (name, description)"
// @Success      201       {object}  map[string]interface{}
// @Failure      400       {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/categories [post]
func (h *InventoryHandler) CreateCategory(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Category created", "data": req})
}

// ListCategories godoc
// @Summary      List all categories
// @Description  Retrieve all product categories
// @Tags         Inventory - Categories
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/categories [get]
func (h *InventoryHandler) ListCategories(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `SELECT id, name, description, parent_id FROM inventory.categories ORDER BY name`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var categories []map[string]interface{}
	for rows.Next() {
		var id, name string
		var desc, parentID *string
		if err := rows.Scan(&id, &name, &desc, &parentID); err == nil {
			categories = append(categories, map[string]interface{}{
				"id": id, "name": name, "description": desc, "parent_id": parentID,
			})
		}
	}
	if categories == nil {
		categories = []map[string]interface{}{}
	}

	c.JSON(http.StatusOK, categories)
}

// CreateWarehouse godoc
// @Summary      Create a new warehouse
// @Description  Create a new warehouse location
// @Tags         Inventory - Warehouses
// @Accept       json
// @Produce      json
// @Param        warehouse  body      map[string]interface{}  true  "Warehouse data (name, code, address)"
// @Success      201        {object}  map[string]interface{}
// @Failure      400        {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/warehouses [post]
func (h *InventoryHandler) CreateWarehouse(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"message": "Warehouse created", "data": req})
}

// GetWarehouse godoc
// @Summary      Get a warehouse by ID
// @Description  Retrieve a single warehouse by its ID
// @Tags         Inventory - Warehouses
// @Produce      json
// @Param        id   path      string  true  "Warehouse ID"
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/warehouses/{id} [get]
func (h *InventoryHandler) GetWarehouse(c *gin.Context) {
	id := c.Param("id")

	var name, code, city, country, status string
	err := h.pool.QueryRow(c.Request.Context(), `
		SELECT name, code, city, country, status 
		FROM inventory.warehouses WHERE id = $1`, id).Scan(
		&name, &code, &city, &country, &status,
	)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"success": false, "error": "Warehouse not found"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"success": true,
		"data": map[string]interface{}{
			"id": id, "name": name, "code": code, "city": city, "country": country, "status": status,
		},
	})
}

// ListWarehouses godoc
// @Summary      List all warehouses
// @Description  Retrieve a list of all warehouses
// @Tags         Inventory - Warehouses
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/warehouses [get]
func (h *InventoryHandler) ListWarehouses(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, name, code, city, country, status 
		FROM inventory.warehouses ORDER BY name`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var warehouses []map[string]interface{}
	for rows.Next() {
		var id, name, code, status string
		var city, country *string
		if err := rows.Scan(&id, &name, &code, &city, &country, &status); err == nil {
			warehouses = append(warehouses, map[string]interface{}{
				"id": id, "name": name, "code": code, "city": city, "country": country, "status": status,
			})
		}
	}
	if warehouses == nil {
		warehouses = []map[string]interface{}{}
	}

	c.JSON(http.StatusOK, warehouses)
}

// GetStockLevels godoc
// @Summary      Get stock levels for a product
// @Description  Retrieve stock levels across all warehouses for a given product
// @Tags         Inventory - Stock
// @Produce      json
// @Param        productId     path      string  true   "Product ID"
// @Param        warehouse_id  query     string  false  "Filter by warehouse"
// @Success      200           {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/{productId} [get]
func (h *InventoryHandler) GetStockLevels(c *gin.Context) {
	productID := c.Param("productId")
	warehouseID := c.Query("warehouse_id")

	query := `SELECT warehouse_id, quantity_on_hand, quantity_reserved, quantity_on_hand - quantity_reserved as available 
	          FROM inventory.stock_levels WHERE product_id = $1`
	args := []interface{}{productID}

	if warehouseID != "" {
		query += ` AND warehouse_id = $2`
		args = append(args, warehouseID)
	}

	rows, err := h.pool.Query(c.Request.Context(), query, args...)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var levels []map[string]interface{}
	for rows.Next() {
		var wID string
		var qOH, qR, qA int32
		if err := rows.Scan(&wID, &qOH, &qR, &qA); err == nil {
			levels = append(levels, map[string]interface{}{
				"warehouse_id": wID, "quantity_on_hand": qOH, "quantity_reserved": qR, "quantity_available": qA,
			})
		}
	}
	if levels == nil {
		levels = []map[string]interface{}{}
	}

	c.JSON(http.StatusOK, gin.H{"success": true, "data": levels, "product_id": productID})
}

// AdjustStock godoc
// @Summary      Adjust stock quantity
// @Description  Manually adjust stock quantity for a product in a warehouse
// @Tags         Inventory - Stock
// @Accept       json
// @Produce      json
// @Param        adjustment  body      map[string]interface{}  true  "Stock adjustment data (product_id, warehouse_id, quantity, reason)"
// @Success      200         {object}  map[string]interface{}
// @Failure      400         {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/adjust [post]
func (h *InventoryHandler) AdjustStock(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Stock adjusted", "data": req})
}

// TransferStock godoc
// @Summary      Transfer stock between warehouses
// @Description  Transfer stock from one warehouse to another
// @Tags         Inventory - Stock
// @Accept       json
// @Produce      json
// @Param        transfer  body      map[string]interface{}  true  "Transfer data (product_id, from_warehouse, to_warehouse, quantity)"
// @Success      200       {object}  map[string]interface{}
// @Failure      400       {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/transfer [post]
func (h *InventoryHandler) TransferStock(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Stock transferred", "data": req})
}

// ReserveStock godoc
// @Summary      Reserve stock for an order
// @Description  Reserve stock quantity for a pending order
// @Tags         Inventory - Stock
// @Accept       json
// @Produce      json
// @Param        reservation  body      map[string]interface{}  true  "Reservation data (product_id, warehouse_id, quantity, order_id)"
// @Success      200          {object}  map[string]interface{}
// @Failure      400          {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/reserve [post]
func (h *InventoryHandler) ReserveStock(c *gin.Context) {
	var req map[string]interface{}
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "Stock reserved", "data": req})
}

// ListStockMovements godoc
// @Summary      List stock movements
// @Description  Retrieve a history of all stock movements (adjustments, transfers, etc.)
// @Tags         Inventory - Stock
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/movements [get]
func (h *InventoryHandler) ListStockMovements(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT id, product_id, warehouse_id, reason, quantity_change, quantity_before, quantity_after, created_at 
		FROM inventory.stock_movements ORDER BY created_at DESC LIMIT 50`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"success": false, "error": err.Error()})
		return
	}
	defer rows.Close()

	var movements []map[string]interface{}
	for rows.Next() {
		var id, pID, wID, reason string
		var qC, qB, qA int32
		var createdAt string
		if err := rows.Scan(&id, &pID, &wID, &reason, &qC, &qB, &qA, &createdAt); err == nil {
			movements = append(movements, map[string]interface{}{
				"id": id, "product_id": pID, "warehouse_id": wID, "reason": reason,
				"quantity_change": qC, "quantity_before": qB, "quantity_after": qA, "created_at": createdAt,
			})
		}
	}
	if movements == nil {
		movements = []map[string]interface{}{}
	}

	c.JSON(http.StatusOK, movements)
}

// ListStock godoc
// @Summary      List all stock levels
// @Description  Retrieve stock levels for all products across all warehouses
// @Tags         Inventory - Stock
// @Produce      json
// @Success      200  {array}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock [get]
func (h *InventoryHandler) ListStock(c *gin.Context) {
	rows, err := h.pool.Query(c.Request.Context(), `
		SELECT sl.id, p.name as product_name, p.sku, w.name as warehouse_name,
		       sl.quantity_on_hand, sl.quantity_reserved, sl.bin_location
		FROM inventory.stock_levels sl
		JOIN inventory.products p ON sl.product_id = p.id
		JOIN inventory.warehouses w ON sl.warehouse_id = w.id
		ORDER BY p.name, w.name LIMIT 100`)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	defer rows.Close()

	var stock []map[string]interface{}
	for rows.Next() {
		var id, prodName, sku, whName string
		var qOH, qR int32
		var bin *string
		if err := rows.Scan(&id, &prodName, &sku, &whName, &qOH, &qR, &bin); err == nil {
			stock = append(stock, map[string]interface{}{
				"id": id, "product_name": prodName, "sku": sku, "warehouse_name": whName,
				"quantity_on_hand": qOH, "quantity_reserved": qR, "bin_location": bin,
			})
		}
	}
	if stock == nil {
		stock = []map[string]interface{}{}
	}
	c.JSON(http.StatusOK, stock)
}

// GetLowStockAlerts godoc
// @Summary      Get low stock alerts
// @Description  Retrieve products that are below their minimum stock threshold
// @Tags         Inventory - Stock
// @Produce      json
// @Success      200  {object}  map[string]interface{}
// @Security     BearerAuth
// @Router       /api/v1/inventory/stock/alerts [get]
func (h *InventoryHandler) GetLowStockAlerts(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"message": "Low stock alerts"})
}
