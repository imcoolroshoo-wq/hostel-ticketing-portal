package com.hostel.entity;

/**
 * Enum representing vendor status in the hostel ticketing system
 */
public enum VendorStatus {
    
    /**
     * Active - vendor is available for service requests
     */
    ACTIVE("Active", "Vendor is available for service requests", "✅"),
    
    /**
     * Inactive - vendor is temporarily unavailable
     */
    INACTIVE("Inactive", "Vendor is temporarily unavailable", "⏸️"),
    
    /**
     * Blacklisted - vendor is permanently banned due to poor performance
     */
    BLACKLISTED("Blacklisted", "Vendor is banned due to poor performance", "🚫"),
    
    /**
     * Contract Expired - vendor's contract has expired
     */
    CONTRACT_EXPIRED("Contract Expired", "Vendor's contract has expired", "📅"),
    
    /**
     * Under Review - vendor is being evaluated
     */
    UNDER_REVIEW("Under Review", "Vendor is being evaluated", "🔍"),
    
    /**
     * Pending Approval - new vendor waiting for approval
     */
    PENDING_APPROVAL("Pending Approval", "New vendor waiting for approval", "⏳");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    VendorStatus(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    /**
     * Check if vendor can receive new service requests
     */
    public boolean canReceiveRequests() {
        return this == ACTIVE;
    }
    
    /**
     * Check if status allows contract renewal
     */
    public boolean allowsContractRenewal() {
        return this == ACTIVE || this == INACTIVE || this == CONTRACT_EXPIRED;
    }
    
    /**
     * Check if status indicates a problem
     */
    public boolean indicatesProblem() {
        return this == BLACKLISTED || this == UNDER_REVIEW;
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case ACTIVE:
                return "success";
            case INACTIVE:
                return "warning";
            case BLACKLISTED:
                return "error";
            case CONTRACT_EXPIRED:
                return "error";
            case UNDER_REVIEW:
                return "warning";
            case PENDING_APPROVAL:
                return "info";
            default:
                return "default";
        }
    }
}
