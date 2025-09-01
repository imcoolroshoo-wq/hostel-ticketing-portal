package com.hostel.entity;

/**
 * Comprehensive ticket categories based on IIM Trichy Hostel Ticket Management System
 * Product Design Document. Categories are organized by functional areas and expertise requirements.
 */
public enum TicketCategory {
    
    // =====================================================
    // INFRASTRUCTURE CATEGORIES
    // =====================================================
    
    /**
     * Electrical Issues - Power-related problems, electrical fixtures, and safety concerns
     */
    ELECTRICAL_ISSUES("Electrical Issues", 
        "Power outages, faulty outlets, lighting issues, electrical appliances, circuit breakers", 
        "⚡", true, 4),
    
    /**
     * Plumbing & Water - Water supply, drainage, and sanitation issues
     */
    PLUMBING_WATER("Plumbing & Water", 
        "Leaking pipes, blocked drains, water pressure issues, toilet/bathroom fixtures, water heaters", 
        "🚰", true, 3),
    
    /**
     * HVAC - Heating, Ventilation, Air Conditioning
     */
    HVAC("HVAC", 
        "AC not working, heating issues, ventilation problems, air quality concerns", 
        "❄️", true, 6),
    
    /**
     * Structural & Civil - Building structure, walls, doors, windows, and civil works
     */
    STRUCTURAL_CIVIL("Structural & Civil", 
        "Broken doors/windows, wall cracks, ceiling issues, flooring problems, structural damage", 
        "🏗️", true, 24),
    
    /**
     * Furniture & Fixtures - Room furniture, fixtures, and interior fittings
     */
    FURNITURE_FIXTURES("Furniture & Fixtures", 
        "Broken beds/chairs, damaged wardrobes, loose fittings, furniture assembly", 
        "🪑", false, 4),
    
    // =====================================================
    // IT & TECHNOLOGY CATEGORIES
    // =====================================================
    
    /**
     * Network & Internet - Internet connectivity, WiFi, and network infrastructure issues
     */
    NETWORK_INTERNET("Network & Internet", 
        "No internet, slow connection, WiFi not working, network cable issues, router problems", 
        "🌐", true, 2),
    
    /**
     * Computer & Hardware - Desktop computers, laptops, and hardware peripherals
     */
    COMPUTER_HARDWARE("Computer & Hardware", 
        "Computer not starting, hardware failure, peripheral issues, software installation", 
        "💻", true, 4),
    
    /**
     * Audio/Visual Equipment - Projectors, speakers, display systems, and AV equipment
     */
    AUDIO_VISUAL_EQUIPMENT("Audio/Visual Equipment", 
        "Projector not working, audio issues, display problems, cable connections", 
        "📺", true, 3),
    
    /**
     * Security Systems - CCTV, access control, and security technology
     */
    SECURITY_SYSTEMS("Security Systems", 
        "CCTV not working, access card issues, security system failures, camera problems", 
        "📹", true, 2),
    
    // =====================================================
    // GENERAL MAINTENANCE CATEGORIES
    // =====================================================
    
    /**
     * Housekeeping & Cleanliness - Cleaning, sanitation, and general housekeeping issues
     */
    HOUSEKEEPING_CLEANLINESS("Housekeeping & Cleanliness", 
        "Deep cleaning requests, pest control, garbage disposal, common area maintenance", 
        "🧽", false, 4),
    
    /**
     * Safety & Security - Physical safety, security concerns, and emergency situations
     */
    SAFETY_SECURITY("Safety & Security", 
        "Broken locks, safety hazards, emergency lighting, fire safety equipment", 
        "🔒", true, 1),
    
    /**
     * Landscaping & Outdoor - Garden maintenance, outdoor facilities, and external areas
     */
    LANDSCAPING_OUTDOOR("Landscaping & Outdoor", 
        "Garden maintenance, outdoor lighting, pathway issues, external building maintenance", 
        "🌳", false, 8),
    
    /**
     * General - Miscellaneous issues that don't fit other categories
     */
    GENERAL("General", 
        "General maintenance and other issues", 
        "🔧", false, 12);
    
    private final String displayName;
    private final String description;
    private final String icon;
    private final boolean requiresSpecializedStaff;
    private final int estimatedResolutionHours;
    
