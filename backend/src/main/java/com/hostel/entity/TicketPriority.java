package com.hostel.entity;

/**
 * Enum representing the different priority levels for tickets in the hostel ticketing system.
 * Priority determines the order in which tickets should be handled and their escalation rules.
 */
public enum TicketPriority {
    
    /**
     * Low priority - non-urgent issues that can be handled during normal business hours
     */
    LOW("Low", "Non-urgent issues", 1, "🟢"),
    
    /**
     * Medium priority - standard issues that should be addressed within 24-48 hours
     */
    MEDIUM("Medium", "Standard issues", 2, "🟡"),
    
    /**
     * High priority - important issues that require prompt attention
     */
    HIGH("High", "Important issues", 3, "🟠"),
    
    /**
     * Emergency priority - critical issues that require immediate attention
     */
    EMERGENCY("Emergency", "Critical issues requiring immediate attention", 4, "🔴");
    
    private final String displayName;
    private final String description;
    private final int level;
    private final String icon;
    
    TicketPriority(String displayName, String description, int level, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getIcon() {
        return icon;
    }
    
    /**
     * Check if this priority is emergency
     */
    public boolean isEmergency() {
        return this == EMERGENCY;
    }
    
    /**
     * Check if this priority is high or emergency
     */
    public boolean isHighOrEmergency() {
        return this == HIGH || this == EMERGENCY;
    }
    
    /**
     * Check if this priority requires escalation
     */
    public boolean requiresEscalation() {
        return this == EMERGENCY || this == HIGH;
    }
    
    /**
     * Get the escalation time in hours for this priority
     */
    public int getEscalationHours() {
        switch (this) {
            case EMERGENCY:
                return 1; // 1 hour
            case HIGH:
                return 4; // 4 hours
            case MEDIUM:
                return 24; // 24 hours
            case LOW:
                return 72; // 72 hours
            default:
                return 24; // 24 hours
        }
    }
    
    /**
     * Get the notification frequency in hours for this priority
     */
    public int getNotificationFrequencyHours() {
        switch (this) {
            case EMERGENCY:
                return 1; // Every hour
            case HIGH:
                return 4; // Every 4 hours
            case MEDIUM:
                return 12; // Every 12 hours
            case LOW:
                return 24; // Every 24 hours
            default:
                return 12; // Every 12 hours
        }
    }
    
    /**
     * Compare priority levels
     */
    public boolean isHigherThan(TicketPriority other) {
        return this.level > other.level;
    }
    
    public boolean isLowerThan(TicketPriority other) {
        return this.level < other.level;
    }
    
    public boolean isSameOrHigherThan(TicketPriority other) {
        return this.level >= other.level;
    }
} 