package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BuildingMaintenanceSchedule entity representing scheduled maintenance activities.
 * Tracks planned maintenance work for buildings and facilities.
 */
@Entity
@Table(name = "building_maintenance_schedule", indexes = {
    @Index(name = "idx_maintenance_building", columnList = "building"),
    @Index(name = "idx_maintenance_scheduled_date", columnList = "scheduled_date"),
    @Index(name = "idx_maintenance_status", columnList = "status"),
    @Index(name = "idx_maintenance_assigned_staff", columnList = "assigned_staff")
})
public class BuildingMaintenanceSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Building is required")
    @Column(nullable = false, length = 50)
    private String building;
    
    @NotBlank(message = "Maintenance type is required")
    @Column(name = "maintenance_type", nullable = false, length = 100)
    private String maintenanceType;
    
    @NotNull(message = "Scheduled date is required")
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;
    
    @Positive(message = "Estimated duration must be positive")
    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff")
    private User assignedStaff;
    
    @Column(length = 20)
    private String status = "SCHEDULED";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public BuildingMaintenanceSchedule() {}
    
    public BuildingMaintenanceSchedule(String building, String maintenanceType, LocalDate scheduledDate) {
        this.building = building;
        this.maintenanceType = maintenanceType;
        this.scheduledDate = scheduledDate;
    }
    
    public BuildingMaintenanceSchedule(String building, String maintenanceType, LocalDate scheduledDate, 
                                     Integer estimatedDurationHours, String description) {
        this.building = building;
        this.maintenanceType = maintenanceType;
        this.scheduledDate = scheduledDate;
        this.estimatedDurationHours = estimatedDurationHours;
        this.description = description;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getBuilding() {
        return building;
    }
    
    public void setBuilding(String building) {
        this.building = building;
    }
    
    public String getMaintenanceType() {
        return maintenanceType;
    }
    
    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }
    
    public LocalDate getScheduledDate() {
        return scheduledDate;
    }
    
    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }
    
    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public User getAssignedStaff() {
        return assignedStaff;
    }
    
    public void setAssignedStaff(User assignedStaff) {
        this.assignedStaff = assignedStaff;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public boolean isScheduled() {
        return "SCHEDULED".equals(status);
    }
    
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }
    
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }
    
    public boolean isOverdue() {
        if (isCompleted() || isCancelled()) return false;
        return scheduledDate.isBefore(LocalDate.now());
    }
    
    public boolean isToday() {
        return scheduledDate.equals(LocalDate.now());
    }
    
    public boolean isUpcoming() {
        return scheduledDate.isAfter(LocalDate.now());
    }
    
    public boolean isPast() {
        return scheduledDate.isBefore(LocalDate.now());
    }
    
    public long getDaysUntilScheduled() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
    }
    
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(scheduledDate, LocalDate.now());
    }
    
    public String getAssignedStaffDisplayName() {
        return assignedStaff != null ? assignedStaff.getFullName() : "Unassigned";
    }
    
    public String getAssignedStaffRole() {
        return assignedStaff != null ? assignedStaff.getRole().getDisplayName() : "Unassigned";
    }
    
    public String getFormattedScheduledDate() {
        if (scheduledDate == null) return "Unknown";
        return scheduledDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
    
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Unknown";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    public String getDurationFormatted() {
        if (estimatedDurationHours == null) return "Not specified";
        
        if (estimatedDurationHours < 24) {
            return estimatedDurationHours + " hour" + (estimatedDurationHours != 1 ? "s" : "");
        } else {
            int days = estimatedDurationHours / 24;
            int hours = estimatedDurationHours % 24;
            if (hours == 0) {
                return days + " day" + (days != 1 ? "s" : "");
            } else {
                return days + " day" + (days != 1 ? "s" : "") + " " + hours + " hour" + (hours != 1 ? "s" : "");
            }
        }
    }
    
    public boolean canBeAssignedTo(User user) {
        if (user == null) return false;
        
        // Admin can assign to anyone
        if (user.isAdmin()) return true;
        
        // Staff can be assigned maintenance tasks
        return user.isStaff();
    }
    
    public boolean canBeViewedBy(User user) {
        if (user == null) return false;
        
        // Admin can view all schedules
        if (user.isAdmin()) return true;
        
        // Staff can view schedules they're assigned to or for their building
        if (user.isStaff()) {
            return (assignedStaff != null && assignedStaff.getId().equals(user.getId())) ||
                   (user.getHostelBlock() != null && user.getHostelBlock().equals(building));
        }
        
        // Students can view schedules for their building
        return user.getHostelBlock() != null && user.getHostelBlock().equals(building);
    }
    
    public boolean canBeModifiedBy(User user) {
        if (user == null) return false;
        
        // Admin can modify all schedules
        if (user.isAdmin()) return true;
        
        // Staff can modify schedules they're assigned to
        return assignedStaff != null && assignedStaff.getId().equals(user.getId());
    }
    
    @Override
    public String toString() {
        return "BuildingMaintenanceSchedule{" +
                "id=" + id +
                ", building='" + building + '\'' +
                ", maintenanceType='" + maintenanceType + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", estimatedDurationHours=" + estimatedDurationHours +
                ", status='" + status + '\'' +
                ", assignedStaff=" + (assignedStaff != null ? assignedStaff.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildingMaintenanceSchedule that = (BuildingMaintenanceSchedule) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 