package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing IIM Trichy hostel blocks with their specific configurations
 */
@Entity
@Table(name = "hostel_blocks", indexes = {
    @Index(name = "idx_hostel_blocks_block_code", columnList = "block_code"),
    @Index(name = "idx_hostel_blocks_is_active", columnList = "is_active"),
    @Index(name = "idx_hostel_blocks_is_female", columnList = "is_female_block")
})
public class HostelBlock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Block name is required")
    @Column(name = "block_name", nullable = false, unique = true, length = 50)
    private String blockName;
    
    @NotBlank(message = "Block code is required")
    @Column(name = "block_code", nullable = false, unique = true, length = 10)
    private String blockCode;
    
    @NotNull(message = "Total floors is required")
    @Positive(message = "Total floors must be positive")
    @Column(name = "total_floors", nullable = false)
    private Integer totalFloors = 3;
    
    @NotNull(message = "Rooms per floor is required")
    @Positive(message = "Rooms per floor must be positive")
    @Column(name = "rooms_per_floor", nullable = false)
    private Integer roomsPerFloor = 18;
    
    @NotNull(message = "Total rooms is required")
    @Positive(message = "Total rooms must be positive")
    @Column(name = "total_rooms", nullable = false)
    private Integer totalRooms;
    
    @Column(name = "is_female_block", nullable = false)
    private Boolean isFemaleBlock = false;
    
    @Column(name = "has_disabled_access", nullable = false)
    private Boolean hasDisabledAccess = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warden_id")
    private User warden;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "hostelBlock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> residents = new ArrayList<>();
    
    // Constructors
    public HostelBlock() {}
    
    public HostelBlock(String blockName, String blockCode, Integer totalFloors, 
                      Integer roomsPerFloor, Boolean isFemaleBlock) {
        this.blockName = blockName;
        this.blockCode = blockCode;
        this.totalFloors = totalFloors;
        this.roomsPerFloor = roomsPerFloor;
        this.totalRooms = totalFloors * roomsPerFloor;
        this.isFemaleBlock = isFemaleBlock;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getBlockName() {
        return blockName;
    }
    
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }
    
    public String getBlockCode() {
        return blockCode;
    }
    
    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }
    
    public Integer getTotalFloors() {
        return totalFloors;
    }
    
    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
        if (this.roomsPerFloor != null) {
            this.totalRooms = totalFloors * this.roomsPerFloor;
        }
    }
    
    public Integer getRoomsPerFloor() {
        return roomsPerFloor;
    }
    
    public void setRoomsPerFloor(Integer roomsPerFloor) {
        this.roomsPerFloor = roomsPerFloor;
        if (this.totalFloors != null) {
            this.totalRooms = this.totalFloors * roomsPerFloor;
        }
    }
    
    public Integer getTotalRooms() {
        return totalRooms;
    }
    
    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }
    
    public Boolean getIsFemaleBlock() {
        return isFemaleBlock;
    }
    
    public void setIsFemaleBlock(Boolean isFemaleBlock) {
        this.isFemaleBlock = isFemaleBlock;
    }
    
    public Boolean getHasDisabledAccess() {
        return hasDisabledAccess;
    }
    
    public void setHasDisabledAccess(Boolean hasDisabledAccess) {
        this.hasDisabledAccess = hasDisabledAccess;
    }
    
    public User getWarden() {
        return warden;
    }
    
    public void setWarden(User warden) {
        this.warden = warden;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<User> getResidents() {
        return residents;
    }
    
    public void setResidents(List<User> residents) {
        this.residents = residents;
    }
    
    // Utility methods
    public boolean isFemaleOnly() {
        return Boolean.TRUE.equals(isFemaleBlock);
    }
    
    public boolean hasAccessibilityFeatures() {
        return Boolean.TRUE.equals(hasDisabledAccess);
    }
    
    public int getOccupancyCount() {
        return residents != null ? (int) residents.stream()
            .filter(user -> Boolean.TRUE.equals(user.getIsActive()))
            .count() : 0;
    }
    
    public double getOccupancyRate() {
        if (totalRooms == null || totalRooms == 0) return 0.0;
        return (double) getOccupancyCount() / totalRooms * 100.0;
    }
    
    public boolean isFullyOccupied() {
        return getOccupancyCount() >= totalRooms;
    }
    
    public int getAvailableRooms() {
        return Math.max(0, totalRooms - getOccupancyCount());
    }
    
    @Override
    public String toString() {
        return "HostelBlock{" +
                "id=" + id +
                ", blockName='" + blockName + '\'' +
                ", blockCode='" + blockCode + '\'' +
                ", totalFloors=" + totalFloors +
                ", roomsPerFloor=" + roomsPerFloor +
                ", totalRooms=" + totalRooms +
                ", isFemaleBlock=" + isFemaleBlock +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostelBlock that = (HostelBlock) o;
        return id != null && id.equals(that.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
