package com.erp.core.api;

import com.erp.core.config.ApiConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private static ApiClient instance;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private String accessToken;
    private String refreshToken;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ApiConfig.CONNECT_TIMEOUT_SECONDS))
                .build();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public ObjectMapper getMapper() { return mapper; }

    public void setTokens(String access, String refresh) {
        this.accessToken = access;
        this.refreshToken = refresh;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }

    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }

    public CompletableFuture<Map<String, Object>> get(String path) {
        var req = buildRequest(path).GET().build();
        return sendAsync(req);
    }

    public CompletableFuture<Map<String, Object>> post(String path, Object body) {
        var json = toJson(body);
        var req = buildRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return sendAsync(req);
    }

    public CompletableFuture<Map<String, Object>> put(String path, Object body) {
        var json = toJson(body);
        var req = buildRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return sendAsync(req);
    }

    public CompletableFuture<String> getRaw(String path) {
        var req = buildRequest(path).GET().build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<String> postRaw(String path, Object body) {
        var json = toJson(body);
        var req = buildRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    private HttpRequest.Builder buildRequest(String path) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(ApiConfig.API_PREFIX + path))
                .timeout(Duration.ofSeconds(ApiConfig.REQUEST_TIMEOUT_SECONDS));
        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Map<String, Object>> sendAsync(HttpRequest request) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    try {
                        if (resp.statusCode() >= 400) {
                            Map<String, Object> err = mapper.readValue(resp.body(), new TypeReference<>() {});
                            err.put("_statusCode", resp.statusCode());
                            return err;
                        }
                        return mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        return Map.of("error", e.getMessage(), "_statusCode", resp.statusCode());
                    }
                });
    }

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }
}
