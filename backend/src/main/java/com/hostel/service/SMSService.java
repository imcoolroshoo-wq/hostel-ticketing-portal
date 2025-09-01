package com.hostel.service;

import com.hostel.entity.NotificationType;
import com.hostel.entity.Ticket;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * SMS Service for sending notification SMS
 * Implementation of SMS notifications as per Product Design Document
 * Note: This is a placeholder implementation. In production, integrate with actual SMS providers.
 */
@Service
public class SMSService {

    /**
     * Send notification SMS
     */
    @Async
    public CompletableFuture<Void> sendNotificationSMS(String phoneNumber, String message, 
                                                      NotificationType type, Ticket relatedTicket) {
        try {
            // TODO: Integrate with actual SMS provider (Twilio, AWS SNS, etc.)
            // For now, this is a mock implementation
            
            String formattedMessage = formatSMSMessage(message, type, relatedTicket);
            
            // Mock SMS sending logic
            System.out.println("MOCK SMS to " + phoneNumber + ": " + formattedMessage);
            
            // Simulate SMS delivery delay
            Thread.sleep(1000);
            
            // In production, you would implement:
            // 1. SMS provider integration (Twilio, AWS SNS, etc.)
            // 2. Delivery status tracking
            // 3. Retry logic for failed sends
            // 4. Rate limiting
            // 5. Cost tracking
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Format SMS message for optimal delivery
     */
    private String formatSMSMessage(String message, NotificationType type, Ticket relatedTicket) {
        StringBuilder sms = new StringBuilder();
        
        // Add prefix based on type
        switch (type) {
            case EMERGENCY:
                sms.append("🚨 EMERGENCY: ");
                break;
            case SLA_BREACH:
                sms.append("⚠️ SLA BREACH: ");
                break;
            case ESCALATION:
                sms.append("📈 ESCALATED: ");
                break;
            case TICKET_ASSIGNMENT:
                sms.append("📋 ASSIGNED: ");
                break;
            default:
                sms.append("🔔 ");
                break;
        }
        
        // Add ticket number if available
        if (relatedTicket != null) {
            sms.append("[").append(relatedTicket.getTicketNumber()).append("] ");
        }
        
        // Add main message (truncated to fit SMS limits)
        String mainMessage = message;
        int maxLength = 160 - sms.length() - 20; // Reserve space for footer
        
        if (mainMessage.length() > maxLength) {
            mainMessage = mainMessage.substring(0, maxLength - 3) + "...";
        }
        
        sms.append(mainMessage);
        
        // Add footer
        sms.append(" - IIM Trichy Hostel");
        
        return sms.toString();
    }

    /**
     * Send bulk SMS
     */
    @Async
    public CompletableFuture<Void> sendBulkSMS(java.util.List<String> phoneNumbers, String message, NotificationType type) {
        for (String phoneNumber : phoneNumbers) {
            try {
                sendNotificationSMS(phoneNumber, message, type, null);
                // Add delay between messages to avoid rate limiting
                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("Failed to send bulk SMS to " + phoneNumber + ": " + e.getMessage());
            }
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Simple validation - in production, use more sophisticated validation
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        return cleaned.length() >= 10 && cleaned.length() <= 15;
    }

    /**
     * Format phone number for SMS delivery
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number: " + phoneNumber);
        }
        
        // Remove all non-numeric characters except +
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Add country code if not present (assuming India +91)
        if (!cleaned.startsWith("+")) {
            if (cleaned.startsWith("91") && cleaned.length() == 12) {
                cleaned = "+" + cleaned;
            } else if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned;
            }
        }
        
        return cleaned;
    }
}
