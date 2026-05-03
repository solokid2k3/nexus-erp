package service

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	"erp-system/inventory-service/internal/model"
	"erp-system/inventory-service/internal/repository"

	"github.com/redis/go-redis/v9"
)

const (
	productCacheTTL = 15 * time.Minute
	stockCacheTTL   = 5 * time.Minute
	cachePrefix     = "inv:"
)

type InventoryService struct {
	repo *repository.InventoryRepository
	rdb  *redis.Client
}

func NewInventoryService(repo *repository.InventoryRepository, rdb *redis.Client) *InventoryService {
	return &InventoryService{repo: repo, rdb: rdb}
}

// ============================================================
// Product Operations with Redis Caching
// ============================================================

func (s *InventoryService) CreateProduct(ctx context.Context, p *model.Product) error {
	if p.SKU == "" || p.Name == "" {
		return fmt.Errorf("SKU and Name are required")
	}
	if p.Status == "" {
		p.Status = "ACTIVE"
	}
	if p.Currency == "" {
		p.Currency = "USD"
	}
	if p.UnitOfMeasure == "" {
		p.UnitOfMeasure = "PCS"
	}
	if p.SellingPriceCents <= p.UnitCostCents {
		log.Printf("WARN: Product %s selling price <= cost", p.SKU)
	}
	return s.repo.CreateProduct(ctx, p)
}

func (s *InventoryService) GetProduct(ctx context.Context, id string) (*model.Product, error) {
	// Try cache first
	cacheKey := cachePrefix + "product:" + id
	cached, err := s.rdb.Get(ctx, cacheKey).Bytes()
	if err == nil {
		p := &model.Product{}
		if json.Unmarshal(cached, p) == nil {
			return p, nil
		}
	}

	// Cache miss - fetch from DB
	p, err := s.repo.GetProduct(ctx, id)
	if err != nil {
		return nil, err
	}

	// Cache the result
	if data, err := json.Marshal(p); err == nil {
		s.rdb.Set(ctx, cacheKey, data, productCacheTTL)
	}

	return p, nil
}

func (s *InventoryService) UpdateProduct(ctx context.Context, p *model.Product) error {
	err := s.repo.UpdateProduct(ctx, p)
	if err != nil {
		return err
	}
	// Invalidate cache
	s.rdb.Del(ctx, cachePrefix+"product:"+p.ID)
	return nil
}

func (s *InventoryService) ListProducts(ctx context.Context, categoryID, status, search string, page, pageSize int) ([]*model.Product, int, error) {
	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}
	offset := (page - 1) * pageSize
	return s.repo.ListProducts(ctx, categoryID, status, search, offset, pageSize)
}

// ============================================================
// Category Operations
// ============================================================

func (s *InventoryService) CreateCategory(ctx context.Context, c *model.Category) error {
	if c.Name == "" {
		return fmt.Errorf("category name is required")
	}
	return s.repo.CreateCategory(ctx, c)
}

func (s *InventoryService) ListCategories(ctx context.Context, parentID string) ([]*model.Category, error) {
	return s.repo.ListCategories(ctx, parentID)
}

// ============================================================
// Warehouse Operations
// ============================================================

func (s *InventoryService) CreateWarehouse(ctx context.Context, w *model.Warehouse) error {
	if w.Name == "" || w.Code == "" {
		return fmt.Errorf("warehouse name and code are required")
	}
	if w.Status == "" {
		w.Status = "ACTIVE"
	}
	return s.repo.CreateWarehouse(ctx, w)
}

// ============================================================
// Stock Operations with Cache Invalidation
// ============================================================

func (s *InventoryService) GetStockLevels(ctx context.Context, productID, warehouseID string) ([]*model.StockLevel, error) {
	if warehouseID != "" {
		level, err := s.repo.GetStockLevel(ctx, productID, warehouseID)
		if err != nil {
			return nil, err
		}
		return []*model.StockLevel{level}, nil
	}
	return s.repo.GetStockLevelsForProduct(ctx, productID)
}

func (s *InventoryService) AdjustStock(ctx context.Context, productID, warehouseID string,
	quantityChange int32, reason, referenceID, notes, adjustedBy string) error {

	if productID == "" || warehouseID == "" {
		return fmt.Errorf("product_id and warehouse_id are required")
	}
	if quantityChange == 0 {
		return fmt.Errorf("quantity_change cannot be zero")
	}
	if reason == "" {
		return fmt.Errorf("adjustment reason is required")
	}

	err := s.repo.AdjustStock(ctx, productID, warehouseID, quantityChange, reason, referenceID, notes, adjustedBy)
	if err != nil {
		return err
	}

	// Invalidate stock cache
	s.rdb.Del(ctx, cachePrefix+"stock:"+productID+":"+warehouseID)
	return nil
}

func (s *InventoryService) TransferStock(ctx context.Context, productID, fromWarehouse, toWarehouse string,
	quantity int32, notes, transferredBy string) error {

	if productID == "" || fromWarehouse == "" || toWarehouse == "" {
		return fmt.Errorf("product_id, from_warehouse, and to_warehouse are required")
	}
	if fromWarehouse == toWarehouse {
		return fmt.Errorf("source and destination warehouses must be different")
	}
	if quantity <= 0 {
		return fmt.Errorf("transfer quantity must be positive")
	}

	err := s.repo.TransferStock(ctx, productID, fromWarehouse, toWarehouse, quantity, notes, transferredBy)
	if err != nil {
		return err
	}

	// Invalidate caches for both warehouses
	s.rdb.Del(ctx, cachePrefix+"stock:"+productID+":"+fromWarehouse)
	s.rdb.Del(ctx, cachePrefix+"stock:"+productID+":"+toWarehouse)
	return nil
}

func (s *InventoryService) ReserveStock(ctx context.Context, productID, warehouseID string,
	quantity int32, orderID, reservedBy string) (string, error) {

	if quantity <= 0 {
		return "", fmt.Errorf("reserve quantity must be positive")
	}
	if orderID == "" {
		return "", fmt.Errorf("order_id is required for reservation")
	}

	reservationID, err := s.repo.ReserveStock(ctx, productID, warehouseID, quantity, orderID, reservedBy)
	if err != nil {
		return "", err
	}

	s.rdb.Del(ctx, cachePrefix+"stock:"+productID+":"+warehouseID)
	return reservationID, nil
}

func (s *InventoryService) ReleaseReservation(ctx context.Context, reservationID string) error {
	return s.repo.ReleaseReservation(ctx, reservationID)
}

func (s *InventoryService) ListStockMovements(ctx context.Context, productID, warehouseID, reason string,
	startDate, endDate time.Time, page, pageSize int) ([]*model.StockMovement, int, error) {

	if page < 1 {
		page = 1
	}
	if pageSize < 1 || pageSize > 100 {
		pageSize = 20
	}
	offset := (page - 1) * pageSize
	return s.repo.ListStockMovements(ctx, productID, warehouseID, reason, startDate, endDate, offset, pageSize)
}

func (s *InventoryService) GetLowStockAlerts(ctx context.Context, warehouseID string) ([]*model.LowStockAlert, error) {
	return s.repo.GetLowStockAlerts(ctx, warehouseID)
}
