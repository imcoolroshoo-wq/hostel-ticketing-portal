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
    IN_APP("In-App", "Displayed within the application", "🔔"),
    
    /**
     * Ticket assignment notification
     */
    TICKET_ASSIGNMENT("Ticket Assignment", "Notification for ticket assignment", "👤"),
    
    /**
     * Status update notification
     */
    STATUS_UPDATE("Status Update", "Notification for ticket status changes", "🔄"),
    
    /**
     * SLA warning notification
     */
    SLA_WARNING("SLA Warning", "Warning for approaching SLA breach", "⚠️"),
    
    /**
     * SLA breach notification
     */
    SLA_BREACH("SLA Breach", "Alert for SLA breach", "🚨"),
    
    /**
     * Escalation notification
     */
    ESCALATION("Escalation", "Notification for ticket escalation", "📈"),
    
    /**
     * Resolution notification
     */
    RESOLUTION("Resolution", "Notification for ticket resolution", "✅"),
    
    /**
     * Feedback request notification
     */
    FEEDBACK_REQUEST("Feedback Request", "Request for user feedback", "💬"),
    
    /**
     * System notification
     */
    SYSTEM("System", "General system notifications", "⚙️"),
    
    /**
     * Maintenance notification
     */
    MAINTENANCE("Maintenance", "Maintenance schedule notifications", "🔧"),
    
    /**
     * Emergency notification
     */
    EMERGENCY("Emergency", "Critical emergency notifications", "🚨"),
    
    /**
     * Resolution verification notification
     */
    RESOLUTION_VERIFICATION("Resolution Verification", "Request for resolution verification", "✔️"),
    
    /**
     * Ticket closed notification
     */
    TICKET_CLOSED("Ticket Closed", "Notification when ticket is closed", "🔒"),
    
    /**
     * Ticket assigned notification
     */
    TICKET_ASSIGNED("Ticket Assigned", "Notification when ticket is assigned", "👤"),
    
    /**
     * Status change notification
     */
    STATUS_CHANGE("Status Change", "Notification for status changes", "🔄"),
    
    /**
     * Quality review notification
     */
    QUALITY_REVIEW("Quality Review", "Notification for quality review required", "🔍"),
    
    /**
     * Recurring issue notification
     */
    RECURRING_ISSUE("Recurring Issue", "Notification for recurring issues detected", "🔁"),
    
    /**
     * System alert notification
     */
    SYSTEM_ALERT("System Alert", "System alerts and warnings", "⚠️");
    
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
        return this == IN_APP || this == SMS || this == EMERGENCY || this == SLA_BREACH || this == ESCALATION;
    }
    
    /**
     * Check if this notification type can be delayed
     */
    public boolean canBeDelayed() {
        return this == EMAIL || this == SYSTEM || this == MAINTENANCE;
    }
    
    /**
     * Get the delivery priority for this notification type
     */
    public int getDeliveryPriority() {
        switch (this) {
            case EMERGENCY:
            case SLA_BREACH:
                return 1; // Critical priority
            case SMS:
            case ESCALATION:
                return 2; // High priority
            case SLA_WARNING:
            case TICKET_ASSIGNMENT:
            case STATUS_UPDATE:
                return 3; // Medium-high priority
            case IN_APP:
            case RESOLUTION:
            case FEEDBACK_REQUEST:
                return 4; // Medium priority
            case EMAIL:
            case SYSTEM:
                return 5; // Low priority
            case MAINTENANCE:
                return 6; // Lowest priority
            default:
                return 5;
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