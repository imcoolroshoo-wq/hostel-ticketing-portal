package com.hostel.entity;

/**
 * Enum representing the different statuses a ticket can have in the hostel ticketing system.
 * Status indicates the current state of the ticket in its lifecycle.
 */
public enum TicketStatus {
    
    /**
     * Open - ticket has been created and is waiting to be assigned
     */
    OPEN("Open", "Ticket created and waiting for assignment", "🆕"),
    
    /**
     * Assigned - ticket has been assigned to a staff member but work hasn't started
     */
    ASSIGNED("Assigned", "Ticket assigned to staff member", "👤"),
    
    /**
     * In Progress - work is actively being done on the ticket
     */
    IN_PROGRESS("In Progress", "Work is being done on the ticket", "🔄"),
    
    /**
     * On Hold - ticket is temporarily paused waiting for resources/information
     */
    ON_HOLD("On Hold", "Ticket is on hold waiting for resources or information", "⏸️"),
    
    /**
     * Resolved - issue has been fixed but ticket is not yet closed
     */
    RESOLVED("Resolved", "Issue has been fixed, waiting for closure", "✅"),
    
    /**
     * Closed - ticket has been completed and closed
     */
    CLOSED("Closed", "Ticket has been completed and closed", "🔒"),
    
    /**
     * Cancelled - ticket has been cancelled and will not be processed
     */
    CANCELLED("Cancelled", "Ticket has been cancelled", "❌"),
    
    /**
     * Reopened - previously closed ticket has been reopened
     */
    REOPENED("Reopened", "Ticket has been reopened for additional work", "🔄");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    TicketStatus(String displayName, String description, String icon) {
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
     * Check if this status is active (not closed or cancelled)
     */
    public boolean isActive() {
        return this != CLOSED && this != CANCELLED;
    }
    
    /**
     * Check if this status allows assignment
     */
    public boolean allowsAssignment() {
        return this == OPEN || this == REOPENED;
    }
    
    /**
     * Check if this status allows status changes
     */
    public boolean allowsStatusChange() {
        return this != CLOSED && this != CANCELLED;
    }
    
    /**
     * Check if this status allows comments
     */
    public boolean allowsComments() {
        return this != CLOSED && this != CANCELLED;
    }
    
    /**
     * Check if this status allows attachments
     */
    public boolean allowsAttachments() {
        return this != CLOSED && this != CANCELLED;
    }
    
    /**
     * Check if this status indicates completion
     */
    public boolean indicatesCompletion() {
        return this == RESOLVED || this == CLOSED;
    }
    
    /**
     * Check if this status requires user confirmation
     */
    public boolean requiresUserConfirmation() {
        return this == RESOLVED;
    }
    
    /**
     * Get the next possible statuses from this status
     */
    public TicketStatus[] getNextPossibleStatuses() {
        switch (this) {
            case OPEN:
                return new TicketStatus[]{ASSIGNED, CANCELLED};
            case ASSIGNED:
                return new TicketStatus[]{IN_PROGRESS, ON_HOLD, CANCELLED};
            case IN_PROGRESS:
                return new TicketStatus[]{ON_HOLD, RESOLVED, CANCELLED};
            case ON_HOLD:
                return new TicketStatus[]{IN_PROGRESS, RESOLVED, CANCELLED};
            case RESOLVED:
                return new TicketStatus[]{CLOSED, REOPENED};
            case CLOSED:
                return new TicketStatus[]{REOPENED};
            case CANCELLED:
                return new TicketStatus[]{OPEN}; // Reactivate
            case REOPENED:
                return new TicketStatus[]{ASSIGNED, IN_PROGRESS, CANCELLED};
            default:
                return new TicketStatus[]{};
        }
    }
    
    /**
     * Check if a transition to the target status is valid
     */
    public boolean canTransitionTo(TicketStatus targetStatus) {
        if (targetStatus == null) return false;
        
        TicketStatus[] possibleStatuses = getNextPossibleStatuses();
        for (TicketStatus status : possibleStatuses) {
            if (status == targetStatus) return true;
        }
        return false;
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case OPEN:
                return "primary";
            case ASSIGNED:
                return "info";
            case IN_PROGRESS:
                return "warning";
            case ON_HOLD:
                return "secondary";
            case RESOLVED:
                return "success";
            case CLOSED:
                return "default";
            case CANCELLED:
                return "error";
            case REOPENED:
                return "primary";
            default:
                return "default";
        }
    }
} 