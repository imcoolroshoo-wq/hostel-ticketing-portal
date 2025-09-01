package com.hostel.entity;

/**
 * Enum representing different types of attachments in the hostel ticketing system
 * Supports photo documentation requirements as per Product Design Document
 */
public enum AttachmentType {
    
    /**
     * General attachment - any file type
     */
    GENERAL("General", "General file attachment", "📎"),
    
    /**
     * Problem documentation photo - shows the issue
     */
    PROBLEM_PHOTO("Problem Photo", "Photo documenting the reported issue", "📷"),
    
    /**
     * Before work photo - taken before starting work
     */
    BEFORE_WORK_PHOTO("Before Work Photo", "Photo taken before maintenance work begins", "📸"),
    
    /**
     * Before photo - alias for before work photo (for compatibility)
     */
    BEFORE_PHOTO("Before Photo", "Photo taken before maintenance work begins", "📸"),
    
    /**
     * After work photo - taken after completing work
     */
    AFTER_WORK_PHOTO("After Work Photo", "Photo taken after maintenance work is completed", "✅"),
    
    /**
     * After photo - alias for after work photo (for compatibility)
     */
    AFTER_PHOTO("After Photo", "Photo taken after maintenance work is completed", "✅"),
    
    /**
     * Progress photo - showing work in progress
     */
    PROGRESS_PHOTO("Progress Photo", "Photo showing work in progress", "🔄"),
    
    /**
     * Evidence photo - supporting evidence for the issue
     */
    EVIDENCE_PHOTO("Evidence Photo", "Photo providing evidence related to the issue", "🔍"),
    
    /**
     * Solution photo - shows the completed solution
     */
    SOLUTION_PHOTO("Solution Photo", "Photo showing the implemented solution", "🛠️"),
    
    /**
     * Document - text or PDF documents
     */
    DOCUMENT("Document", "Document file (PDF, Word, etc.)", "📄"),
    
    /**
     * Video documentation
     */
    VIDEO("Video", "Video file for documentation", "🎥"),
    
    /**
     * Audio recording
     */
    AUDIO("Audio", "Audio recording", "🎵"),
    
    /**
     * Technical diagram or drawing
     */
    DIAGRAM("Diagram", "Technical diagram or drawing", "📐"),
    
    /**
     * Receipt or invoice
     */
    RECEIPT("Receipt", "Receipt or invoice for parts/services", "🧾");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    AttachmentType(String displayName, String description, String icon) {
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
     * Check if this attachment type is a photo
     */
    public boolean isPhoto() {
        return this == PROBLEM_PHOTO || this == BEFORE_WORK_PHOTO || 
               this == AFTER_WORK_PHOTO || this == PROGRESS_PHOTO ||
               this == EVIDENCE_PHOTO || this == SOLUTION_PHOTO;
    }
    
    /**
     * Check if this attachment type is required for certain work types
     */
    public boolean isRequiredForWorkType(TicketCategory category) {
        switch (this) {
            case BEFORE_WORK_PHOTO:
            case AFTER_WORK_PHOTO:
                // Required for certain types of work
                return category == TicketCategory.ELECTRICAL_ISSUES ||
                       category == TicketCategory.PLUMBING_WATER ||
                       category == TicketCategory.HVAC ||
                       category == TicketCategory.STRUCTURAL_CIVIL ||
                       category == TicketCategory.FURNITURE_FIXTURES;
            case PROBLEM_PHOTO:
                // Recommended for all visible issues
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Get the priority level for this attachment type
     */
    public int getPriorityLevel() {
        switch (this) {
            case BEFORE_WORK_PHOTO:
            case AFTER_WORK_PHOTO:
                return 1; // Highest priority for quality assurance
            case PROBLEM_PHOTO:
            case EVIDENCE_PHOTO:
                return 2; // High priority for documentation
            case SOLUTION_PHOTO:
            case PROGRESS_PHOTO:
                return 3; // Medium priority
            case DOCUMENT:
            case RECEIPT:
                return 4; // Low-medium priority
            default:
                return 5; // Lowest priority
        }
    }
    
    /**
     * Check if this attachment type supports metadata
     */
    public boolean supportsMetadata() {
        return isPhoto() || this == VIDEO;
    }
    
    /**
     * Get allowed file extensions for this attachment type
     */
    public String[] getAllowedExtensions() {
        switch (this) {
            case PROBLEM_PHOTO:
            case BEFORE_WORK_PHOTO:
            case AFTER_WORK_PHOTO:
            case PROGRESS_PHOTO:
            case EVIDENCE_PHOTO:
            case SOLUTION_PHOTO:
                return new String[]{"jpg", "jpeg", "png", "webp", "heic"};
            case VIDEO:
                return new String[]{"mp4", "mov", "avi", "mkv", "webm"};
            case AUDIO:
                return new String[]{"mp3", "wav", "m4a", "ogg"};
            case DOCUMENT:
                return new String[]{"pdf", "doc", "docx", "txt", "rtf"};
            case DIAGRAM:
                return new String[]{"jpg", "jpeg", "png", "pdf", "svg", "dwg"};
            case RECEIPT:
                return new String[]{"jpg", "jpeg", "png", "pdf"};
            default:
                return new String[]{"jpg", "jpeg", "png", "pdf", "doc", "docx", "txt"};
        }
    }
    
    /**
     * Get maximum file size in bytes for this attachment type
     */
    public long getMaxFileSizeBytes() {
        switch (this) {
            case VIDEO:
                return 100 * 1024 * 1024; // 100MB for videos
            case PROBLEM_PHOTO:
            case BEFORE_WORK_PHOTO:
            case AFTER_WORK_PHOTO:
            case PROGRESS_PHOTO:
            case EVIDENCE_PHOTO:
            case SOLUTION_PHOTO:
                return 10 * 1024 * 1024; // 10MB for photos
            case AUDIO:
                return 25 * 1024 * 1024; // 25MB for audio
            case DOCUMENT:
            case DIAGRAM:
            case RECEIPT:
                return 5 * 1024 * 1024; // 5MB for documents
            default:
                return 10 * 1024 * 1024; // 10MB default
        }
    }
}
