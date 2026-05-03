package com.erp.desktop.config;

public final class ApiConfig {
    private ApiConfig() {}

    public static final String BASE_URL = "http://localhost:8080";
    public static final String API_PREFIX = BASE_URL + "/api/v1";
    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int REQUEST_TIMEOUT_SECONDS = 30;
}
