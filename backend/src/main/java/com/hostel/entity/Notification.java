package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notification entity representing notifications sent to users in the hostel ticketing system.
 * Supports different notification types and delivery methods.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_is_read", columnList = "is_read"),
    @Index(name = "idx_notifications_created_at", columnList = "created_at"),
    @Index(name = "idx_notifications_type", columnList = "type"),
    @Index(name = "idx_notifications_related_ticket", columnList = "related_ticket_id")
})
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Message is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.IN_APP;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_ticket_id")
    private Ticket relatedTicket;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Constructors
    public Notification() {}
    
    public Notification(User user, String title, String message, NotificationType type) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    public Notification(User user, String title, String message, NotificationType type, Ticket relatedTicket) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedTicket = relatedTicket;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public Ticket getRelatedTicket() {
        return relatedTicket;
    }
    
    public void setRelatedTicket(Ticket relatedTicket) {
        this.relatedTicket = relatedTicket;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    // Utility methods
    public boolean isUnread() {
        return !isRead;
    }
    
    public boolean isEmailNotification() {
        return NotificationType.EMAIL.equals(type);
    }
    
    public boolean isSmsNotification() {
        return NotificationType.SMS.equals(type);
    }
    
    public boolean isInAppNotification() {
        return NotificationType.IN_APP.equals(type);
    }
    
    public boolean isTicketRelated() {
        return relatedTicket != null;
    }
    
    public String getTicketNumber() {
        return relatedTicket != null ? relatedTicket.getTicketNumber() : null;
    }
    
    public String getTicketTitle() {
        return relatedTicket != null ? relatedTicket.getTitle() : null;
    }
    
    public String getTicketCategory() {
        return relatedTicket != null ? relatedTicket.getCategory().getDisplayName() : null;
    }
    
    public String getTicketPriority() {
        return relatedTicket != null ? relatedTicket.getPriority().getDisplayName() : null;
    }
    
    public String getTicketStatus() {
        return relatedTicket != null ? relatedTicket.getStatus().getDisplayName() : null;
    }
    
    public String getRecipientDisplayName() {
        return user != null ? user.getFullName() : "Unknown User";
    }
    
    public String getRecipientEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    public String getRecipientPhone() {
        return user != null ? user.getPhone() : null;
    }
    
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "Unknown";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    public String getFormattedReadAt() {
        if (readAt == null) return "Not read";
        return readAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }
    
    public long getAgeInMinutes() {
        if (createdAt == null) return 0;
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }
    
    public boolean isRecent() {
        return getAgeInMinutes() < 60; // Less than 1 hour
    }
    
    public boolean isOld() {
        return getAgeInMinutes() > 1440; // More than 24 hours
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", relatedTicketId=" + (relatedTicket != null ? relatedTicket.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 