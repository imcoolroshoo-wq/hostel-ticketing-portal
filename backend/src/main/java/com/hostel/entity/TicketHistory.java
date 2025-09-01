package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TicketHistory entity representing the audit trail of changes made to tickets.
 * Tracks all field modifications for compliance and debugging purposes.
 */
@Entity
@Table(name = "ticket_history", indexes = {
    @Index(name = "idx_history_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_history_changed_by", columnList = "changed_by"),
    @Index(name = "idx_history_changed_at", columnList = "changed_at"),
    @Index(name = "idx_history_field_name", columnList = "field_name")
})
public class TicketHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Ticket is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @NotBlank(message = "Field name is required")
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;
    
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    
    @NotNull(message = "Changed by is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;
    
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    // Constructors
    public TicketHistory() {}
    
    public TicketHistory(Ticket ticket, String fieldName, String oldValue, String newValue, User changedBy) {
        this.ticket = ticket;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public User getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
    
    // Utility methods
    public boolean hasValueChanged() {
        if (oldValue == null && newValue == null) return false;
        if (oldValue == null || newValue == null) return true;
        return !oldValue.equals(newValue);
    }
    
    public String getChangeDescription() {
        if (!hasValueChanged()) return "No change";
        
        String fieldDisplayName = getFieldDisplayName();
        
        if (oldValue == null) {
            return fieldDisplayName + " set to: " + newValue;
        } else if (newValue == null) {
            return fieldDisplayName + " cleared (was: " + oldValue + ")";
        } else {
            return fieldDisplayName + " changed from '" + oldValue + "' to '" + newValue + "'";
        }
    }
    
    public String getFieldDisplayName() {
        switch (fieldName) {
            case "status":
                return "Status";
            case "priority":
                return "Priority";
            case "category":
                return "Category";
            case "assignedTo":
                return "Assigned To";
            case "title":
                return "Title";
            case "description":
                return "Description";
            case "roomNumber":
                return "Room Number";
            case "building":
                return "Building";
            case "locationDetails":
                return "Location Details";
            case "estimatedResolutionTime":
                return "Estimated Resolution Time";
            default:
                return fieldName;
        }
    }
    
    public boolean isStatusChange() {
        return "status".equals(fieldName);
    }
    
    public boolean isPriorityChange() {
        return "priority".equals(fieldName);
    }
    
    public boolean isAssignmentChange() {
        return "assignedTo".equals(fieldName);
    }
    
    public boolean isSignificantChange() {
        return isStatusChange() || isPriorityChange() || isAssignmentChange();
    }
    
    public String getChangedByDisplayName() {
        return changedBy != null ? changedBy.getFullName() : "Unknown User";
    }
    
    public String getChangedByRole() {
        return changedBy != null ? changedBy.getRole().getDisplayName() : "Unknown";
    }
    
    public String getFormattedChangedAt() {
        if (changedAt == null) return "Unknown";
        return changedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    @Override
    public String toString() {
        return "TicketHistory{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", fieldName='" + fieldName + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                ", changedBy=" + (changedBy != null ? changedBy.getId() : null) +
                ", changedAt=" + changedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketHistory that = (TicketHistory) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 