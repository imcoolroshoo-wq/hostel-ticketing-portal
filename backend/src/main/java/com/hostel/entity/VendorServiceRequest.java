package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing service requests sent to external vendors
 * Implements Vendor Service Management as per PDD Section 4.3.4
 */
@Entity
@Table(name = "vendor_service_requests", indexes = {
    @Index(name = "idx_vendor_service_requests_vendor", columnList = "vendor_id"),
    @Index(name = "idx_vendor_service_requests_ticket", columnList = "ticket_id"),
    @Index(name = "idx_vendor_service_requests_status", columnList = "status"),
    @Index(name = "idx_vendor_service_requests_created_at", columnList = "created_at")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VendorServiceRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @NotBlank(message = "Service description is required")
    @Size(min = 10, message = "Service description must be at least 10 characters")
    @Column(name = "service_description", nullable = false, columnDefinition = "TEXT")
    private String serviceDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorServiceStatus status = VendorServiceStatus.REQUESTED;
    
    @Column(name = "requested_by", nullable = false)
    private UUID requestedBy;
    
    @Column(name = "approved_by")
    private UUID approvedBy;
    
    @Column(name = "expected_completion_date")
    private LocalDateTime expectedCompletionDate;
    
    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;
    
    @Column(name = "response_time_hours")
    private Integer responseTimeHours;
    
    @Column(name = "completion_time_hours")
    private Integer completionTimeHours;
    
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(name = "actual_cost", precision = 10, scale = 2)
    private BigDecimal actualCost;
    
    @Column(name = "vendor_quote", precision = 10, scale = 2)
    private BigDecimal vendorQuote;
    
    @Column(name = "quote_received_at")
    private LocalDateTime quoteReceivedAt;
    
    @Column(name = "work_started_at")
    private LocalDateTime workStartedAt;
    
    @Column(name = "materials_used", columnDefinition = "TEXT")
    private String materialsUsed;
    
    @Column(name = "work_performed", columnDefinition = "TEXT")
    private String workPerformed;
    
    @Column(name = "vendor_notes", columnDefinition = "TEXT")
    private String vendorNotes;
    
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
    
    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;
    
    @Column(name = "quality_rating")
    private Integer qualityRating;
    
    @Column(name = "timeliness_rating")
    private Integer timelinessRating;
    
    @Column(name = "communication_rating")
    private Integer communicationRating;
    
    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;
    
    @Column(name = "invoice_date")
    private LocalDateTime invoiceDate;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths;
    
    @Column(name = "warranty_expiry_date")
    private LocalDateTime warrantyExpiryDate;
    
    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;
    
    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public VendorServiceRequest() {}
    
    public VendorServiceRequest(Vendor vendor, Ticket ticket, String serviceDescription, UUID requestedBy) {
        this.vendor = vendor;
        this.ticket = ticket;
        this.serviceDescription = serviceDescription;
        this.requestedBy = requestedBy;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Vendor getVendor() {
        return vendor;
    }
    
    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
    
    public String getServiceDescription() {
        return serviceDescription;
    }
    
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }
    
    public VendorServiceStatus getStatus() {
        return status;
    }
    
    public void setStatus(VendorServiceStatus status) {
        this.status = status;
    }
    
    public UUID getRequestedBy() {
        return requestedBy;
    }
    
    public void setRequestedBy(UUID requestedBy) {
        this.requestedBy = requestedBy;
    }
    
    public UUID getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(UUID approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getExpectedCompletionDate() {
        return expectedCompletionDate;
    }
    
    public void setExpectedCompletionDate(LocalDateTime expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }
    
    public LocalDateTime getActualCompletionDate() {
        return actualCompletionDate;
    }
    
    public void setActualCompletionDate(LocalDateTime actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }
    
    public Integer getResponseTimeHours() {
        return responseTimeHours;
    }
    
    public void setResponseTimeHours(Integer responseTimeHours) {
        this.responseTimeHours = responseTimeHours;
    }
    
    public Integer getCompletionTimeHours() {
        return completionTimeHours;
    }
    
    public void setCompletionTimeHours(Integer completionTimeHours) {
        this.completionTimeHours = completionTimeHours;
    }
    
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
    
    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }
    
    public BigDecimal getActualCost() {
        return actualCost;
    }
    
    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }
    
    public BigDecimal getVendorQuote() {
        return vendorQuote;
    }
    
    public void setVendorQuote(BigDecimal vendorQuote) {
        this.vendorQuote = vendorQuote;
    }
    
    public LocalDateTime getQuoteReceivedAt() {
        return quoteReceivedAt;
    }
    
    public void setQuoteReceivedAt(LocalDateTime quoteReceivedAt) {
        this.quoteReceivedAt = quoteReceivedAt;
    }
    
    public LocalDateTime getWorkStartedAt() {
        return workStartedAt;
    }
    
    public void setWorkStartedAt(LocalDateTime workStartedAt) {
        this.workStartedAt = workStartedAt;
    }
    
    public String getMaterialsUsed() {
        return materialsUsed;
    }
    
    public void setMaterialsUsed(String materialsUsed) {
        this.materialsUsed = materialsUsed;
    }
    
    public String getWorkPerformed() {
        return workPerformed;
    }
    
    public void setWorkPerformed(String workPerformed) {
        this.workPerformed = workPerformed;
    }
    
    public String getVendorNotes() {
        return vendorNotes;
    }
    
    public void setVendorNotes(String vendorNotes) {
        this.vendorNotes = vendorNotes;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    public Integer getSatisfactionRating() {
        return satisfactionRating;
    }
    
    public void setSatisfactionRating(Integer satisfactionRating) {
        this.satisfactionRating = satisfactionRating;
    }
    
    public Integer getQualityRating() {
        return qualityRating;
    }
    
    public void setQualityRating(Integer qualityRating) {
        this.qualityRating = qualityRating;
    }
    
    public Integer getTimelinessRating() {
        return timelinessRating;
    }
    
    public void setTimelinessRating(Integer timelinessRating) {
        this.timelinessRating = timelinessRating;
    }
    
    public Integer getCommunicationRating() {
        return communicationRating;
    }
    
    public void setCommunicationRating(Integer communicationRating) {
        this.communicationRating = communicationRating;
    }
    
    public String getOverallFeedback() {
        return overallFeedback;
    }
    
    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }
    
    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public Integer getWarrantyPeriodMonths() {
        return warrantyPeriodMonths;
    }
    
    public void setWarrantyPeriodMonths(Integer warrantyPeriodMonths) {
        this.warrantyPeriodMonths = warrantyPeriodMonths;
    }
    
    public LocalDateTime getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }
    
    public void setWarrantyExpiryDate(LocalDateTime warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }
    
    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }
    
    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }
    
    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }
    
    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
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
    public boolean isCompleted() {
        return status == VendorServiceStatus.COMPLETED;
    }
    
    public boolean isPending() {
        return status == VendorServiceStatus.REQUESTED || status == VendorServiceStatus.QUOTE_RECEIVED;
    }
    
    public boolean isInProgress() {
        return status == VendorServiceStatus.APPROVED || status == VendorServiceStatus.IN_PROGRESS;
    }
    
    public double getAverageRating() {
        int ratingCount = 0;
        int totalRating = 0;
        
        if (satisfactionRating != null) {
            totalRating += satisfactionRating;
            ratingCount++;
        }
        if (qualityRating != null) {
            totalRating += qualityRating;
            ratingCount++;
        }
        if (timelinessRating != null) {
            totalRating += timelinessRating;
            ratingCount++;
        }
        if (communicationRating != null) {
            totalRating += communicationRating;
            ratingCount++;
        }
        
        return ratingCount > 0 ? (double) totalRating / ratingCount : 0.0;
    }
    
    public boolean isOverdue() {
        return expectedCompletionDate != null && 
               LocalDateTime.now().isAfter(expectedCompletionDate) && 
               !isCompleted();
    }
    
    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.Duration.between(expectedCompletionDate, LocalDateTime.now()).toDays();
    }
    
    @Override
    public String toString() {
        return "VendorServiceRequest{" +
                "id=" + id +
                ", vendor=" + (vendor != null ? vendor.getVendorName() : "null") +
                ", ticket=" + (ticket != null ? ticket.getTicketNumber() : "null") +
                ", status=" + status +
                ", estimatedCost=" + estimatedCost +
                ", actualCost=" + actualCost +
                '}';
    }
}
