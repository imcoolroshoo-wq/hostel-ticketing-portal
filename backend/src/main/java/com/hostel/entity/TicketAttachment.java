package com.hostel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TicketAttachment entity representing file attachments on tickets.
 * Supports various file types including images, documents, and other media.
 */
@Entity
@Table(name = "ticket_attachments", indexes = {
    @Index(name = "idx_attachments_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_attachments_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_attachments_created_at", columnList = "created_at")
})
public class TicketAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Ticket is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @NotBlank(message = "Filename is required")
    @Column(nullable = false, length = 255)
    private String filename;
    
    @NotBlank(message = "Original filename is required")
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    
    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @NotNull(message = "Uploaded by is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TicketAttachment() {}
    
    public TicketAttachment(Ticket ticket, String filename, String originalFilename, 
                          String filePath, Long fileSize, String mimeType, User uploadedBy) {
        this.ticket = ticket;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.uploadedBy = uploadedBy;
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
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public User getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    public boolean isDocument() {
        if (mimeType == null) return false;
        return mimeType.startsWith("application/pdf") ||
               mimeType.startsWith("application/msword") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
               mimeType.startsWith("text/");
    }
    
    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }
    
    public boolean isAudio() {
        return mimeType != null && mimeType.startsWith("audio/");
    }
    
    public String getFileExtension() {
        if (originalFilename == null) return "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        return lastDotIndex > 0 ? originalFilename.substring(lastDotIndex + 1) : "";
    }
    
    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%d %s", size, units[unitIndex]);
    }
    
    public String getUploaderDisplayName() {
        return uploadedBy != null ? uploadedBy.getFullName() : "Unknown User";
    }
    
    public boolean canBeDownloadedBy(User user) {
        if (user == null) return false;
        
        // Admin can download all attachments
        if (user.isAdmin()) return true;
        
        // Staff can download attachments on tickets they can see
        if (user.isStaff()) return true;
        
        // Students can only download attachments on their own tickets
        return ticket != null && ticket.getCreatedBy() != null && 
               ticket.getCreatedBy().getId().equals(user.getId());
    }
    
    @Override
    public String toString() {
        return "TicketAttachment{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", filename='" + filename + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", fileSize=" + fileSize +
                ", mimeType='" + mimeType + '\'' +
                ", uploadedBy=" + (uploadedBy != null ? uploadedBy.getId() : null) +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketAttachment that = (TicketAttachment) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 