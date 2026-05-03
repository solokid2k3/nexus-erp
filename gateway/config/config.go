package config

import "os"

type Config struct {
	ServerPort          string
	InventoryServiceAddr string
	OrderServiceAddr    string
	FinanceServiceAddr  string
	HRServiceAddr       string
	RedisAddr           string
	JWTSecret           string
}

func Load() *Config {
	return &Config{
		ServerPort:          getEnv("SERVER_PORT", "8080"),
		InventoryServiceAddr: getEnv("INVENTORY_SERVICE_ADDR", "localhost:50051"),
		OrderServiceAddr:    getEnv("ORDER_SERVICE_ADDR", "localhost:50052"),
		FinanceServiceAddr:  getEnv("FINANCE_SERVICE_ADDR", "localhost:50053"),
		HRServiceAddr:       getEnv("HR_SERVICE_ADDR", "localhost:50054"),
		RedisAddr:           getEnv("REDIS_ADDR", "localhost:6379"),
		JWTSecret:           getEnv("JWT_SECRET", "erp-jwt-secret-key-change-in-production"),
	}
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
