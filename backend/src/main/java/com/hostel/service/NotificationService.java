package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Simple Notification Service interface for sending notifications
 * Acts as a facade for the Enhanced Notification Service
 */
@Service
public class NotificationService {

    @Autowired
    private EnhancedNotificationService enhancedNotificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Send notification to a user
     */
    public void sendNotification(User user, String title, String message, NotificationType type, Ticket relatedTicket) {
        try {
            enhancedNotificationService.sendInAppNotification(user, title, message, type, relatedTicket);
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Send notification to admin users
     */
    public void sendAdminNotification(String title, String message, NotificationType type, Ticket relatedTicket) {
        try {
            List<User> admins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
            for (User admin : admins) {
                sendNotification(admin, title, message, type, relatedTicket);
            }
        } catch (Exception e) {
            System.err.println("Failed to send admin notification: " + e.getMessage());
        }
    }

    /**
     * Send notification to multiple users
     */
    public void sendBulkNotification(List<User> users, String title, String message, NotificationType type, Ticket relatedTicket) {
        try {
            enhancedNotificationService.sendBulkNotifications(title, message, users, type, relatedTicket);
        } catch (Exception e) {
            System.err.println("Failed to send bulk notification: " + e.getMessage());
        }
    }

    /**
     * Send system notification to all users
     */
    public void sendSystemNotification(String title, String message) {
        try {
            List<User> allUsers = userRepository.findByIsActiveTrue();
            enhancedNotificationService.sendSystemNotification(title, message, allUsers);
        } catch (Exception e) {
            System.err.println("Failed to send system notification: " + e.getMessage());
        }
    }

    /**
     * Send ticket assignment notification
     */
    public void sendTicketAssignmentNotification(Ticket ticket, User assignedTo) {
        try {
            enhancedNotificationService.sendTicketAssignmentNotification(ticket, assignedTo, ticket.getCreatedBy());
        } catch (Exception e) {
            System.err.println("Failed to send assignment notification: " + e.getMessage());
        }
    }

    /**
     * Send ticket status change notification
     */
    public void sendStatusChangeNotification(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        try {
            enhancedNotificationService.sendStatusChangeNotification(ticket, oldStatus, newStatus);
        } catch (Exception e) {
            System.err.println("Failed to send status change notification: " + e.getMessage());
        }
    }

    /**
     * Send resolution notification
     */
    public void sendResolutionNotification(Ticket ticket) {
        try {
            enhancedNotificationService.sendResolutionNotification(ticket, ticket.getCreatedBy());
        } catch (Exception e) {
            System.err.println("Failed to send resolution notification: " + e.getMessage());
        }
    }

    /**
     * Send escalation notification
     */
    public void sendEscalationNotification(Ticket ticket, TicketEscalation escalation, User escalatedTo) {
        try {
            enhancedNotificationService.sendEscalationNotification(ticket, escalation, escalatedTo);
        } catch (Exception e) {
            System.err.println("Failed to send escalation notification: " + e.getMessage());
        }
    }
}