    TicketCategory(String displayName, String description, String icon, 
                   boolean requiresSpecializedStaff, int estimatedResolutionHours) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.requiresSpecializedStaff = requiresSpecializedStaff;
        this.estimatedResolutionHours = estimatedResolutionHours;
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
    
    public boolean requiresSpecializedStaff() {
        return requiresSpecializedStaff;
    }
    
    public int getEstimatedResolutionHours() {
        return estimatedResolutionHours;
    }
    
    /**
     * Check if this category requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == SAFETY_SECURITY || this == ELECTRICAL_ISSUES || this == NETWORK_INTERNET;
    }
    
    /**
     * Check if this category can be handled by general staff
     */
    public boolean canBeHandledByGeneralStaff() {
        return !requiresSpecializedStaff;
    }
    
    /**
     * Get the default priority for this category
     */
    public TicketPriority getDefaultPriority() {
        switch (this) {
            case SAFETY_SECURITY:
                return TicketPriority.EMERGENCY;
            case ELECTRICAL_ISSUES:
            case PLUMBING_WATER:
            case NETWORK_INTERNET:
            case SECURITY_SYSTEMS:
                return TicketPriority.HIGH;
            case HVAC:
            case STRUCTURAL_CIVIL:
            case COMPUTER_HARDWARE:
            case AUDIO_VISUAL_EQUIPMENT:
                return TicketPriority.MEDIUM;
            case FURNITURE_FIXTURES:
            case HOUSEKEEPING_CLEANLINESS:
            case LANDSCAPING_OUTDOOR:
            case GENERAL:
                return TicketPriority.LOW;
            default:
                return TicketPriority.MEDIUM;
        }
    }
    
    /**
     * Get the category group for organizational purposes
     */
    public String getCategoryGroup() {
        switch (this) {
            case ELECTRICAL_ISSUES:
            case PLUMBING_WATER:
            case HVAC:
            case STRUCTURAL_CIVIL:
            case FURNITURE_FIXTURES:
                return "Infrastructure";
            case NETWORK_INTERNET:
            case COMPUTER_HARDWARE:
            case AUDIO_VISUAL_EQUIPMENT:
            case SECURITY_SYSTEMS:
                return "IT & Technology";
            case HOUSEKEEPING_CLEANLINESS:
            case SAFETY_SECURITY:
            case LANDSCAPING_OUTDOOR:
            case GENERAL:
                return "General Maintenance";
            default:
                return "Other";
        }
    }
    
    /**
     * Get the required staff vertical for this category
     */
    public String getRequiredStaffVertical() {
        switch (this) {
            case ELECTRICAL_ISSUES:
                return "ELECTRICAL";
            case PLUMBING_WATER:
                return "PLUMBING";
            case HVAC:
                return "HVAC";
            case STRUCTURAL_CIVIL:
                return "GENERAL_MAINTENANCE";
            case FURNITURE_FIXTURES:
                return "CARPENTRY";
            case NETWORK_INTERNET:
            case COMPUTER_HARDWARE:
                return "IT_SUPPORT";
            case AUDIO_VISUAL_EQUIPMENT:
                return "IT_SUPPORT";
            case SECURITY_SYSTEMS:
                return "SECURITY_SYSTEMS";
            case HOUSEKEEPING_CLEANLINESS:
                return "HOUSEKEEPING";
            case SAFETY_SECURITY:
                return "SECURITY_OFFICER";
            case LANDSCAPING_OUTDOOR:
                return "LANDSCAPING";
            case GENERAL:
                return "GENERAL_MAINTENANCE";
            default:
                return "GENERAL_MAINTENANCE";
        }
    }
    
    /**
     * Get estimated resolution time based on priority
     */
    public int getEstimatedResolutionHours(TicketPriority priority) {
        int baseHours = this.estimatedResolutionHours;
        
        switch (priority) {
            case EMERGENCY:
                return Math.max(1, baseHours / 4); // Quarter time for emergency
            case HIGH:
                return Math.max(2, baseHours / 2); // Half time for high priority
            case MEDIUM:
                return baseHours; // Standard time
            case LOW:
                return baseHours * 2; // Double time for low priority
            default:
                return baseHours;
        }
    }
    
    /**
     * Check if this category supports emergency priority
     */
    public boolean supportsEmergencyPriority() {
        return this == SAFETY_SECURITY || this == ELECTRICAL_ISSUES || 
               this == PLUMBING_WATER || this == SECURITY_SYSTEMS;
    }
}