package main

import (
	"context"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"erp-system/order-service/internal/handler"
	"erp-system/order-service/internal/repository"
	"erp-system/order-service/internal/service"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/reflection"
)

func main() {
	port := getEnv("GRPC_PORT", "50052")
	dbURL := getEnv("DATABASE_URL", "postgres://erp_user:erp_secret_2025@localhost:5432/erp_db?sslmode=disable")
	redisAddr := getEnv("REDIS_ADDR", "localhost:6379")
	inventoryAddr := getEnv("INVENTORY_SERVICE_ADDR", "localhost:50051")
	financeAddr := getEnv("FINANCE_SERVICE_ADDR", "localhost:50053")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// Database
	pool, err := pgxpool.New(ctx, dbURL)
	if err != nil {
		log.Fatalf("DB connection failed: %v", err)
	}
	defer pool.Close()
	log.Println("Connected to PostgreSQL")

	// Redis
	rdb := redis.NewClient(&redis.Options{Addr: redisAddr})
	defer rdb.Close()
	log.Println("Connected to Redis")

	// gRPC clients for inter-service communication
	invConn, err := grpc.NewClient(inventoryAddr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Printf("WARN: Could not connect to inventory service: %v", err)
	}
	defer invConn.Close()

	finConn, err := grpc.NewClient(financeAddr, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Printf("WARN: Could not connect to finance service: %v", err)
	}
	defer finConn.Close()

	// Initialize layers
	repo := repository.NewOrderRepository(pool)
	svc := service.NewOrderService(repo, rdb, invConn, finConn)
	grpcHandler := handler.NewOrderHandler(svc)

	// Start gRPC
	lis, err := net.Listen("tcp", fmt.Sprintf(":%s", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	srv := grpc.NewServer()
	grpcHandler.Register(srv)
	reflection.Register(srv)

	go func() {
		log.Printf("Order Service starting on port %s", port)
		if err := srv.Serve(lis); err != nil {
			log.Fatalf("gRPC error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down Order Service...")
	srv.GracefulStop()
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
