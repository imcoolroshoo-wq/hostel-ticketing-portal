package com.hostel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing asset movements/transfers between locations
 */
@Entity
@Table(name = "asset_movements")
public class AssetMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;
    
    @Column(nullable = false)
    private String fromBuilding;
    
    private String fromRoomNumber;
    
    private String fromLocation;
    
    @Column(nullable = false)
    private String toBuilding;
    
    private String toRoomNumber;
    
    private String toLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moved_by", nullable = false)
    private User movedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_by")
    private User authorizedBy;
    
    private String reason;
    
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementStatus status;
    
    @CreationTimestamp
    private LocalDateTime movementDate;
    
    private LocalDateTime completedDate;
    
    // Constructors
    public AssetMovement() {}
    
    public AssetMovement(Asset asset, String fromBuilding, String toBuilding, User movedBy) {
        this.asset = asset;
        this.fromBuilding = fromBuilding;
        this.toBuilding = toBuilding;
        this.movedBy = movedBy;
        this.status = MovementStatus.PLANNED;
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
    
    public String getFromBuilding() {
        return fromBuilding;
    }
    
    public void setFromBuilding(String fromBuilding) {
        this.fromBuilding = fromBuilding;
    }
    
    public String getFromRoomNumber() {
        return fromRoomNumber;
    }
    
    public void setFromRoomNumber(String fromRoomNumber) {
        this.fromRoomNumber = fromRoomNumber;
    }
    
    public String getFromLocation() {
        return fromLocation;
    }
    
    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }
    
    public String getToBuilding() {
        return toBuilding;
    }
    
    public void setToBuilding(String toBuilding) {
        this.toBuilding = toBuilding;
    }
    
    public String getToRoomNumber() {
        return toRoomNumber;
    }
    
    public void setToRoomNumber(String toRoomNumber) {
        this.toRoomNumber = toRoomNumber;
    }
    
    public String getToLocation() {
        return toLocation;
    }
    
    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }
    
    public User getMovedBy() {
        return movedBy;
    }
    
    public void setMovedBy(User movedBy) {
        this.movedBy = movedBy;
    }
    
    public User getAuthorizedBy() {
        return authorizedBy;
    }
    
    public void setAuthorizedBy(User authorizedBy) {
        this.authorizedBy = authorizedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public MovementStatus getStatus() {
        return status;
    }
    
    public void setStatus(MovementStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getMovementDate() {
        return movementDate;
    }
    
    public void setMovementDate(LocalDateTime movementDate) {
        this.movementDate = movementDate;
    }
    
    public LocalDateTime getCompletedDate() {
        return completedDate;
    }
    
    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }
    
    // Utility methods
    public String getFromFullLocation() {
        StringBuilder location = new StringBuilder(fromBuilding);
        if (fromRoomNumber != null && !fromRoomNumber.isEmpty()) {
            location.append(" - Room ").append(fromRoomNumber);
        }
        if (fromLocation != null && !fromLocation.isEmpty()) {
            location.append(" (").append(fromLocation).append(")");
        }
        return location.toString();
    }
    
    public String getToFullLocation() {
        StringBuilder location = new StringBuilder(toBuilding);
        if (toRoomNumber != null && !toRoomNumber.isEmpty()) {
            location.append(" - Room ").append(toRoomNumber);
        }
        if (toLocation != null && !toLocation.isEmpty()) {
            location.append(" (").append(toLocation).append(")");
        }
        return location.toString();
    }
    
    public void complete() {
        this.status = MovementStatus.COMPLETED;
        this.completedDate = LocalDateTime.now();
        
        // Update asset location
        if (asset != null) {
            asset.setBuilding(toBuilding);
            asset.setRoomNumber(toRoomNumber);
            asset.setLocation(toLocation);
        }
    }
}

/**
 * Enum for movement status
 */
enum MovementStatus {
    PLANNED("Planned", "Movement is planned"),
    IN_PROGRESS("In Progress", "Movement is in progress"),
    COMPLETED("Completed", "Movement has been completed"),
    CANCELLED("Cancelled", "Movement has been cancelled");
    
    private final String displayName;
    private final String description;
    
    MovementStatus(String displayName, String description) {
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
