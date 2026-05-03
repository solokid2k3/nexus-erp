package handler

import (
	"context"
	"log"

	"erp-system/inventory-service/internal/model"
	"erp-system/inventory-service/internal/service"

	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// InventoryHandler implements the gRPC InventoryService.
// Note: In production, import generated proto stubs. Here we define the handler
// structure that wraps the service layer.
type InventoryHandler struct {
	svc *service.InventoryService
}

func NewInventoryHandler(svc *service.InventoryService) *InventoryHandler {
	return &InventoryHandler{svc: svc}
}

// Register registers the handler with a gRPC server.
// In production, use: pb.RegisterInventoryServiceServer(srv, h)
func (h *InventoryHandler) Register(srv *grpc.Server) {
	log.Println("Inventory gRPC handler registered")
	// pb.RegisterInventoryServiceServer(srv, h)
}

// Example gRPC method implementations showing the pattern:

func (h *InventoryHandler) CreateProduct(ctx context.Context, sku, name, description string,
	unitCostCents, sellingPriceCents int64, createdBy string) (*model.Product, error) {

	p := &model.Product{
		SKU:               sku,
		Name:              name,
		Description:       description,
		UnitCostCents:     unitCostCents,
		SellingPriceCents: sellingPriceCents,
		CreatedBy:         createdBy,
	}

	if err := h.svc.CreateProduct(ctx, p); err != nil {
		return nil, status.Errorf(codes.Internal, "failed to create product: %v", err)
	}
	return p, nil
}

func (h *InventoryHandler) GetProduct(ctx context.Context, id string) (*model.Product, error) {
	p, err := h.svc.GetProduct(ctx, id)
	if err != nil {
		return nil, status.Errorf(codes.NotFound, "product not found: %v", err)
	}
	return p, nil
}

func (h *InventoryHandler) AdjustStock(ctx context.Context, productID, warehouseID string,
	quantityChange int32, reason, referenceID, notes, adjustedBy string) error {

	return h.svc.AdjustStock(ctx, productID, warehouseID, quantityChange, reason, referenceID, notes, adjustedBy)
}

func (h *InventoryHandler) TransferStock(ctx context.Context, productID, fromWH, toWH string,
	qty int32, notes, by string) error {

	return h.svc.TransferStock(ctx, productID, fromWH, toWH, qty, notes, by)
}

func (h *InventoryHandler) ReserveStock(ctx context.Context, productID, warehouseID string,
	qty int32, orderID, by string) (string, error) {

	return h.svc.ReserveStock(ctx, productID, warehouseID, qty, orderID, by)
}
