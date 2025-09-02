package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing ticket templates for recurring issues
 * Implements Template System as per PDD Section 4.1.4
 */
@Entity
@Table(name = "ticket_templates", indexes = {
    @Index(name = "idx_ticket_templates_name", columnList = "name"),
    @Index(name = "idx_ticket_templates_category", columnList = "category"),
    @Index(name = "idx_ticket_templates_is_active", columnList = "is_active"),
    @Index(name = "idx_ticket_templates_hostel_block", columnList = "hostel_block")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TicketTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Template name is required")
    @Size(min = 5, max = 100, message = "Template name must be between 5 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;
    
    @NotBlank(message = "Title template is required")
    @Size(min = 10, max = 200, message = "Title template must be between 10 and 200 characters")
    @Column(name = "title_template", nullable = false, length = 200)
    private String titleTemplate;
    
    @NotBlank(message = "Description template is required")
    @Size(min = 20, message = "Description template must be at least 20 characters")
    @Column(name = "description_template", nullable = false, columnDefinition = "TEXT")
    private String descriptionTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "default_priority", nullable = false)
    private TicketPriority defaultPriority = TicketPriority.MEDIUM;
    
    @Convert(converter = HostelNameConverter.class)
    @Column(name = "hostel_block", length = 50)
    private HostelName hostelBlock; // NULL means applies to all hostels
    
    @Column(name = "room_number_required")
    private Boolean roomNumberRequired = false;
    
    @Column(name = "location_details_required")
    private Boolean locationDetailsRequired = false;
    
    @Column(name = "photo_required")
    private Boolean photoRequired = false;
    
    @Column(name = "estimated_resolution_hours")
    private Integer estimatedResolutionHours;
    
    @Column(name = "common_solutions", columnDefinition = "TEXT")
    private String commonSolutions;
    
    @Column(name = "troubleshooting_steps", columnDefinition = "TEXT")
    private String troubleshootingSteps;
    
    @Column(name = "required_materials", columnDefinition = "TEXT")
    private String requiredMaterials;
    
    @Column(name = "safety_notes", columnDefinition = "TEXT")
    private String safetyNotes;
    
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // Constructors
    public TicketTemplate() {}
    
    public TicketTemplate(String name, String titleTemplate, String descriptionTemplate, 
                         TicketCategory category, TicketPriority defaultPriority) {
        this.name = name;
        this.titleTemplate = titleTemplate;
        this.descriptionTemplate = descriptionTemplate;
        this.category = category;
        this.defaultPriority = defaultPriority;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTitleTemplate() {
        return titleTemplate;
    }
    
    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }
    
    public String getDescriptionTemplate() {
        return descriptionTemplate;
    }
    
    public void setDescriptionTemplate(String descriptionTemplate) {
        this.descriptionTemplate = descriptionTemplate;
    }
    
    public TicketCategory getCategory() {
        return category;
    }
    
    public void setCategory(TicketCategory category) {
        this.category = category;
    }
    
    public TicketPriority getDefaultPriority() {
        return defaultPriority;
    }
    
    public void setDefaultPriority(TicketPriority defaultPriority) {
        this.defaultPriority = defaultPriority;
    }
    
    public HostelName getHostelBlock() {
        return hostelBlock;
    }
    
    public void setHostelBlock(HostelName hostelBlock) {
        this.hostelBlock = hostelBlock;
    }
    
    public Boolean getRoomNumberRequired() {
        return roomNumberRequired;
    }
    
    public void setRoomNumberRequired(Boolean roomNumberRequired) {
        this.roomNumberRequired = roomNumberRequired;
    }
    
    public Boolean getLocationDetailsRequired() {
        return locationDetailsRequired;
    }
    
    public void setLocationDetailsRequired(Boolean locationDetailsRequired) {
        this.locationDetailsRequired = locationDetailsRequired;
    }
    
    public Boolean getPhotoRequired() {
        return photoRequired;
    }
    
    public void setPhotoRequired(Boolean photoRequired) {
        this.photoRequired = photoRequired;
    }
    
    public Integer getEstimatedResolutionHours() {
        return estimatedResolutionHours;
    }
    
    public void setEstimatedResolutionHours(Integer estimatedResolutionHours) {
        this.estimatedResolutionHours = estimatedResolutionHours;
    }
    
    public String getCommonSolutions() {
        return commonSolutions;
    }
    
    public void setCommonSolutions(String commonSolutions) {
        this.commonSolutions = commonSolutions;
    }
    
    public String getTroubleshootingSteps() {
        return troubleshootingSteps;
    }
    
    public void setTroubleshootingSteps(String troubleshootingSteps) {
        this.troubleshootingSteps = troubleshootingSteps;
    }
    
    public String getRequiredMaterials() {
        return requiredMaterials;
    }
    
    public void setRequiredMaterials(String requiredMaterials) {
        this.requiredMaterials = requiredMaterials;
    }
    
    public String getSafetyNotes() {
        return safetyNotes;
    }
    
    public void setSafetyNotes(String safetyNotes) {
        this.safetyNotes = safetyNotes;
    }
    
    public Integer getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    // Utility methods
    public void incrementUsageCount() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
    
    public boolean isApplicableToHostel(HostelName targetHostel) {
        return this.hostelBlock == null || this.hostelBlock.equals(targetHostel);
    }
    
    /**
     * Generate ticket title from template with variable substitution
     */
    public String generateTitle(String roomNumber, String hostelName, String additionalInfo) {
        String title = this.titleTemplate;
        
        if (roomNumber != null) {
            title = title.replace("{ROOM_NUMBER}", roomNumber);
        }
        if (hostelName != null) {
            title = title.replace("{HOSTEL_BLOCK}", hostelName);
        }
        if (additionalInfo != null) {
            title = title.replace("{ADDITIONAL_INFO}", additionalInfo);
        }
        
        // Clean up any remaining placeholders
        title = title.replaceAll("\\{[^}]+\\}", "");
        
        return title.trim();
    }
    
    /**
     * Generate ticket description from template with variable substitution
     */
    public String generateDescription(String roomNumber, String hostelName, 
                                    String additionalInfo, String specificDetails) {
        String description = this.descriptionTemplate;
        
        if (roomNumber != null) {
            description = description.replace("{ROOM_NUMBER}", roomNumber);
        }
        if (hostelName != null) {
            description = description.replace("{HOSTEL_BLOCK}", hostelName);
        }
        if (additionalInfo != null) {
            description = description.replace("{ADDITIONAL_INFO}", additionalInfo);
        }
        if (specificDetails != null) {
            description = description.replace("{SPECIFIC_DETAILS}", specificDetails);
        }
        
        // Add troubleshooting steps if available
        if (troubleshootingSteps != null && !troubleshootingSteps.trim().isEmpty()) {
            description += "\n\nTroubleshooting Steps:\n" + troubleshootingSteps;
        }
        
        // Add safety notes if available
        if (safetyNotes != null && !safetyNotes.trim().isEmpty()) {
            description += "\n\nSafety Notes:\n" + safetyNotes;
        }
        
        // Clean up any remaining placeholders
        description = description.replaceAll("\\{[^}]+\\}", "");
        
        return description.trim();
    }
    
    @Override
    public String toString() {
        return "TicketTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", defaultPriority=" + defaultPriority +
                ", hostelBlock=" + hostelBlock +
                ", usageCount=" + usageCount +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketTemplate that = (TicketTemplate) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
