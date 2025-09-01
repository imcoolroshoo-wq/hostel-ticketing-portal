package com.hostel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing multi-dimensional mapping between staff, hostel blocks, and ticket categories
 * Supports the intelligent assignment algorithm as per product design
 */
@Entity
@Table(name = "category_staff_mappings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"staff_id", "hostel_block", "category"}))
public class CategoryStaffMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Column(name = "hostel_block", length = 50)
    private String hostelBlock; // NULL means all blocks

    @Column(nullable = false, length = 100)
    private String category; // Can be enum value or custom category

    @Column(name = "priority_level", nullable = false)
    private Integer priorityLevel = 1; // 1 = highest priority

    @Column(name = "capacity_weight", nullable = false, precision = 3, scale = 2)
    private java.math.BigDecimal capacityWeight = java.math.BigDecimal.ONE; // Staff capacity multiplier

    @Column(name = "expertise_level", nullable = false)
    private Integer expertiseLevel = 1; // 1-5 scale

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public CategoryStaffMapping() {}

    public CategoryStaffMapping(User staff, String hostelBlock, String category, Integer priorityLevel) {
        this.staff = staff;
        this.hostelBlock = hostelBlock;
        this.category = category;
        this.priorityLevel = priorityLevel;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHostelBlock() {
        return hostelBlock;
    }

    public void setHostelBlock(String hostelBlock) {
        this.hostelBlock = hostelBlock;
    }

    public Integer getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(Integer priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public java.math.BigDecimal getCapacityWeight() {
        return capacityWeight;
    }

    public void setCapacityWeight(java.math.BigDecimal capacityWeight) {
        this.capacityWeight = capacityWeight;
    }

    public Integer getExpertiseLevel() {
        return expertiseLevel;
    }

    public void setExpertiseLevel(Integer expertiseLevel) {
        this.expertiseLevel = expertiseLevel;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CategoryStaffMapping{" +
                "id=" + id +
                ", staff=" + (staff != null ? staff.getUsername() : "null") +
                ", hostelBlock='" + hostelBlock + '\'' +
                ", category='" + category + '\'' +
                ", priorityLevel=" + priorityLevel +
                ", expertiseLevel=" + expertiseLevel +
                ", isActive=" + isActive +
                '}';
    }
}
