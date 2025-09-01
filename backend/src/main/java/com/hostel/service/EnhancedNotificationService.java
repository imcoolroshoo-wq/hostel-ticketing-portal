package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.NotificationRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced Notification Service implementing comprehensive notification functionality
 * as per IIM Trichy Hostel Ticket Management System Product Design Document
 */
@Service
@Transactional
public class EnhancedNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private SMSService smsService;

    /**
     * Send ticket assignment notification
     */
    @Async
    public CompletableFuture<Void> sendTicketAssignmentNotification(Ticket ticket, User assignedTo, User assignedBy) {
        String title = String.format("New Ticket Assigned: %s", ticket.getTicketNumber());
        String message = String.format(
            "You have been assigned a new ticket:\n\n" +
            "Ticket: %s\n" +
            "Title: %s\n" +
            "Priority: %s\n" +
            "Category: %s\n" +
            "Location: %s - %s\n" +
            "Assigned by: %s\n\n" +
            "Please review and start working on this ticket.",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getPriority().getDisplayName(),
            ticket.getEffectiveCategory(),
            ticket.getHostelBlock(),
            ticket.getRoomNumber(),
            assignedBy.getFullName()
        );

        // Send in-app notification
        sendInAppNotification(assignedTo, title, message, NotificationType.TICKET_ASSIGNMENT, ticket);

        // Send email notification
        if (assignedTo.getEmail() != null) {
            sendEmailNotification(assignedTo, title, message, NotificationType.TICKET_ASSIGNMENT, ticket);
        }

        // Send high-priority SMS for emergency tickets
        if (ticket.getPriority() == TicketPriority.EMERGENCY && assignedTo.getPhone() != null) {
            sendSMSNotification(assignedTo, title, message, NotificationType.EMERGENCY, ticket);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send status update notification
     */
    @Async
    public CompletableFuture<Void> sendStatusUpdateNotification(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, User updatedBy) {
        User ticketCreator = ticket.getCreatedBy();
        
        String title = String.format("Ticket Status Updated: %s", ticket.getTicketNumber());
        String message = String.format(
            "Your ticket status has been updated:\n\n" +
            "Ticket: %s\n" +
            "Title: %s\n" +
            "Previous Status: %s\n" +
            "New Status: %s\n" +
            "Updated by: %s\n" +
            "Updated at: %s",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            oldStatus.getDisplayName(),
            newStatus.getDisplayName(),
            updatedBy.getFullName(),
            LocalDateTime.now()
        );

        // Notify ticket creator
        sendInAppNotification(ticketCreator, title, message, NotificationType.STATUS_UPDATE, ticket);
        
        if (ticketCreator.getEmail() != null) {
            sendEmailNotification(ticketCreator, title, message, NotificationType.STATUS_UPDATE, ticket);
        }

        // Notify assigned staff if different from updater
        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(updatedBy.getId())) {
            sendInAppNotification(ticket.getAssignedTo(), title, message, NotificationType.STATUS_UPDATE, ticket);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send SLA warning notification
     */
    @Async
    public CompletableFuture<Void> sendSLAWarningNotification(Ticket ticket) {
        String title = String.format("SLA Warning: %s", ticket.getTicketNumber());
        String message = String.format(
            "Ticket %s (%s) is approaching SLA breach.\n\n" +
            "Expected resolution: %s\n" +
            "Current status: %s\n" +
            "Priority: %s\n\n" +
            "Please take immediate action to resolve this ticket.",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getEstimatedResolutionTime(),
            ticket.getStatus().getDisplayName(),
            ticket.getPriority().getDisplayName()
        );

        // Notify assigned staff
        if (ticket.getAssignedTo() != null) {
            sendInAppNotification(ticket.getAssignedTo(), title, message, NotificationType.SLA_WARNING, ticket);
            sendEmailNotification(ticket.getAssignedTo(), title, message, NotificationType.SLA_WARNING, ticket);
        }

        // Notify supervisors
        List<User> supervisors = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        for (User supervisor : supervisors) {
            sendInAppNotification(supervisor, title, message, NotificationType.SLA_WARNING, ticket);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send SLA breach notification
     */
    @Async
    public CompletableFuture<Void> sendSLABreachNotification(Ticket ticket) {
        String title = String.format("SLA BREACH ALERT: %s", ticket.getTicketNumber());
        String message = String.format(
            "URGENT: Ticket %s (%s) has breached SLA!\n\n" +
            "Expected resolution was: %s\n" +
            "Current time: %s\n" +
            "Status: %s\n" +
            "Priority: %s\n\n" +
            "IMMEDIATE ACTION REQUIRED!",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getEstimatedResolutionTime(),
            LocalDateTime.now(),
            ticket.getStatus().getDisplayName(),
            ticket.getPriority().getDisplayName()
        );

        // High-priority notifications to all relevant parties
        List<User> notifyUsers = new ArrayList<>();
        
        // Add assigned staff
        if (ticket.getAssignedTo() != null) {
            notifyUsers.add(ticket.getAssignedTo());
        }
        
        // Add all supervisors and admins
        notifyUsers.addAll(userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN));

        for (User user : notifyUsers) {
            sendInAppNotification(user, title, message, NotificationType.SLA_BREACH, ticket);
            sendEmailNotification(user, title, message, NotificationType.SLA_BREACH, ticket);
            
            // Send SMS for critical SLA breaches
            if (user.getPhone() != null) {
                sendSMSNotification(user, title, message, NotificationType.SLA_BREACH, ticket);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send escalation notification
     */
    @Async
    public CompletableFuture<Void> sendEscalationNotification(Ticket ticket, TicketEscalation escalation, User escalatedTo) {
        String title = String.format("Ticket Escalated: %s", ticket.getTicketNumber());
        String message = String.format(
            "A ticket has been escalated to you:\n\n" +
            "Ticket: %s\n" +
            "Title: %s\n" +
            "Escalation Level: %d\n" +
            "Reason: %s\n" +
            "Previous Assignee: %s\n" +
            "Priority: %s\n\n" +
            "Please review and take appropriate action.",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            escalation.getEscalationLevel(),
            escalation.getReason(),
            escalation.getEscalatedFrom() != null ? escalation.getEscalatedFrom().getFullName() : "Unassigned",
            ticket.getPriority().getDisplayName()
        );

        sendInAppNotification(escalatedTo, title, message, NotificationType.ESCALATION, ticket);
        sendEmailNotification(escalatedTo, title, message, NotificationType.ESCALATION, ticket);

        // Notify previous assignee about escalation
        if (escalation.getEscalatedFrom() != null) {
            String escalatedFromMessage = String.format(
                "Your ticket %s (%s) has been escalated to %s.\n\nReason: %s",
                ticket.getTicketNumber(),
                ticket.getTitle(),
                escalatedTo.getFullName(),
                escalation.getReason()
            );
            sendInAppNotification(escalation.getEscalatedFrom(), 
                "Ticket Escalated", escalatedFromMessage, NotificationType.ESCALATION, ticket);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send resolution notification
     */
    @Async
    public CompletableFuture<Void> sendResolutionNotification(Ticket ticket, User resolvedBy) {
        String title = String.format("Ticket Resolved: %s", ticket.getTicketNumber());
        String message = String.format(
            "Your ticket has been resolved:\n\n" +
            "Ticket: %s\n" +
            "Title: %s\n" +
            "Resolved by: %s\n" +
            "Resolution time: %s\n\n" +
            "Please review the work and provide feedback. " +
            "If you're satisfied with the resolution, the ticket will be automatically closed in 24 hours.",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            resolvedBy.getFullName(),
            LocalDateTime.now()
        );

        User ticketCreator = ticket.getCreatedBy();
        sendInAppNotification(ticketCreator, title, message, NotificationType.RESOLUTION, ticket);
        sendEmailNotification(ticketCreator, title, message, NotificationType.RESOLUTION, ticket);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send feedback request notification
     */
    @Async
    public CompletableFuture<Void> sendFeedbackRequestNotification(Ticket ticket) {
        String title = String.format("Feedback Request: %s", ticket.getTicketNumber());
        String message = String.format(
            "Please provide feedback for your resolved ticket:\n\n" +
            "Ticket: %s\n" +
            "Title: %s\n" +
            "Resolved at: %s\n\n" +
            "Your feedback helps us improve our service quality.",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getResolvedAt()
        );

        User ticketCreator = ticket.getCreatedBy();
        sendInAppNotification(ticketCreator, title, message, NotificationType.FEEDBACK_REQUEST, ticket);

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send maintenance schedule notification
     */
    @Async
    public CompletableFuture<Void> sendMaintenanceNotification(MaintenanceSchedule schedule, List<User> affectedUsers) {
        String title = String.format("Scheduled Maintenance: %s", schedule.getMaintenanceType());
        String message = String.format(
            "Scheduled maintenance notification:\n\n" +
            "Type: %s\n" +
            "Asset: %s\n" +
            "Scheduled: %s\n" +
            "Estimated Duration: %d minutes\n" +
            "Description: %s\n\n" +
            "Please plan accordingly for any service interruptions.",
            schedule.getMaintenanceType(),
            schedule.getAsset().getName(),
            schedule.getNextDueDate(),
            schedule.getEstimatedDurationMinutes(),
            schedule.getDescription()
        );

        for (User user : affectedUsers) {
            sendInAppNotification(user, title, message, NotificationType.MAINTENANCE, null);
            sendEmailNotification(user, title, message, NotificationType.MAINTENANCE, null);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send emergency notification
     */
    @Async
    public CompletableFuture<Void> sendEmergencyNotification(String title, String message, List<User> recipients) {
        for (User user : recipients) {
            sendInAppNotification(user, title, message, NotificationType.EMERGENCY, null);
            sendEmailNotification(user, title, message, NotificationType.EMERGENCY, null);
            
            if (user.getPhone() != null) {
                sendSMSNotification(user, title, message, NotificationType.EMERGENCY, null);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send system notification
     */
    @Async
    public CompletableFuture<Void> sendSystemNotification(String title, String message, List<User> recipients) {
        for (User user : recipients) {
            sendInAppNotification(user, title, message, NotificationType.SYSTEM, null);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send bulk notifications
     */
    @Async
    public CompletableFuture<Void> sendBulkNotifications(String title, String message, 
                                                         List<User> recipients, NotificationType type, 
                                                         Ticket relatedTicket) {
        for (User user : recipients) {
            sendInAppNotification(user, title, message, type, relatedTicket);
            
            if (!type.canBeDelayed() && user.getEmail() != null) {
                sendEmailNotification(user, title, message, type, relatedTicket);
            }
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send in-app notification
     */
    public void sendInAppNotification(User user, String title, String message, 
                                     NotificationType type, Ticket relatedTicket) {
        try {
            Notification notification = new Notification(user, title, message, type, relatedTicket);
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Failed to send in-app notification: " + e.getMessage());
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(User user, String title, String message, 
                                     NotificationType type, Ticket relatedTicket) {
        try {
            if (emailService != null) {
                emailService.sendNotificationEmail(user.getEmail(), title, message, type, relatedTicket);
            }
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }

    /**
     * Send SMS notification
     */
    private void sendSMSNotification(User user, String title, String message, 
                                   NotificationType type, Ticket relatedTicket) {
        try {
            if (smsService != null && user.getPhone() != null) {
                String shortMessage = String.format("%s: %s", title, 
                    message.length() > 100 ? message.substring(0, 97) + "..." : message);
                smsService.sendNotificationSMS(user.getPhone(), shortMessage, type, relatedTicket);
            }
        } catch (Exception e) {
            // Log error but don't fail the entire operation
            System.err.println("Failed to send SMS notification: " + e.getMessage());
        }
    }

    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId);
        if (notification != null && !notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * Mark all notifications as read for user
     */
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        LocalDateTime readTime = LocalDateTime.now();
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(readTime);
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Delete old notifications (cleanup job)
     */
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        notificationRepository.deleteByCreatedAtBefore(cutoffDate);
    }
}
