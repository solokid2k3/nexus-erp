package handlers

import (
	"context"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"github.com/redis/go-redis/v9"
)

type LoginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type AuthResponse struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	ExpiresIn    int64  `json:"expires_in"`
	TokenType    string `json:"token_type"`
}

// Login godoc
// @Summary      User login
// @Description  Authenticate with username and password to receive JWT tokens
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        credentials  body      LoginRequest   true  "Login credentials"
// @Success      200          {object}  AuthResponse
// @Failure      400          {object}  map[string]interface{}
// @Failure      401          {object}  map[string]interface{}
// @Router       /api/v1/auth/login [post]
func Login(jwtSecret string, rdb *redis.Client) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req LoginRequest
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		// TODO: Replace with real user authentication against DB
		// This is a simplified version for demonstration
		userID, role, name, email := authenticateUser(req.Username, req.Password)
		if userID == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid credentials"})
			return
		}

		accessToken, err := generateToken(jwtSecret, userID, name, email, role, 24*time.Hour)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
			return
		}

		refreshToken, err := generateToken(jwtSecret, userID, name, email, role, 7*24*time.Hour)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate refresh token"})
			return
		}

		// Store refresh token in Redis
		ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
		defer cancel()
		rdb.Set(ctx, "refresh:"+userID, refreshToken, 7*24*time.Hour)

		c.JSON(http.StatusOK, AuthResponse{
			AccessToken:  accessToken,
			RefreshToken: refreshToken,
			ExpiresIn:    int64(24 * time.Hour / time.Second),
			TokenType:    "Bearer",
		})
	}
}

// RefreshToken godoc
// @Summary      Refresh access token
// @Description  Exchange a valid refresh token for a new access token
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        token  body      object{refresh_token=string}  true  "Refresh token"
// @Success      200    {object}  AuthResponse
// @Failure      400    {object}  map[string]interface{}
// @Failure      401    {object}  map[string]interface{}
// @Router       /api/v1/auth/refresh [post]
func RefreshToken(jwtSecret string, rdb *redis.Client) gin.HandlerFunc {
	return func(c *gin.Context) {
		var req struct {
			RefreshToken string `json:"refresh_token" binding:"required"`
		}
		if err := c.ShouldBindJSON(&req); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		token, err := jwt.Parse(req.RefreshToken, func(token *jwt.Token) (interface{}, error) {
			return []byte(jwtSecret), nil
		})
		if err != nil || !token.Valid {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid refresh token"})
			return
		}

		claims := token.Claims.(jwt.MapClaims)
		userID := claims["sub"].(string)
		name := claims["name"].(string)
		email := claims["email"].(string)
		role := claims["role"].(string)

		accessToken, err := generateToken(jwtSecret, userID, name, email, role, 24*time.Hour)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
			return
		}

		c.JSON(http.StatusOK, AuthResponse{
			AccessToken: accessToken,
			ExpiresIn:   int64(24 * time.Hour / time.Second),
			TokenType:   "Bearer",
		})
	}
}

func generateToken(secret, userID, name, email, role string, duration time.Duration) (string, error) {
	claims := jwt.MapClaims{
		"sub":   userID,
		"name":  name,
		"email": email,
		"role":  role,
		"exp":   time.Now().Add(duration).Unix(),
		"iat":   time.Now().Unix(),
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(secret))
}

// Simplified user auth - replace with real DB lookup
func authenticateUser(username, password string) (id, role, name, email string) {
	// Demo users for development
	users := map[string]struct {
		password string
		id       string
		role     string
		name     string
		email    string
	}{
		"admin":   {"admin123", "usr_001", "ADMIN", "System Admin", "admin@erp.com"},
		"manager": {"manager123", "usr_002", "MANAGER", "Operations Manager", "manager@erp.com"},
		"finance": {"finance123", "usr_003", "FINANCE", "Finance Lead", "finance@erp.com"},
		"hr":      {"hr123", "usr_004", "HR", "HR Manager", "hr@erp.com"},
		"sales":   {"sales123", "usr_005", "SALES", "Sales Rep", "sales@erp.com"},
	}

	if u, ok := users[username]; ok && u.password == password {
		return u.id, u.role, u.name, u.email
	}
	return "", "", "", ""
}
