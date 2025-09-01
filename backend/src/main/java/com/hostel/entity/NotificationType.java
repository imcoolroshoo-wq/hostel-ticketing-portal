package com.hostel.entity;

/**
 * Enum representing the different types of notifications in the hostel ticketing system.
 * Each type has specific delivery methods and characteristics.
 */
public enum NotificationType {
    
    /**
     * Email notification - sent via email
     */
    EMAIL("Email", "Sent via email", "📧"),
    
    /**
     * SMS notification - sent via text message
     */
    SMS("SMS", "Sent via text message", "📱"),
    
    /**
     * In-app notification - displayed within the application
     */
    IN_APP("In-App", "Displayed within the application", "🔔");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    NotificationType(String displayName, String description, String icon) {
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
     * Check if this notification type requires external service
     */
    public boolean requiresExternalService() {
        return this == EMAIL || this == SMS;
    }
    
    /**
     * Check if this notification type is immediate
     */
    public boolean isImmediate() {
        return this == IN_APP || this == SMS;
    }
    
    /**
     * Check if this notification type can be delayed
     */
    public boolean canBeDelayed() {
        return this == EMAIL;
    }
    
    /**
     * Get the delivery priority for this notification type
     */
    public int getDeliveryPriority() {
        switch (this) {
            case SMS:
                return 1; // Highest priority
            case IN_APP:
                return 2; // Medium priority
            case EMAIL:
                return 3; // Lowest priority
            default:
                return 3;
        }
    }
    
    /**
     * Get the retry attempts for this notification type
     */
    public int getRetryAttempts() {
        switch (this) {
            case SMS:
                return 3; // 3 retry attempts
            case EMAIL:
                return 5; // 5 retry attempts
            case IN_APP:
                return 0; // No retry needed
            default:
                return 0;
        }
    }
    
    /**
     * Check if this notification type supports delivery confirmation
     */
    public boolean supportsDeliveryConfirmation() {
        return this == EMAIL || this == SMS;
    }
    
    /**
     * Get the default delivery delay in minutes
     */
    public int getDefaultDeliveryDelayMinutes() {
        switch (this) {
            case SMS:
                return 0; // Immediate
            case IN_APP:
                return 0; // Immediate
            case EMAIL:
                return 5; // 5 minutes delay
            default:
                return 0;
        }
    }
} 