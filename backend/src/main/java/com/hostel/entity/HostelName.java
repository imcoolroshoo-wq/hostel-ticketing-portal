package com.hostel.entity;

/**
 * Enum representing hostel names/numbers for IIM Trichy
 * This replaces the complex HostelBlock entity for simpler management
 */
public enum HostelName {
    
    BLOCK_A("Block A", "BLK-A", "Hostel Block A", false),
    BLOCK_B("Block B", "BLK-B", "Hostel Block B", false),
    BLOCK_C("Block C", "BLK-C", "Hostel Block C", false),
    BLOCK_D("Block D", "BLK-D", "Hostel Block D", false),
    BLOCK_E("Block E", "BLK-E", "Hostel Block E", false),
    BLOCK_F("Block F", "BLK-F", "Hostel Block F", false),
    BLOCK_G("Block G", "BLK-G", "Hostel Block G", true),  // Female block
    BLOCK_H("Block H", "BLK-H", "Hostel Block H", true);  // Female block
    
    private final String displayName;
    private final String code;
    private final String fullName;
    private final boolean isFemaleBlock;
    
    HostelName(String displayName, String code, String fullName, boolean isFemaleBlock) {
        this.displayName = displayName;
        this.code = code;
        this.fullName = fullName;
        this.isFemaleBlock = isFemaleBlock;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public boolean isFemaleBlock() {
        return isFemaleBlock;
    }
    
    public boolean isMaleBlock() {
        return !isFemaleBlock;
    }
    
    /**
     * Get hostel by display name
     */
    public static HostelName fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return null;
        }
        for (HostelName hostel : values()) {
            if (hostel.displayName.equals(displayName)) {
                return hostel;
            }
        }
        throw new IllegalArgumentException("Unknown hostel: " + displayName);
    }
    
    /**
     * Get hostel by full name (for backward compatibility)
     */
    public static HostelName fromFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        for (HostelName hostel : values()) {
            if (hostel.fullName.equals(fullName)) {
                return hostel;
            }
        }
        throw new IllegalArgumentException("Unknown hostel full name: " + fullName);
    }
    
    /**
     * Smart lookup that tries multiple matching strategies
     */
    public static HostelName fromAnyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // Try exact enum name match first
        try {
            return HostelName.valueOf(name.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            // Continue to other methods
        }
        
        // Try display name match
        try {
            return fromDisplayName(name);
        } catch (IllegalArgumentException e) {
            // Continue to other methods
        }
        
        // Try full name match
        try {
            return fromFullName(name);
        } catch (IllegalArgumentException e) {
            // Continue to other methods
        }
        
        // Try code match
        try {
            return fromCode(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown hostel name: " + name);
        }
    }
    
    /**
     * Get hostel by code
     */
    public static HostelName fromCode(String code) {
        for (HostelName hostel : values()) {
            if (hostel.code.equals(code)) {
                return hostel;
            }
        }
        throw new IllegalArgumentException("Unknown hostel code: " + code);
    }
    
    /**
     * Get all male hostels
     */
    public static HostelName[] getMaleHostels() {
        return java.util.Arrays.stream(values())
                .filter(HostelName::isMaleBlock)
                .toArray(HostelName[]::new);
    }
    
    /**
     * Get all female hostels
     */
    public static HostelName[] getFemaleHostels() {
        return java.util.Arrays.stream(values())
                .filter(HostelName::isFemaleBlock)
                .toArray(HostelName[]::new);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
