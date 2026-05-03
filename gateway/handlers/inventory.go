package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"google.golang.org/grpc"
)

// InventoryHandler proxies REST calls to the Inventory gRPC service
type InventoryHandler struct {
	conn *grpc.ClientConn
}

func NewInventoryHandler(conn *grpc.ClientConn) *InventoryHandler {
	return &InventoryHandler{conn: conn}
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
	c.JSON(http.StatusOK, gin.H{"message": "Get product", "id": id})
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
	page := c.DefaultQuery("page", "1")
	pageSize := c.DefaultQuery("page_size", "20")
	category := c.Query("category_id")
	status := c.DefaultQuery("status", "ACTIVE")
	search := c.Query("q")

	c.JSON(http.StatusOK, gin.H{
		"message": "List products",
		"filters": gin.H{
			"page": page, "page_size": pageSize,
			"category": category, "status": status, "search": search,
		},
	})
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
	c.JSON(http.StatusOK, gin.H{"message": "List categories"})
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
	c.JSON(http.StatusOK, gin.H{"message": "Stock levels", "product_id": productID, "warehouse_id": warehouseID})
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
	c.JSON(http.StatusOK, gin.H{"message": "Stock movements"})
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
