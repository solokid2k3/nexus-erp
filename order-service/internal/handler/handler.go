package handler

import (
	"log"

	"erp-system/order-service/internal/service"

	"google.golang.org/grpc"
)

type OrderHandler struct {
	svc *service.OrderService
}

func NewOrderHandler(svc *service.OrderService) *OrderHandler {
	return &OrderHandler{svc: svc}
}

func (h *OrderHandler) Register(srv *grpc.Server) {
	log.Println("Order gRPC handler registered")
	// pb.RegisterOrderServiceServer(srv, h)
}
