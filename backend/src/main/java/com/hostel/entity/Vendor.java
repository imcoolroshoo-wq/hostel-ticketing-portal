package com.hostel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing external vendors for specialized repairs and services
 * Implements Vendor Management as per PDD Section 4.3.4
 */
@Entity
@Table(name = "vendors", indexes = {
    @Index(name = "idx_vendors_name", columnList = "vendor_name"),
    @Index(name = "idx_vendors_category", columnList = "service_category"),
    @Index(name = "idx_vendors_status", columnList = "status"),
    @Index(name = "idx_vendors_performance_rating", columnList = "performance_rating")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Vendor name is required")
    @Size(min = 2, max = 100, message = "Vendor name must be between 2 and 100 characters")
    @Column(name = "vendor_name", nullable = false, length = 100)
    private String vendorName;
    
    @NotBlank(message = "Contact person name is required")
    @Size(min = 2, max = 100, message = "Contact person name must be between 2 and 100 characters")
    @Column(name = "contact_person", nullable = false, length = 100)
    private String contactPerson;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Column(length = 100)
    private String email;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @NotBlank(message = "Service category is required")
    @Size(max = 100, message = "Service category must not exceed 100 characters")
    @Column(name = "service_category", nullable = false, length = 100)
    private String serviceCategory;
    
    @Column(name = "specialization", columnDefinition = "TEXT")
    private String specialization;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorStatus status = VendorStatus.ACTIVE;
    
    @Column(name = "performance_rating", precision = 3, scale = 2)
    private BigDecimal performanceRating;
    
    @Column(name = "total_service_requests", nullable = false)
    private Integer totalServiceRequests = 0;
    
    @Column(name = "completed_requests", nullable = false)
    private Integer completedRequests = 0;
    
    @Column(name = "average_response_time_hours")
    private Integer averageResponseTimeHours;
    
    @Column(name = "average_completion_time_hours")
    private Integer averageCompletionTimeHours;
    
    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;
    
    @Column(name = "preferred_vendor")
    private Boolean preferredVendor = false;
    
    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;
    
    @Column(name = "contract_end_date")
    private LocalDateTime contractEndDate;
    
    @Column(name = "sla_hours")
    private Integer slaHours;
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    @Column(name = "minimum_charge", precision = 10, scale = 2)
    private BigDecimal minimumCharge;
    
    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;
    
    @Column(name = "license_number", length = 50)
    private String licenseNumber;
    
    @Column(name = "insurance_details", columnDefinition = "TEXT")
    private String insuranceDetails;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "last_service_date")
    private LocalDateTime lastServiceDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorServiceRequest> serviceRequests = new ArrayList<>();
    
    @JsonIgnore
    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorPerformanceReview> performanceReviews = new ArrayList<>();
    
    // Constructors
    public Vendor() {}
    
    public Vendor(String vendorName, String contactPerson, String phoneNumber, String serviceCategory) {
        this.vendorName = vendorName;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.serviceCategory = serviceCategory;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getVendorName() {
        return vendorName;
    }
    
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getServiceCategory() {
        return serviceCategory;
    }
    
    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }
    
    public String getSpecialization() {
        return specialization;
    }
    
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    
    public VendorStatus getStatus() {
        return status;
    }
    
    public void setStatus(VendorStatus status) {
        this.status = status;
    }
    
    public BigDecimal getPerformanceRating() {
        return performanceRating;
    }
    
    public void setPerformanceRating(BigDecimal performanceRating) {
        this.performanceRating = performanceRating;
    }
    
    public Integer getTotalServiceRequests() {
        return totalServiceRequests;
    }
    
    public void setTotalServiceRequests(Integer totalServiceRequests) {
        this.totalServiceRequests = totalServiceRequests;
    }
    
    public Integer getCompletedRequests() {
        return completedRequests;
    }
    
    public void setCompletedRequests(Integer completedRequests) {
        this.completedRequests = completedRequests;
    }
    
    public Integer getAverageResponseTimeHours() {
        return averageResponseTimeHours;
    }
    
    public void setAverageResponseTimeHours(Integer averageResponseTimeHours) {
        this.averageResponseTimeHours = averageResponseTimeHours;
    }
    
    public Integer getAverageCompletionTimeHours() {
        return averageCompletionTimeHours;
    }
    
    public void setAverageCompletionTimeHours(Integer averageCompletionTimeHours) {
        this.averageCompletionTimeHours = averageCompletionTimeHours;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public Boolean getPreferredVendor() {
        return preferredVendor;
    }
    
    public void setPreferredVendor(Boolean preferredVendor) {
        this.preferredVendor = preferredVendor;
    }
    
    public LocalDateTime getContractStartDate() {
        return contractStartDate;
    }
    
    public void setContractStartDate(LocalDateTime contractStartDate) {
        this.contractStartDate = contractStartDate;
    }
    
    public LocalDateTime getContractEndDate() {
        return contractEndDate;
    }
    
    public void setContractEndDate(LocalDateTime contractEndDate) {
        this.contractEndDate = contractEndDate;
    }
    
    public Integer getSlaHours() {
        return slaHours;
    }
    
    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }
    
    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }
    
    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
    
    public BigDecimal getMinimumCharge() {
        return minimumCharge;
    }
    
    public void setMinimumCharge(BigDecimal minimumCharge) {
        this.minimumCharge = minimumCharge;
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public String getLicenseNumber() {
        return licenseNumber;
    }
    
    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public String getInsuranceDetails() {
        return insuranceDetails;
    }
    
    public void setInsuranceDetails(String insuranceDetails) {
        this.insuranceDetails = insuranceDetails;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getLastServiceDate() {
        return lastServiceDate;
    }
    
    public void setLastServiceDate(LocalDateTime lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
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
    
    public List<VendorServiceRequest> getServiceRequests() {
        return serviceRequests;
    }
    
    public void setServiceRequests(List<VendorServiceRequest> serviceRequests) {
        this.serviceRequests = serviceRequests;
    }
    
    public List<VendorPerformanceReview> getPerformanceReviews() {
        return performanceReviews;
    }
    
    public void setPerformanceReviews(List<VendorPerformanceReview> performanceReviews) {
        this.performanceReviews = performanceReviews;
    }
    
    // Utility methods
    public double getCompletionRate() {
        if (totalServiceRequests == 0) return 0.0;
        return (double) completedRequests / totalServiceRequests * 100.0;
    }
    
    public boolean isActive() {
        return status == VendorStatus.ACTIVE;
    }
    
    public boolean isContractValid() {
        LocalDateTime now = LocalDateTime.now();
        return (contractStartDate == null || !now.isBefore(contractStartDate)) &&
               (contractEndDate == null || !now.isAfter(contractEndDate));
    }
    
    public void incrementServiceRequests() {
        this.totalServiceRequests++;
        this.lastServiceDate = LocalDateTime.now();
    }
    
    public void incrementCompletedRequests() {
        this.completedRequests++;
    }
    
    public void addToCost(BigDecimal cost) {
        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
            this.totalCost = this.totalCost.add(cost);
        }
    }
    
    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", vendorName='" + vendorName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", serviceCategory='" + serviceCategory + '\'' +
                ", status=" + status +
                ", performanceRating=" + performanceRating +
                ", totalServiceRequests=" + totalServiceRequests +
                ", completedRequests=" + completedRequests +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vendor vendor = (Vendor) o;
        return id != null && id.equals(vendor.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
