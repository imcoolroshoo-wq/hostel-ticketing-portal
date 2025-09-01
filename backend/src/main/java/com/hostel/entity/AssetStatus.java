package com.hostel.entity;

/**
 * Enum representing the status of assets in the hostel
 */
public enum AssetStatus {
    
    /**
     * Asset is active and in use
     */
    ACTIVE("Active", "Asset is in use and functioning", "✅"),
    
    /**
     * Asset is under maintenance
     */
    MAINTENANCE("Under Maintenance", "Asset is being serviced or repaired", "🔧"),
    
    /**
     * Asset is out of order and needs repair
     */
    OUT_OF_ORDER("Out of Order", "Asset is not functioning and needs repair", "❌"),
    
    /**
     * Asset is retired and no longer in use
     */
    RETIRED("Retired", "Asset is no longer in service", "📦"),
    
    /**
     * Asset is lost or missing
     */
    LOST("Lost", "Asset cannot be located", "❓"),
    
    /**
     * Asset is damaged beyond repair
     */
    DAMAGED("Damaged", "Asset is damaged and cannot be repaired", "💥"),
    
    /**
     * Asset is in storage
     */
    STORED("In Storage", "Asset is stored and not currently in use", "📦"),
    
    /**
     * Asset is reserved for future use
     */
    RESERVED("Reserved", "Asset is reserved for specific use", "🔒"),
    
    /**
     * Asset is being transferred between locations
     */
    IN_TRANSIT("In Transit", "Asset is being moved to a new location", "🚚");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    AssetStatus(String displayName, String description, String icon) {
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
     * Check if the asset is available for use
     */
    public boolean isAvailable() {
        return this == ACTIVE;
    }
    
    /**
     * Check if the asset is operational (can be used)
     */
    public boolean isOperational() {
        return this == ACTIVE || this == RESERVED;
    }
    
    /**
     * Check if the asset needs attention
     */
    public boolean needsAttention() {
        return this == OUT_OF_ORDER || 
               this == DAMAGED || 
               this == LOST ||
               this == MAINTENANCE;
    }
    
    /**
     * Check if the asset can be assigned to users
     */
    public boolean canBeAssigned() {
        return this == ACTIVE || this == RESERVED;
    }
    
    /**
     * Check if the asset can be moved
     */
    public boolean canBeMoved() {
        return this != OUT_OF_ORDER && 
               this != DAMAGED && 
               this != LOST &&
               this != IN_TRANSIT;
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case ACTIVE:
                return "success";
            case MAINTENANCE:
                return "warning";
            case OUT_OF_ORDER:
            case DAMAGED:
                return "error";
            case RETIRED:
            case STORED:
                return "secondary";
            case LOST:
                return "error";
            case RESERVED:
                return "info";
            case IN_TRANSIT:
                return "primary";
            default:
                return "default";
        }
    }
    
    /**
     * Get the next possible statuses from this status
     */
    public AssetStatus[] getNextPossibleStatuses() {
        switch (this) {
            case ACTIVE:
                return new AssetStatus[]{MAINTENANCE, OUT_OF_ORDER, RETIRED, STORED, RESERVED, IN_TRANSIT, LOST};
            case MAINTENANCE:
                return new AssetStatus[]{ACTIVE, OUT_OF_ORDER, DAMAGED, RETIRED};
            case OUT_OF_ORDER:
                return new AssetStatus[]{MAINTENANCE, ACTIVE, DAMAGED, RETIRED};
            case RETIRED:
                return new AssetStatus[]{STORED, ACTIVE}; // Can be reactivated
            case LOST:
                return new AssetStatus[]{ACTIVE, DAMAGED, RETIRED}; // If found
            case DAMAGED:
                return new AssetStatus[]{RETIRED, MAINTENANCE}; // Might be repairable
            case STORED:
                return new AssetStatus[]{ACTIVE, RETIRED, IN_TRANSIT};
            case RESERVED:
                return new AssetStatus[]{ACTIVE, STORED, IN_TRANSIT};
            case IN_TRANSIT:
                return new AssetStatus[]{ACTIVE, STORED, LOST};
            default:
                return new AssetStatus[]{};
        }
    }
    
    /**
     * Check if a transition to the target status is valid
     */
    public boolean canTransitionTo(AssetStatus targetStatus) {
        if (targetStatus == null) return false;
        
        AssetStatus[] possibleStatuses = getNextPossibleStatuses();
        for (AssetStatus status : possibleStatuses) {
            if (status == targetStatus) return true;
        }
        return false;
    }
}
