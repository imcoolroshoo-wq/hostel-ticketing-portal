package com.hostel.entity;

/**
 * Enum representing escalation levels in the multi-tier escalation hierarchy
 * Implements Escalation Hierarchy as per PDD Section 5.3.2
 */
public enum EscalationLevel {
    
    /**
     * Level 1: Assigned Staff Member
     */
    STAFF_MEMBER(1, "Staff Member", "Initial assignment to staff member", "👤"),
    
    /**
     * Level 2: Team Lead/Supervisor
     */
    TEAM_LEAD(2, "Team Lead/Supervisor", "Escalated to team lead or supervisor", "👥"),
    
    /**
     * Level 3: Department Head/Warden
     */
    DEPARTMENT_HEAD(3, "Department Head/Warden", "Escalated to department head or warden", "🏢"),
    
    /**
     * Level 4: Hostel Administration
     */
    HOSTEL_ADMINISTRATION(4, "Hostel Administration", "Escalated to hostel administration", "🏛️"),
    
    /**
     * Level 5: Institute Administration
     */
    INSTITUTE_ADMINISTRATION(5, "Institute Administration", "Escalated to institute administration", "🏫");
    
    private final int level;
    private final String displayName;
    private final String description;
    private final String icon;
    
    EscalationLevel(int level, String displayName, String description, String icon) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    public int getLevel() {
        return level;
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
     * Get the next escalation level
     */
    public EscalationLevel getNextLevel() {
        switch (this) {
            case STAFF_MEMBER:
                return TEAM_LEAD;
            case TEAM_LEAD:
                return DEPARTMENT_HEAD;
            case DEPARTMENT_HEAD:
                return HOSTEL_ADMINISTRATION;
            case HOSTEL_ADMINISTRATION:
                return INSTITUTE_ADMINISTRATION;
            case INSTITUTE_ADMINISTRATION:
                return null; // No further escalation
            default:
                return null;
        }
    }
    
    /**
     * Check if this level can be escalated further
     */
    public boolean canEscalate() {
        return this != INSTITUTE_ADMINISTRATION;
    }
    
    /**
     * Check if this level is critical (requires immediate attention)
     */
    public boolean isCritical() {
        return level >= 4;
    }
    
    /**
     * Get the escalation time threshold in hours for this level
     */
    public int getEscalationThresholdHours(TicketPriority priority) {
        int baseHours = getBaseEscalationHours();
        
        switch (priority) {
            case EMERGENCY:
                return Math.max(1, baseHours / 4);
            case HIGH:
                return Math.max(2, baseHours / 2);
            case MEDIUM:
                return baseHours;
            case LOW:
                return baseHours * 2;
            default:
                return baseHours;
        }
    }
    
    /**
     * Get base escalation hours for this level
     */
    private int getBaseEscalationHours() {
        switch (this) {
            case STAFF_MEMBER:
                return 4; // 4 hours without progress
            case TEAM_LEAD:
                return 8; // 8 hours at team lead level
            case DEPARTMENT_HEAD:
                return 12; // 12 hours at department head level
            case HOSTEL_ADMINISTRATION:
                return 24; // 24 hours at hostel admin level
            case INSTITUTE_ADMINISTRATION:
                return 48; // 48 hours at institute level (final)
            default:
                return 4;
        }
    }
    
    /**
     * Get the staff roles that should be notified at this level
     */
    public StaffVertical[] getNotificationRoles() {
        switch (this) {
            case STAFF_MEMBER:
                return new StaffVertical[]{
                    StaffVertical.ELECTRICAL, StaffVertical.PLUMBING, 
                    StaffVertical.HVAC, StaffVertical.IT_SUPPORT,
                    StaffVertical.GENERAL_MAINTENANCE, StaffVertical.HOUSEKEEPING
                };
            case TEAM_LEAD:
                return new StaffVertical[]{
                    StaffVertical.BLOCK_SUPERVISOR, StaffVertical.MAINTENANCE_SUPERVISOR
                };
            case DEPARTMENT_HEAD:
                return new StaffVertical[]{
                    StaffVertical.HOSTEL_WARDEN, StaffVertical.ASSISTANT_WARDEN
                };
            case HOSTEL_ADMINISTRATION:
                return new StaffVertical[]{
                    StaffVertical.HOSTEL_WARDEN, StaffVertical.CHIEF_WARDEN
                };
            case INSTITUTE_ADMINISTRATION:
                return new StaffVertical[]{
                    StaffVertical.CHIEF_WARDEN, StaffVertical.ADMIN_OFFICER
                };
            default:
                return new StaffVertical[]{};
        }
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case STAFF_MEMBER:
                return "primary";
            case TEAM_LEAD:
                return "info";
            case DEPARTMENT_HEAD:
                return "warning";
            case HOSTEL_ADMINISTRATION:
                return "error";
            case INSTITUTE_ADMINISTRATION:
                return "error";
            default:
                return "default";
        }
    }
    
    /**
     * Get escalation level from integer
     */
    public static EscalationLevel fromLevel(int level) {
        for (EscalationLevel escalationLevel : values()) {
            if (escalationLevel.getLevel() == level) {
                return escalationLevel;
            }
        }
        return STAFF_MEMBER; // Default
    }
}
