package com.hostel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing scheduled maintenance for assets
 */
@Entity
@Table(name = "maintenance_schedules")
public class MaintenanceSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @Column(nullable = false)
    private String maintenanceType;
    
    private String description;
    
    @Column(nullable = false)
    private Integer intervalDays;
    
    @Column(nullable = false)
    private LocalDateTime lastPerformed;
    
    @Column(nullable = false)
    private LocalDateTime nextDueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenancePriority priority;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician")
    private User assignedTechnician;
    
    private Double estimatedCost;
    
    private String instructions;
    
    private String requiredParts;
    
    private Integer estimatedDurationMinutes;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors
    public MaintenanceSchedule() {}
    
    public MaintenanceSchedule(Asset asset, String maintenanceType, Integer intervalDays) {
        this.asset = asset;
        this.maintenanceType = maintenanceType;
        this.intervalDays = intervalDays;
        this.status = MaintenanceStatus.SCHEDULED;
        this.priority = MaintenancePriority.MEDIUM;
        this.lastPerformed = LocalDateTime.now();
        this.nextDueDate = LocalDateTime.now().plusDays(intervalDays);
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Asset getAsset() {
        return asset;
    }
    
    public void setAsset(Asset asset) {
        this.asset = asset;
    }
    
    public String getMaintenanceType() {
        return maintenanceType;
    }
    
    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getIntervalDays() {
        return intervalDays;
    }
    
    public void setIntervalDays(Integer intervalDays) {
        this.intervalDays = intervalDays;
    }
    
    public LocalDateTime getLastPerformed() {
        return lastPerformed;
    }
    
    public void setLastPerformed(LocalDateTime lastPerformed) {
        this.lastPerformed = lastPerformed;
    }
    
    public LocalDateTime getNextDueDate() {
        return nextDueDate;
    }
    
    public void setNextDueDate(LocalDateTime nextDueDate) {
        this.nextDueDate = nextDueDate;
    }
    
    public MaintenanceStatus getStatus() {
        return status;
    }
    
    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }
    
    public MaintenancePriority getPriority() {
        return priority;
    }
    
    public void setPriority(MaintenancePriority priority) {
        this.priority = priority;
    }
    
    public User getAssignedTechnician() {
        return assignedTechnician;
    }
    
    public void setAssignedTechnician(User assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }
    
    public Double getEstimatedCost() {
        return estimatedCost;
    }
    
    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getRequiredParts() {
        return requiredParts;
    }
    
    public void setRequiredParts(String requiredParts) {
        this.requiredParts = requiredParts;
    }
    
    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }
    
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
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
    
    // Utility methods
    public boolean isOverdue() {
        return nextDueDate.isBefore(LocalDateTime.now()) && status == MaintenanceStatus.SCHEDULED;
    }
    
    public boolean isDueSoon() {
        return nextDueDate.isBefore(LocalDateTime.now().plusDays(7)) && status == MaintenanceStatus.SCHEDULED;
    }
    
    public void markCompleted() {
        this.status = MaintenanceStatus.COMPLETED;
        this.lastPerformed = LocalDateTime.now();
        this.nextDueDate = LocalDateTime.now().plusDays(intervalDays);
    }
    
    public void reschedule(LocalDateTime newDate) {
        this.nextDueDate = newDate;
        this.status = MaintenanceStatus.SCHEDULED;
    }
    
    public long getDaysUntilDue() {
        return java.time.Duration.between(LocalDateTime.now(), nextDueDate).toDays();
    }
}

/**
 * Enum for maintenance status
 */
enum MaintenanceStatus {
    SCHEDULED("Scheduled", "Maintenance is scheduled"),
    IN_PROGRESS("In Progress", "Maintenance is being performed"),
    COMPLETED("Completed", "Maintenance has been completed"),
    CANCELLED("Cancelled", "Maintenance has been cancelled"),
    OVERDUE("Overdue", "Maintenance is overdue");
    
    private final String displayName;
    private final String description;
    
    MaintenanceStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * Enum for maintenance priority
 */
enum MaintenancePriority {
    LOW("Low", "Non-urgent maintenance"),
    MEDIUM("Medium", "Standard maintenance"),
    HIGH("High", "Important maintenance"),
    CRITICAL("Critical", "Critical maintenance required");
    
    private final String displayName;
    private final String description;
    
    MaintenancePriority(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
