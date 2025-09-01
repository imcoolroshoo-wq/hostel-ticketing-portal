package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TicketComment entity representing comments made on tickets.
 * Comments can be public (visible to ticket creator) or internal (staff only).
 */
@Entity
@Table(name = "ticket_comments", indexes = {
    @Index(name = "idx_comments_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_comments_created_at", columnList = "created_at"),
    @Index(name = "idx_comments_user_id", columnList = "user_id")
})
public class TicketComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Ticket is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Comment is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
    
    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TicketComment() {}
    
    public TicketComment(Ticket ticket, User user, String comment) {
        this.ticket = ticket;
        this.user = user;
        this.comment = comment;
    }
    
    public TicketComment(Ticket ticket, User user, String comment, Boolean isInternal) {
        this.ticket = ticket;
        this.user = user;
        this.comment = comment;
        this.isInternal = isInternal;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public Boolean getIsInternal() {
        return isInternal;
    }
    
    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public boolean isPublic() {
        return !isInternal;
    }
    
    public boolean isStaffComment() {
        return user != null && user.isStaff();
    }
    
    public boolean isStudentComment() {
        return user != null && user.isStudent();
    }
    
    public boolean isAdminComment() {
        return user != null && user.isAdmin();
    }
    
    public String getUserDisplayName() {
        return user != null ? user.getFullName() : "Unknown User";
    }
    
    public String getUserRole() {
        return user != null ? user.getRole().getDisplayName() : "Unknown";
    }
    
    @Override
    public String toString() {
        return "TicketComment{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                ", comment='" + comment + '\'' +
                ", isInternal=" + isInternal +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketComment that = (TicketComment) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 