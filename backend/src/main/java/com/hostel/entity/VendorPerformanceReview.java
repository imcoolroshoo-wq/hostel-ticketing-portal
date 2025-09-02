package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing vendor performance reviews
 */
@Entity
@Table(name = "vendor_performance_reviews")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VendorPerformanceReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @Column(name = "review_period_start")
    private LocalDateTime reviewPeriodStart;
    
    @Column(name = "review_period_end")
    private LocalDateTime reviewPeriodEnd;
    
    @Min(1) @Max(5)
    @Column(name = "overall_rating")
    private Integer overallRating;
    
    @Min(1) @Max(5)
    @Column(name = "quality_rating")
    private Integer qualityRating;
    
    @Min(1) @Max(5)
    @Column(name = "timeliness_rating")
    private Integer timelinessRating;
    
    @Min(1) @Max(5)
    @Column(name = "communication_rating")
    private Integer communicationRating;
    
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;
    
    @Column(name = "reviewed_by")
    private UUID reviewedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public VendorPerformanceReview() {}
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }
    
    public LocalDateTime getReviewPeriodStart() { return reviewPeriodStart; }
    public void setReviewPeriodStart(LocalDateTime reviewPeriodStart) { this.reviewPeriodStart = reviewPeriodStart; }
    
    public LocalDateTime getReviewPeriodEnd() { return reviewPeriodEnd; }
    public void setReviewPeriodEnd(LocalDateTime reviewPeriodEnd) { this.reviewPeriodEnd = reviewPeriodEnd; }
    
    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }
    
    public Integer getQualityRating() { return qualityRating; }
    public void setQualityRating(Integer qualityRating) { this.qualityRating = qualityRating; }
    
    public Integer getTimelinessRating() { return timelinessRating; }
    public void setTimelinessRating(Integer timelinessRating) { this.timelinessRating = timelinessRating; }
    
    public Integer getCommunicationRating() { return communicationRating; }
    public void setCommunicationRating(Integer communicationRating) { this.communicationRating = communicationRating; }
    
    public String getReviewComments() { return reviewComments; }
    public void setReviewComments(String reviewComments) { this.reviewComments = reviewComments; }
    
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
