package com.pawlink.vet.api;

/**
 * Thread-safe Singleton storing the currently logged-in user's session context.
 * In the Swing client, this holds the user's ID, name, email, and role,
 * which is automatically attached to outgoing HTTP requests via ApiClient.
 */
public class Session {

    private static volatile Session instance;

    private Long userId;
    private String name;
    private String email;
    private String role;

    private Session() {
    }

    public static Session getInstance() {
        if (instance == null) {
            synchronized (Session.class) {
                if (instance == null) {
                    instance = new Session();
                }
            }
        }
        return instance;
    }

    public synchronized void login(Long userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : null;
    }

    public synchronized void logout() {
        this.userId = null;
        this.name = null;
        this.email = null;
        this.role = null;
    }

    public synchronized boolean isLoggedIn() {
        return this.userId != null;
    }

    public synchronized Long getUserId() {
        return userId;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized String getEmail() {
        return email;
    }

    public synchronized String getRole() {
        return role;
    }

    public synchronized boolean isVet() {
        return "VET".equalsIgnoreCase(this.role);
    }

    public synchronized boolean isOwner() {
        return "OWNER".equalsIgnoreCase(this.role);
    }

    public synchronized boolean isSitter() {
        return "SITTER".equalsIgnoreCase(this.role);
    }

    public synchronized boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public synchronized String getDisplayTitle() {
        if (!isLoggedIn()) return "Guest";
        return name + " [" + role + "]";
    }
}
