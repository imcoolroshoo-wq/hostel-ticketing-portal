package com.hostel.entity;

/**
 * Enum representing the different user roles in the hostel ticketing system.
 * Each role has specific permissions and access levels.
 */
public enum UserRole {
    
    /**
     * Student role - can create tickets, view own tickets, and add comments
     */
    STUDENT("Student", "Can create and track tickets"),
    
    /**
     * Staff role - can manage assigned tickets, update status, and communicate with students
     */
    STAFF("Staff", "Can manage assigned tickets and update status"),
    
    /**
     * Admin role - has full system access including user management and reporting
     */
    ADMIN("Administrator", "Full system access and user management");
    
    private final String displayName;
    private final String description;
    
    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this role has admin privileges
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role has staff privileges
     */
    public boolean isStaff() {
        return this == STAFF || this == ADMIN;
    }
    
    /**
     * Check if this role can manage tickets
     */
    public boolean canManageTickets() {
        return this == STAFF || this == ADMIN;
    }
    
    /**
     * Check if this role can assign tickets
     */
    public boolean canAssignTickets() {
        return this == STAFF || this == ADMIN;
    }
    
    /**
     * Check if this role can view all tickets
     */
    public boolean canViewAllTickets() {
        return this == STAFF || this == ADMIN;
    }
    
    /**
     * Check if this role can manage users
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }
    
    /**
     * Check if this role can generate reports
     */
    public boolean canGenerateReports() {
        return this == STAFF || this == ADMIN;
    }
    
    /**
     * Check if this role can escalate tickets
     */
    public boolean canEscalateTickets() {
        return this == STAFF || this == ADMIN;
    }
} 