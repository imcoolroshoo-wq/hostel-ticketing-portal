package com.hostel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing physical assets in the hostel (furniture, appliances, equipment)
 */
@Entity
@Table(name = "assets")
public class Asset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String assetTag;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;
    
    @Column(nullable = false)
    private String building;
    
    private String roomNumber;
    
    private String location;
    
    private String manufacturer;
    
    private String model;
    
    private String serialNumber;
    
    private LocalDateTime purchaseDate;
    
    private LocalDateTime warrantyExpiry;
    
    private Double purchasePrice;
    
    private Double currentValue;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceSchedule> maintenanceSchedules;
    
    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AssetMovement> movements;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // Constructors
    public Asset() {}
    
    public Asset(String assetTag, String name, AssetType type, String building) {
        this.assetTag = assetTag;
        this.name = name;
        this.type = type;
        this.building = building;
        this.status = AssetStatus.ACTIVE;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getAssetTag() {
        return assetTag;
    }
    
    public void setAssetTag(String assetTag) {
        this.assetTag = assetTag;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public AssetType getType() {
        return type;
    }
    
    public void setType(AssetType type) {
        this.type = type;
    }
    
    public AssetStatus getStatus() {
        return status;
    }
    
    public void setStatus(AssetStatus status) {
        this.status = status;
    }
    
    public String getBuilding() {
        return building;
    }
    
    public void setBuilding(String building) {
        this.building = building;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDateTime getWarrantyExpiry() {
        return warrantyExpiry;
    }
    
    public void setWarrantyExpiry(LocalDateTime warrantyExpiry) {
        this.warrantyExpiry = warrantyExpiry;
    }
    
    public Double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public Double getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(Double currentValue) {
        this.currentValue = currentValue;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public List<MaintenanceSchedule> getMaintenanceSchedules() {
        return maintenanceSchedules;
    }
    
    public void setMaintenanceSchedules(List<MaintenanceSchedule> maintenanceSchedules) {
        this.maintenanceSchedules = maintenanceSchedules;
    }
    
    public List<AssetMovement> getMovements() {
        return movements;
    }
    
    public void setMovements(List<AssetMovement> movements) {
        this.movements = movements;
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
    public boolean isUnderWarranty() {
        return warrantyExpiry != null && warrantyExpiry.isAfter(LocalDateTime.now());
    }
    
    public boolean needsMaintenance() {
        return maintenanceSchedules != null && 
               maintenanceSchedules.stream()
                   .anyMatch(schedule -> schedule.getNextDueDate().isBefore(LocalDateTime.now()));
    }
    
    public String getFullLocation() {
        StringBuilder location = new StringBuilder(building);
        if (roomNumber != null && !roomNumber.isEmpty()) {
            location.append(" - Room ").append(roomNumber);
        }
        if (this.location != null && !this.location.isEmpty()) {
            location.append(" (").append(this.location).append(")");
        }
        return location.toString();
    }
}
