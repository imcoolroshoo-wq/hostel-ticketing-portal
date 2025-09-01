package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Ticket entity representing issues reported in the hostel ticketing system.
 * Contains all information about a reported problem and its resolution status.
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_category_enum", columnList = "category_enum"),
    @Index(name = "idx_tickets_created_by", columnList = "created_by"),
    @Index(name = "idx_tickets_assigned_to", columnList = "assigned_to"),
    @Index(name = "idx_tickets_created_at", columnList = "created_at"),
    @Index(name = "idx_tickets_hostel_block", columnList = "hostel_block"),
    @Index(name = "idx_tickets_room_number", columnList = "room_number")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // @NotBlank(message = "Ticket number is required")
    @Column(name = "ticket_number", unique = true, nullable = false, length = 20)
    private String ticketNumber;
    
    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be between 10 and 200 characters")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category_enum", columnDefinition = "varchar")
    private TicketCategory category;
    
    @Column(name = "custom_category")
    private String customCategory;
    
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar")
    private TicketPriority priority = TicketPriority.MEDIUM;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar")
    private TicketStatus status = TicketStatus.OPEN;
    
    @NotNull(message = "Created by is required")
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    @Size(max = 10, message = "Room number must not exceed 10 characters")
    @Column(name = "room_number", length = 10)
    private String roomNumber;
    
    @NotBlank(message = "Hostel block is required")
    @Size(max = 50, message = "Hostel block name must not exceed 50 characters")
    @Column(name = "hostel_block", length = 50, nullable = false)
    @JsonProperty("building")
    private String hostelBlock;
    
    @Column(name = "floor_number")
    private Integer floorNumber;
    
    @Size(max = 500, message = "Location details must not exceed 500 characters")
    @Column(name = "location_details", columnDefinition = "TEXT")
    private String locationDetails;
    
    @Column(name = "estimated_resolution_time")
    private LocalDateTime estimatedResolutionTime;
    
    @Column(name = "actual_resolution_time")
    private LocalDateTime actualResolutionTime;
    
    @Column(name = "sla_breach_time")
    private LocalDateTime slaBreachTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    // Additional metadata
    @Column(name = "is_emergency")
    private Boolean isEmergency = false;
    
    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_ticket_id")
    private Ticket parentTicket;
    
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private java.math.BigDecimal estimatedCost;
    
    @Column(name = "actual_cost", precision = 10, scale = 2)
    private java.math.BigDecimal actualCost;
    
    // Satisfaction and feedback
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketComment> comments = new ArrayList<>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketAttachment> attachments = new ArrayList<>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketHistory> history = new ArrayList<>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketEscalation> escalations = new ArrayList<>();
    
    // Constructors
    public Ticket() {}
    
    public Ticket(String title, String description, TicketCategory category, User createdBy) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.createdBy = createdBy;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTicketNumber() {
        return ticketNumber;
    }
    
    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TicketCategory getCategory() {
        return category;
    }
    
    public void setCategory(TicketCategory category) {
        this.category = category;
    }
    
    public String getCustomCategory() {
        return customCategory;
    }
    
    public void setCustomCategory(String customCategory) {
        this.customCategory = customCategory;
    }
    
    /**
     * Get the effective category - returns custom category if set, otherwise enum category
     */
    public String getEffectiveCategory() {
        if (customCategory != null && !customCategory.trim().isEmpty()) {
            return customCategory;
        }
        return category != null ? category.toString() : "GENERAL";
    }
    
    public TicketPriority getPriority() {
        return priority;
    }
    
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
    
    public TicketStatus getStatus() {
        return status;
    }
    
    public void setStatus(TicketStatus status) {
        this.status = status;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    @JsonProperty("building")
    public String getHostelBlock() {
        return hostelBlock;
    }
    
    public void setHostelBlock(String hostelBlock) {
        this.hostelBlock = hostelBlock;
    }
    
    /**
     * Get hostel block as HostelName enum for assignment logic
     */
    public HostelName getHostelBlockEnum() {
        return hostelBlock != null ? HostelName.fromAnyName(hostelBlock) : null;
    }
    
    public Integer getFloorNumber() {
        return floorNumber;
    }
    
    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }
    
    public String getLocationDetails() {
        return locationDetails;
    }
    
    public void setLocationDetails(String locationDetails) {
        this.locationDetails = locationDetails;
    }
    
    public LocalDateTime getEstimatedResolutionTime() {
        return estimatedResolutionTime;
    }
    
    public void setEstimatedResolutionTime(LocalDateTime estimatedResolutionTime) {
        this.estimatedResolutionTime = estimatedResolutionTime;
    }
    
    public LocalDateTime getActualResolutionTime() {
        return actualResolutionTime;
    }
    
    public void setActualResolutionTime(LocalDateTime actualResolutionTime) {
        this.actualResolutionTime = actualResolutionTime;
    }
    
    public LocalDateTime getSlaBreachTime() {
        return slaBreachTime;
    }
    
    public void setSlaBreachTime(LocalDateTime slaBreachTime) {
        this.slaBreachTime = slaBreachTime;
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
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public LocalDateTime getClosedAt() {
        return closedAt;
    }
    
    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
    
    public Boolean getIsEmergency() {
        return isEmergency;
    }
    
    public void setIsEmergency(Boolean isEmergency) {
        this.isEmergency = isEmergency;
    }
    
    public Boolean getIsRecurring() {
        return isRecurring;
    }
    
    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public Ticket getParentTicket() {
        return parentTicket;
    }
    
    public void setParentTicket(Ticket parentTicket) {
        this.parentTicket = parentTicket;
    }
    
    public java.math.BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
    
    public void setEstimatedCost(java.math.BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    public java.math.BigDecimal getActualCost() {
        return actualCost;
    }
    
    public void setActualCost(java.math.BigDecimal actualCost) {
        this.actualCost = actualCost;
    }
    
    public Integer getSatisfactionRating() {
        return satisfactionRating;
    }
    
    public void setSatisfactionRating(Integer satisfactionRating) {
        this.satisfactionRating = satisfactionRating;
    }
    
    public String getFeedback() {
        return feedback;
    }
    
    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
    
    // Relationship getters
    public List<TicketComment> getComments() {
        return comments;
    }
    
    public void setComments(List<TicketComment> comments) {
        this.comments = comments;
    }
    
    public List<TicketAttachment> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<TicketAttachment> attachments) {
        this.attachments = attachments;
    }
    
    public List<TicketHistory> getHistory() {
        return history;
    }
    
    public void setHistory(List<TicketHistory> history) {
        this.history = history;
    }
    
    public List<TicketEscalation> getEscalations() {
        return escalations;
    }
    
    public void setEscalations(List<TicketEscalation> escalations) {
        this.escalations = escalations;
    }
    
    // Utility methods
    public boolean isOpen() {
        return TicketStatus.OPEN.equals(status);
    }
    
    public boolean isInProgress() {
        return TicketStatus.IN_PROGRESS.equals(status);
    }
    
    public boolean isOnHold() {
        return TicketStatus.ON_HOLD.equals(status);
    }
    
    public boolean isResolved() {
        return TicketStatus.RESOLVED.equals(status);
    }
    
    public boolean isClosed() {
        return TicketStatus.CLOSED.equals(status);
    }
    
    public boolean isCancelled() {
        return TicketStatus.CANCELLED.equals(status);
    }
    
    public boolean isEmergency() {
        return TicketPriority.EMERGENCY.equals(priority);
    }
    
    public boolean isHigh() {
        return TicketPriority.HIGH.equals(priority);
    }
    
    public boolean isEmergencyCategory() {
        return TicketCategory.SAFETY_SECURITY.equals(category);
    }
    
    public boolean isSafetySecurity() {
        return TicketCategory.SAFETY_SECURITY.equals(category);
    }
    
    public boolean isAssigned() {
        return assignedTo != null;
    }
    
    public boolean canBeAssigned() {
        return isOpen() || isOnHold();
    }
    
    public boolean canBeResolved() {
        return isInProgress() || isOnHold();
    }
    
    public boolean canBeClosed() {
        return isResolved();
    }
    
    public boolean canBeCancelled() {
        return isOpen() || isOnHold();
    }
    
    public long getAgeInHours() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }
    
    public boolean isOverdue() {
        if (estimatedResolutionTime == null) return false;
        return LocalDateTime.now().isAfter(estimatedResolutionTime) && !isResolved() && !isClosed();
    }
    
    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", status=" + status +
                ", roomNumber='" + roomNumber + '\'' +
                ", hostelBlock='" + hostelBlock + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return id != null && id.equals(ticket.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 