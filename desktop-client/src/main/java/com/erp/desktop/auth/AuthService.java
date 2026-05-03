package com.erp.desktop.auth;

import com.erp.desktop.api.ApiClient;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.erp.desktop.config.ApiConfig.BASE_URL;

/**
 * Handles login, token refresh, and JWT claim parsing.
 */
public class AuthService {

    private final ApiClient api = ApiClient.getInstance();
    private final SessionContext session = SessionContext.getInstance();

    public CompletableFuture<Boolean> login(String username, String password) {
        var body = Map.of("username", username, "password", password);

        return api.post("/auth/login", body).thenApply(resp -> {
            if (resp.containsKey("access_token")) {
                String access = (String) resp.get("access_token");
                String refresh = (String) resp.get("refresh_token");
                api.setTokens(access, refresh);
                parseAndSetSession(access);
                return true;
            }
            return false;
        });
    }

    public void logout() {
        api.clearTokens();
        session.clear();
    }

    private void parseAndSetSession(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            var claims = api.getMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});
            session.setUser(
                    (String) claims.get("sub"),
                    (String) claims.get("name"),
                    (String) claims.get("email"),
                    (String) claims.get("role")
            );
        } catch (Exception ignored) {}
    }
}
