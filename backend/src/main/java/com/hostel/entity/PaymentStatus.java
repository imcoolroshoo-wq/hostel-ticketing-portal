package com.hostel.entity;

/**
 * Enum representing payment status for vendor service requests
 */
public enum PaymentStatus {
    
    /**
     * Payment is pending
     */
    PENDING("Pending", "Payment is pending", "⏳"),
    
    /**
     * Invoice has been received
     */
    INVOICE_RECEIVED("Invoice Received", "Invoice has been received", "📄"),
    
    /**
     * Payment has been approved for processing
     */
    APPROVED("Approved", "Payment has been approved", "✅"),
    
    /**
     * Payment is being processed
     */
    PROCESSING("Processing", "Payment is being processed", "🔄"),
    
    /**
     * Payment has been completed
     */
    PAID("Paid", "Payment has been completed", "💰"),
    
    /**
     * Payment has been rejected
     */
    REJECTED("Rejected", "Payment has been rejected", "❌"),
    
    /**
     * Payment is disputed
     */
    DISPUTED("Disputed", "Payment is disputed", "⚠️"),
    
    /**
     * Payment has been refunded
     */
    REFUNDED("Refunded", "Payment has been refunded", "↩️");
    
    private final String displayName;
    private final String description;
    private final String icon;
    
    PaymentStatus(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    /**
     * Check if payment is complete
     */
    public boolean isComplete() {
        return this == PAID || this == REFUNDED;
    }
    
    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return this == PENDING || this == INVOICE_RECEIVED || this == APPROVED || this == PROCESSING;
    }
    
    /**
     * Check if payment has issues
     */
    public boolean hasIssues() {
        return this == REJECTED || this == DISPUTED;
    }
    
    /**
     * Get the color class for UI display
     */
    public String getColorClass() {
        switch (this) {
            case PENDING:
                return "secondary";
            case INVOICE_RECEIVED:
                return "info";
            case APPROVED:
                return "primary";
            case PROCESSING:
                return "warning";
            case PAID:
                return "success";
            case REJECTED:
                return "error";
            case DISPUTED:
                return "warning";
            case REFUNDED:
                return "info";
            default:
                return "default";
        }
    }
}
