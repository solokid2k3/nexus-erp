package com.erp.core.auth;

public final class SessionContext {

    private static SessionContext instance;

    private String userId;
    private String userName;
    private String userEmail;
    private String userRole;

    private SessionContext() {}

    public static synchronized SessionContext getInstance() {
        if (instance == null) instance = new SessionContext();
        return instance;
    }

    public void setUser(String id, String name, String email, String role) {
        this.userId = id;
        this.userName = name;
        this.userEmail = email;
        this.userRole = role;
    }

    public void clear() {
        userId = null;
        userName = null;
        userEmail = null;
        userRole = null;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserRole() { return userRole; }
    public boolean isLoggedIn() { return userId != null; }
}
