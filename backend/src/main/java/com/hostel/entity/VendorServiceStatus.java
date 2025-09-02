package com.hostel.entity;

/**
 * Enum representing the status of vendor service requests
 */
public enum VendorServiceStatus {
    
    /**
     * Service has been requested from vendor
     */
    REQUESTED("Requested", "Service has been requested from vendor", "📋"),
    
    /**
     * Quote has been received from vendor
     */
    QUOTE_RECEIVED("Quote Received", "Quote has been received from vendor", "💰"),
    
    /**
     * Service request has been approved by admin
     */
    APPROVED("Approved", "Service request has been approved", "✅"),
    
    /**
     * Vendor is actively working on the service
     */
    IN_PROGRESS("In Progress", "Vendor is working on the service", "🔄"),
    
    /**
     * Service has been completed by vendor
     */
    COMPLETED("Completed", "Service has been completed", "✔️"),
    
    /**
     * Service request has been cancelled
     */
    CANCELLED("Cancelled", "Service request has been cancelled", "❌"),
    
    /**
     * Service request has been rejected
     */
    REJECTED("Rejected", "Service request has been rejected", "🚫"),
    
    /**
     * Service is on hold waiting for approval or resources
     */
    ON_HOLD("On Hold", "Service is on hold", "⏸️");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    VendorServiceStatus(String displayName, String description, String icon) {
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
     * Check if status is active (not completed, cancelled, or rejected)
     */
    public boolean isActive() {
        return this != COMPLETED && this != CANCELLED && this != REJECTED;
    }
    
    /**
     * Check if status allows updates
     */
    public boolean allowsUpdates() {
        return this != COMPLETED && this != CANCELLED && this != REJECTED;
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case REQUESTED:
                return "primary";
            case QUOTE_RECEIVED:
                return "info";
            case APPROVED:
                return "success";
            case IN_PROGRESS:
                return "warning";
            case COMPLETED:
                return "success";
            case CANCELLED:
                return "default";
            case REJECTED:
                return "error";
            case ON_HOLD:
                return "secondary";
            default:
                return "default";
        }
    }
}
