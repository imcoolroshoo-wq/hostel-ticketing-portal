package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TicketEscalation entity representing escalations of tickets to higher-level staff.
 * Tracks when and why tickets are escalated and their resolution.
 */
@Entity
@Table(name = "ticket_escalations", indexes = {
    @Index(name = "idx_escalations_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_escalations_escalated_from", columnList = "escalated_from"),
    @Index(name = "idx_escalations_escalated_to", columnList = "escalated_to"),
    @Index(name = "idx_escalations_escalated_at", columnList = "escalated_at")
})
public class TicketEscalation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Ticket is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @NotNull(message = "Escalated from is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_from", nullable = false)
    private User escalatedFrom;
    
    @NotNull(message = "Escalated to is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to", nullable = false)
    private User escalatedTo;
    
    @NotBlank(message = "Escalation reason is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "escalation_level")
    private Integer escalationLevel;
    
    @Column(name = "is_auto_escalated")
    private Boolean isAutoEscalated = false;
    
    @CreationTimestamp
    @Column(name = "escalated_at", nullable = false, updatable = false)
    private LocalDateTime escalatedAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    // Constructors
    public TicketEscalation() {}
    
    public TicketEscalation(Ticket ticket, User escalatedFrom, User escalatedTo, String reason) {
        this.ticket = ticket;
        this.escalatedFrom = escalatedFrom;
        this.escalatedTo = escalatedTo;
        this.reason = reason;
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
    
    public User getEscalatedFrom() {
        return escalatedFrom;
    }
    
    public void setEscalatedFrom(User escalatedFrom) {
        this.escalatedFrom = escalatedFrom;
    }
    
    public User getEscalatedTo() {
        return escalatedTo;
    }
    
    public void setEscalatedTo(User escalatedTo) {
        this.escalatedTo = escalatedTo;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }
    
    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    public Integer getEscalationLevel() {
        return escalationLevel;
    }
    
    public void setEscalationLevel(Integer escalationLevel) {
        this.escalationLevel = escalationLevel;
    }
    
    public Boolean getIsAutoEscalated() {
        return isAutoEscalated;
    }
    
    public void setIsAutoEscalated(Boolean isAutoEscalated) {
        this.isAutoEscalated = isAutoEscalated;
    }
    
    // Utility methods
    public boolean isResolved() {
        return resolvedAt != null;
    }
    
    public boolean isActive() {
        return !isResolved();
    }
    
    public long getEscalationAgeInHours() {
        if (escalatedAt == null) return 0;
        LocalDateTime endTime = resolvedAt != null ? resolvedAt : LocalDateTime.now();
        return java.time.Duration.between(escalatedAt, endTime).toHours();
    }
    
    public boolean isOverdue() {
        if (isResolved()) return false;
        
        // Check if escalation is overdue based on ticket priority
        if (ticket != null && ticket.getPriority() != null) {
            int escalationHours = ticket.getPriority().getEscalationHours();
            return getEscalationAgeInHours() > escalationHours;
        }
        
        return false;
    }
    
    public String getEscalatedFromDisplayName() {
        return escalatedFrom != null ? escalatedFrom.getFullName() : "Unknown User";
    }
    
    public String getEscalatedToDisplayName() {
        return escalatedTo != null ? escalatedTo.getFullName() : "Unknown User";
    }
    
    public String getEscalatedFromRole() {
        return escalatedFrom != null ? escalatedFrom.getRole().getDisplayName() : "Unknown";
    }
    
    public String getEscalatedToRole() {
        return escalatedTo != null ? escalatedTo.getRole().getDisplayName() : "Unknown";
    }
    
    public String getTicketNumber() {
        return ticket != null ? ticket.getTicketNumber() : null;
    }
    
    public String getTicketTitle() {
        return ticket != null ? ticket.getTitle() : null;
    }
    
    public String getTicketCategory() {
        return ticket != null ? ticket.getCategory().getDisplayName() : null;
    }
    
    public String getTicketPriority() {
        return ticket != null ? ticket.getPriority().getDisplayName() : null;
    }
    
    public String getFormattedEscalatedAt() {
        if (escalatedAt == null) return "Unknown";
        return escalatedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    public String getFormattedResolvedAt() {
        if (resolvedAt == null) return "Not resolved";
        return resolvedAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    public void resolve() {
        this.resolvedAt = LocalDateTime.now();
    }
    
    public boolean canBeResolvedBy(User user) {
        if (user == null) return false;
        
        // Admin can resolve any escalation
        if (user.isAdmin()) return true;
        
        // User escalated to can resolve the escalation
        return escalatedTo != null && escalatedTo.getId().equals(user.getId());
    }
    
    public boolean canBeViewedBy(User user) {
        if (user == null) return false;
        
        // Admin can view all escalations
        if (user.isAdmin()) return true;
        
        // Staff can view escalations they're involved in
        if (user.isStaff()) {
            return (escalatedFrom != null && escalatedFrom.getId().equals(user.getId())) ||
                   (escalatedTo != null && escalatedTo.getId().equals(user.getId()));
        }
        
        // Students can only view escalations on their own tickets
        return ticket != null && ticket.getCreatedBy() != null && 
               ticket.getCreatedBy().getId().equals(user.getId());
    }
    
    @Override
    public String toString() {
        return "TicketEscalation{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", escalatedFrom=" + (escalatedFrom != null ? escalatedFrom.getId() : null) +
                ", escalatedTo=" + (escalatedTo != null ? escalatedTo.getId() : null) +
                ", reason='" + reason + '\'' +
                ", escalatedAt=" + escalatedAt +
                ", resolvedAt=" + resolvedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketEscalation that = (TicketEscalation) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 