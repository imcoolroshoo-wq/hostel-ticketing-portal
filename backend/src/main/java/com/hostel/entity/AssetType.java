package com.hostel.entity;

/**
 * Enum representing different types of assets in the hostel
 */
public enum AssetType {
    
    // Furniture
    FURNITURE_BED("Bed", "Sleeping furniture", "🛏️"),
    FURNITURE_DESK("Desk", "Study/work furniture", "🪑"),
    FURNITURE_CHAIR("Chair", "Seating furniture", "🪑"),
    FURNITURE_WARDROBE("Wardrobe", "Storage furniture", "🚪"),
    FURNITURE_SHELF("Shelf", "Storage/display furniture", "📚"),
    
    // Appliances
    APPLIANCE_AC("Air Conditioner", "Climate control appliance", "❄️"),
    APPLIANCE_FAN("Fan", "Cooling appliance", "🌀"),
    APPLIANCE_REFRIGERATOR("Refrigerator", "Food storage appliance", "🧊"),
    APPLIANCE_MICROWAVE("Microwave", "Cooking appliance", "📱"),
    APPLIANCE_WASHING_MACHINE("Washing Machine", "Laundry appliance", "🧺"),
    APPLIANCE_WATER_HEATER("Water Heater", "Hot water appliance", "🔥"),
    
    // Electronics
    ELECTRONICS_TV("Television", "Entertainment device", "📺"),
    ELECTRONICS_COMPUTER("Computer", "Computing device", "💻"),
    ELECTRONICS_ROUTER("Router", "Network device", "📡"),
    ELECTRONICS_PHONE("Phone", "Communication device", "📞"),
    
    // Safety & Security
    SAFETY_FIRE_EXTINGUISHER("Fire Extinguisher", "Fire safety equipment", "🧯"),
    SAFETY_SMOKE_DETECTOR("Smoke Detector", "Fire detection device", "🚨"),
    SAFETY_CCTV("CCTV Camera", "Security monitoring device", "📹"),
    SAFETY_ACCESS_CONTROL("Access Control", "Security access device", "🔐"),
    
    // Maintenance Equipment
    MAINTENANCE_TOOLS("Tools", "Maintenance equipment", "🔧"),
    MAINTENANCE_CLEANING("Cleaning Equipment", "Cleaning supplies", "🧹"),
    MAINTENANCE_HVAC("HVAC Equipment", "Heating/cooling systems", "🌡️"),
    MAINTENANCE_PLUMBING("Plumbing Equipment", "Water/drainage systems", "🚿"),
    MAINTENANCE_ELECTRICAL("Electrical Equipment", "Power/lighting systems", "⚡"),
    
    // Recreation
    RECREATION_SPORTS("Sports Equipment", "Recreation/fitness equipment", "⚽"),
    RECREATION_GAMES("Games", "Entertainment/gaming equipment", "🎮"),
    RECREATION_BOOKS("Books", "Educational/entertainment materials", "📖"),
    
    // Kitchen & Dining
    KITCHEN_STOVE("Stove", "Cooking equipment", "🔥"),
    KITCHEN_SINK("Sink", "Washing facility", "🚿"),
    KITCHEN_UTENSILS("Utensils", "Cooking/eating tools", "🍴"),
    KITCHEN_APPLIANCES("Kitchen Appliances", "Food preparation equipment", "🍳"),
    
    // Other
    OTHER("Other", "Miscellaneous asset", "📦");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    AssetType(String displayName, String description, String icon) {
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
     * Get the category of this asset type
     */
    public String getCategory() {
        String name = this.name();
        if (name.startsWith("FURNITURE_")) return "Furniture";
        if (name.startsWith("APPLIANCE_")) return "Appliances";
        if (name.startsWith("ELECTRONICS_")) return "Electronics";
        if (name.startsWith("SAFETY_")) return "Safety & Security";
        if (name.startsWith("MAINTENANCE_")) return "Maintenance";
        if (name.startsWith("RECREATION_")) return "Recreation";
        if (name.startsWith("KITCHEN_")) return "Kitchen & Dining";
        return "Other";
    }
    
    /**
     * Check if this asset type requires regular maintenance
     */
    public boolean requiresRegularMaintenance() {
        return this == APPLIANCE_AC || 
               this == APPLIANCE_REFRIGERATOR ||
               this == APPLIANCE_WASHING_MACHINE ||
               this == APPLIANCE_WATER_HEATER ||
               this == MAINTENANCE_HVAC ||
               this == MAINTENANCE_PLUMBING ||
               this == MAINTENANCE_ELECTRICAL ||
               this == SAFETY_FIRE_EXTINGUISHER ||
               this == SAFETY_SMOKE_DETECTOR;
    }
    
    /**
     * Get the default maintenance interval in days
     */
    public int getDefaultMaintenanceIntervalDays() {
        switch (this) {
            case APPLIANCE_AC:
            case MAINTENANCE_HVAC:
                return 90; // 3 months
            case APPLIANCE_REFRIGERATOR:
            case APPLIANCE_WASHING_MACHINE:
                return 180; // 6 months
            case SAFETY_FIRE_EXTINGUISHER:
            case SAFETY_SMOKE_DETECTOR:
                return 365; // 1 year
            case MAINTENANCE_PLUMBING:
            case MAINTENANCE_ELECTRICAL:
                return 180; // 6 months
            default:
                return 365; // 1 year default
        }
    }
    
    /**
     * Check if this asset type is movable
     */
    public boolean isMovable() {
        return this != MAINTENANCE_HVAC &&
               this != MAINTENANCE_PLUMBING &&
               this != MAINTENANCE_ELECTRICAL &&
               this != SAFETY_SMOKE_DETECTOR &&
               this != KITCHEN_SINK &&
               this != KITCHEN_STOVE;
    }
}
