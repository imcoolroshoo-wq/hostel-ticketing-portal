package com.hostel.entity;

/**
 * Enum representing different staff verticals/specializations in the IIM Trichy hostel system.
 * Each vertical corresponds to specific expertise areas and ticket categories.
 */
public enum StaffVertical {
    
    // =====================================================
    // TECHNICAL STAFF VERTICALS
    // =====================================================
    
    /**
     * Electrical technicians and electricians
     */
    ELECTRICAL("Electrical", "Electrical technicians and electricians", "⚡", 8),
    
    /**
     * Plumbing specialists and water system technicians
     */
    PLUMBING("Plumbing", "Plumbing specialists and water system technicians", "🚰", 8),
    
    /**
     * HVAC technicians and climate control specialists
     */
    HVAC("HVAC", "HVAC technicians and climate control specialists", "❄️", 8),
    
    /**
     * Carpentry and furniture repair specialists
     */
    CARPENTRY("Carpentry", "Carpentry and furniture repair specialists", "🪚", 6),
    
    /**
     * IT support and computer technicians
     */
    IT_SUPPORT("IT Support", "IT support and computer technicians", "💻", 10),
    
    /**
     * Network administrators and infrastructure specialists
     */
    NETWORK_ADMIN("Network Admin", "Network administrators and infrastructure specialists", "🌐", 12),
    
    /**
     * Security systems technicians
     */
    SECURITY_SYSTEMS("Security Systems", "Security systems technicians", "📹", 6),
    
    // =====================================================
    // GENERAL MAINTENANCE VERTICALS
    // =====================================================
    
    /**
     * Housekeeping and cleaning staff
     */
    HOUSEKEEPING("Housekeeping", "Housekeeping and cleaning staff", "🧽", 5),
    
    /**
     * Landscaping and outdoor maintenance
     */
    LANDSCAPING("Landscaping", "Landscaping and outdoor maintenance", "🌳", 4),
    
    /**
     * General maintenance workers
     */
    GENERAL_MAINTENANCE("General Maintenance", "General maintenance workers", "🔧", 6),
    
    // =====================================================
    // ADMINISTRATIVE VERTICALS
    // =====================================================
    
    /**
     * Hostel wardens and residential supervisors
     */
    HOSTEL_WARDEN("Hostel Warden", "Hostel wardens and residential supervisors", "🏠", 12),
    
    /**
     * Block supervisors and floor coordinators
     */
    BLOCK_SUPERVISOR("Block Supervisor", "Block supervisors and floor coordinators", "👥", 10),
    
    /**
     * Security officers and safety personnel
     */
    SECURITY_OFFICER("Security Officer", "Security officers and safety personnel", "🔒", 5),
    
    /**
     * Administrative staff and coordinators
     */
    ADMIN_STAFF("Admin Staff", "Administrative staff and coordinators", "📋", 8);
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final int maxActiveTickets; // Maximum tickets this vertical can handle simultaneously
    
    StaffVertical(String displayName, String description, String icon, int maxActiveTickets) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.maxActiveTickets = maxActiveTickets;
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
    
    public int getMaxActiveTickets() {
        return maxActiveTickets;
    }
    
    /**
     * Check if this vertical is technical (requires specialized skills)
     */
    public boolean isTechnical() {
        switch (this) {
            case ELECTRICAL:
            case PLUMBING:
            case HVAC:
            case CARPENTRY:
            case IT_SUPPORT:
            case NETWORK_ADMIN:
            case SECURITY_SYSTEMS:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Check if this vertical is administrative
     */
    public boolean isAdministrative() {
        switch (this) {
            case HOSTEL_WARDEN:
            case BLOCK_SUPERVISOR:
            case ADMIN_STAFF:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Check if this vertical can handle emergency situations
     */
    public boolean canHandleEmergencies() {
        switch (this) {
            case ELECTRICAL:
            case PLUMBING:
            case SECURITY_OFFICER:
            case HOSTEL_WARDEN:
            case BLOCK_SUPERVISOR:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get the vertical group for organizational purposes
     */
    public String getVerticalGroup() {
        if (isTechnical()) {
            return "Technical";
        } else if (isAdministrative()) {
            return "Administrative";
        } else {
            return "General";
        }
    }
    
    /**
     * Get compatible ticket categories for this vertical
     */
    public TicketCategory[] getCompatibleCategories() {
        switch (this) {
            case ELECTRICAL:
                return new TicketCategory[]{TicketCategory.ELECTRICAL_ISSUES};
            case PLUMBING:
                return new TicketCategory[]{TicketCategory.PLUMBING_WATER};
            case HVAC:
                return new TicketCategory[]{TicketCategory.HVAC};
            case CARPENTRY:
                return new TicketCategory[]{TicketCategory.FURNITURE_FIXTURES, TicketCategory.STRUCTURAL_CIVIL};
            case IT_SUPPORT:
                return new TicketCategory[]{TicketCategory.NETWORK_INTERNET, TicketCategory.COMPUTER_HARDWARE, TicketCategory.AUDIO_VISUAL_EQUIPMENT};
            case NETWORK_ADMIN:
                return new TicketCategory[]{TicketCategory.NETWORK_INTERNET, TicketCategory.SECURITY_SYSTEMS};
            case SECURITY_SYSTEMS:
                return new TicketCategory[]{TicketCategory.SECURITY_SYSTEMS, TicketCategory.SAFETY_SECURITY};
            case HOUSEKEEPING:
                return new TicketCategory[]{TicketCategory.HOUSEKEEPING_CLEANLINESS};
            case LANDSCAPING:
                return new TicketCategory[]{TicketCategory.LANDSCAPING_OUTDOOR};
            case GENERAL_MAINTENANCE:
                return new TicketCategory[]{TicketCategory.GENERAL, TicketCategory.STRUCTURAL_CIVIL};
            case HOSTEL_WARDEN:
            case BLOCK_SUPERVISOR:
            case ADMIN_STAFF:
                return new TicketCategory[]{TicketCategory.GENERAL};
            case SECURITY_OFFICER:
                return new TicketCategory[]{TicketCategory.SAFETY_SECURITY, TicketCategory.SECURITY_SYSTEMS};
            default:
                return new TicketCategory[]{TicketCategory.GENERAL};
        }
    }
    
    /**
     * Get the experience level multiplier for workload calculations
     */
    public double getExperienceMultiplier() {
        if (isTechnical()) {
            return 1.2; // Technical staff can handle more complex issues
        } else if (isAdministrative()) {
            return 1.5; // Administrative staff have broader scope
        } else {
            return 1.0; // Standard multiplier
        }
    }
    
    /**
     * Check if this vertical can work across multiple hostel blocks
     */
    public boolean canWorkAcrossBlocks() {
        switch (this) {
            case IT_SUPPORT:
            case NETWORK_ADMIN:
            case SECURITY_OFFICER:
            case ADMIN_STAFF:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get the priority level for emergency assignments (lower number = higher priority)
     */
    public int getEmergencyPriority() {
        switch (this) {
            case SECURITY_OFFICER:
                return 1;
            case HOSTEL_WARDEN:
                return 2;
            case ELECTRICAL:
            case PLUMBING:
                return 3;
            case BLOCK_SUPERVISOR:
                return 4;
            case IT_SUPPORT:
                return 5;
            default:
                return 10;
        }
    }
}