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

	"erp-system/inventory-service/internal/handler"
	"erp-system/inventory-service/internal/repository"
	"erp-system/inventory-service/internal/service"

	"github.com/jackc/pgx/v5/pgxpool"
	"github.com/redis/go-redis/v9"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func main() {
	port := getEnv("GRPC_PORT", "50051")
	dbURL := getEnv("DATABASE_URL", "postgres://erp_user:erp_secret_2025@localhost:5432/erp_db?sslmode=disable")
	redisAddr := getEnv("REDIS_ADDR", "localhost:6379")

	// Database connection pool
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	poolCfg, err := pgxpool.ParseConfig(dbURL)
	if err != nil {
		log.Fatalf("Failed to parse DB config: %v", err)
	}
	poolCfg.MaxConns = 20
	poolCfg.MinConns = 5

	pool, err := pgxpool.NewWithConfig(ctx, poolCfg)
	if err != nil {
		log.Fatalf("Failed to connect to DB: %v", err)
	}
	defer pool.Close()

	if err := pool.Ping(ctx); err != nil {
		log.Fatalf("DB ping failed: %v", err)
	}
	log.Println("Connected to PostgreSQL")

	// Redis client
	rdb := redis.NewClient(&redis.Options{
		Addr:     redisAddr,
		DB:       0,
		PoolSize: 10,
	})
	defer rdb.Close()

	if err := rdb.Ping(ctx).Err(); err != nil {
		log.Fatalf("Redis connection failed: %v", err)
	}
	log.Println("Connected to Redis")

	// Initialize layers
	repo := repository.NewInventoryRepository(pool)
	svc := service.NewInventoryService(repo, rdb)
	grpcHandler := handler.NewInventoryHandler(svc)

	// Start gRPC server
	lis, err := net.Listen("tcp", fmt.Sprintf(":%s", port))
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	srv := grpc.NewServer()
	grpcHandler.Register(srv)
	reflection.Register(srv)

	go func() {
		log.Printf("Inventory Service starting on port %s", port)
		if err := srv.Serve(lis); err != nil {
			log.Fatalf("gRPC server error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("Shutting down Inventory Service...")
	srv.GracefulStop()
	log.Println("Inventory Service stopped")
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
