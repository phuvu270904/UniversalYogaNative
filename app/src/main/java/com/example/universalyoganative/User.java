package com.example.universalyoganative;

public class User {
    private long id;
    private String name;
    private String email;
    private String password;
    private String role; // "admin" or "user"
    private String createdDate;
    private int syncStatus; // 0 = not synced, 1 = synced

    // Default constructor
    public User() {
    }

    // Constructor with parameters
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Constructor with all parameters including ID
    public User(long id, String name, String email, String password, String role, String createdDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdDate = createdDate;
        this.syncStatus = 0; // Default to not synced
    }

    // Constructor with all parameters including sync status
    public User(long id, String name, String email, String password, String role, String createdDate, int syncStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdDate = createdDate;
        this.syncStatus = syncStatus;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

    // Helper methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "user".equalsIgnoreCase(role);
    }

    public boolean isSynced() {
        return syncStatus == 1;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", syncStatus=" + syncStatus +
                '}';
    }
} 