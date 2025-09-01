package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.UserRepository;
import com.hostel.repository.TicketEscalationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing ticket escalations based on time thresholds and SLA breaches
 */
@Service
public class EscalationService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketEscalationRepository escalationRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Check and process automatic escalations
     */
    public void processAutomaticEscalations() {
        processTimeBasedEscalations();
        processSLABreachEscalations();
    }

    /**
     * Process escalations based on time thresholds
     */
    private void processTimeBasedEscalations() {
        LocalDateTime now = LocalDateTime.now();
        
        // Emergency: 1 hour without assignment
        LocalDateTime emergencyThreshold = now.minusHours(1);
        List<Ticket> emergencyTickets = ticketRepository.findTicketsForTimeBasedEscalation(
            TicketPriority.EMERGENCY, TicketStatus.OPEN, emergencyThreshold);
        
        for (Ticket ticket : emergencyTickets) {
            escalateTicket(ticket, "Emergency ticket unassigned for over 1 hour", EscalationLevel.EMERGENCY_UNASSIGNED);
        }

        // High: 4 hours without progress
        LocalDateTime highThreshold = now.minusHours(4);
        List<Ticket> highTickets = ticketRepository.findTicketsForProgressEscalation(
            TicketPriority.HIGH, List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS), highThreshold);
        
        for (Ticket ticket : highTickets) {
            escalateTicket(ticket, "High priority ticket without progress for over 4 hours", EscalationLevel.HIGH_NO_PROGRESS);
        }

        // Medium: 24 hours without assignment
        LocalDateTime mediumThreshold = now.minusHours(24);
        List<Ticket> mediumTickets = ticketRepository.findTicketsForTimeBasedEscalation(
            TicketPriority.MEDIUM, TicketStatus.OPEN, mediumThreshold);
        
        for (Ticket ticket : mediumTickets) {
            escalateTicket(ticket, "Medium priority ticket unassigned for over 24 hours", EscalationLevel.MEDIUM_UNASSIGNED);
        }

        // Low: 72 hours without assignment
        LocalDateTime lowThreshold = now.minusHours(72);
        List<Ticket> lowTickets = ticketRepository.findTicketsForTimeBasedEscalation(
            TicketPriority.LOW, TicketStatus.OPEN, lowThreshold);
        
        for (Ticket ticket : lowTickets) {
            escalateTicket(ticket, "Low priority ticket unassigned for over 72 hours", EscalationLevel.LOW_UNASSIGNED);
        }
    }

    /**
     * Process escalations based on SLA breaches
     */
    private void processSLABreachEscalations() {
        LocalDateTime now = LocalDateTime.now();
        List<Ticket> breachedTickets = ticketRepository.findTicketsWithSLABreach(now);
        
        for (Ticket ticket : breachedTickets) {
            if (!hasRecentEscalation(ticket, EscalationLevel.SLA_BREACH)) {
                escalateTicket(ticket, "Ticket has breached SLA", EscalationLevel.SLA_BREACH);
            }
        }
    }

    /**
     * Escalate a ticket to the next level
     */
    public void escalateTicket(Ticket ticket, String reason, EscalationLevel level) {
        // Find escalation target based on current assignment and level
        User escalationTarget = findEscalationTarget(ticket, level);
        
        if (escalationTarget == null) {
            // No escalation target found, log and notify admins
            notificationService.sendAdminNotification(
                "Escalation Failed",
                String.format("Could not find escalation target for ticket %s", ticket.getTicketNumber()),
                NotificationType.SYSTEM_ALERT,
                ticket
            );
            return;
        }

        // Create escalation record
        TicketEscalation escalation = new TicketEscalation();
        escalation.setTicket(ticket);
        escalation.setEscalatedFrom(ticket.getAssignedTo());
        escalation.setEscalatedTo(escalationTarget);
        escalation.setEscalationLevel(level.getLevel());
        escalation.setReason(reason);
        escalation.setEscalatedAt(LocalDateTime.now());
        escalation.setIsAutoEscalated(true);
        
        escalationRepository.save(escalation);

        // Update ticket assignment and priority if needed
        if (level == EscalationLevel.EMERGENCY_UNASSIGNED || level == EscalationLevel.SLA_BREACH) {
            // For critical escalations, increase priority
            if (ticket.getPriority() != TicketPriority.EMERGENCY) {
                ticket.setPriority(TicketPriority.HIGH);
            }
        }

        ticket.setAssignedTo(escalationTarget);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        // Send notifications
        sendEscalationNotifications(ticket, escalation, escalationTarget);
    }

    /**
     * Find the appropriate escalation target based on hierarchy
     */
    private User findEscalationTarget(Ticket ticket, EscalationLevel level) {
        switch (level) {
            case EMERGENCY_UNASSIGNED:
            case HIGH_NO_PROGRESS:
                // Escalate to supervisor or available senior staff
                return findSupervisorOrSeniorStaff(ticket);
            
            case MEDIUM_UNASSIGNED:
            case LOW_UNASSIGNED:
                // Escalate to any available staff member
                return findAvailableStaff(ticket);
            
            case SLA_BREACH:
                // Escalate to department head or admin
                return findDepartmentHeadOrAdmin();
            
            default:
                return null;
        }
    }

    /**
     * Find supervisor or senior staff for escalation
     */
    private User findSupervisorOrSeniorStaff(Ticket ticket) {
        // First try to find a supervisor in the same vertical
        if (ticket.getAssignedTo() != null && ticket.getAssignedTo().getStaffVertical() != null) {
            List<User> supervisors = userRepository.findSupervisorsByVertical(ticket.getAssignedTo().getStaffVertical());
            if (!supervisors.isEmpty()) {
                return supervisors.get(0); // Return first available supervisor
            }
        }
        
        // Fallback to any admin
        return findDepartmentHeadOrAdmin();
    }

    /**
     * Find available staff member for escalation
     */
    private User findAvailableStaff(Ticket ticket) {
        // Find staff with lowest current workload
        List<User> availableStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        
        User leastBusyStaff = null;
        int minWorkload = Integer.MAX_VALUE;
        
        for (User staff : availableStaff) {
            int currentWorkload = ticketRepository.countByAssignedToAndStatusIn(
                staff, List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS));
            
            if (currentWorkload < minWorkload) {
                minWorkload = currentWorkload;
                leastBusyStaff = staff;
            }
        }
        
        return leastBusyStaff;
    }

    /**
     * Find department head or admin for critical escalations
     */
    private User findDepartmentHeadOrAdmin() {
        List<User> admins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        return admins.isEmpty() ? null : admins.get(0);
    }

    /**
     * Check if ticket has recent escalation of the given level
     */
    private boolean hasRecentEscalation(Ticket ticket, EscalationLevel level) {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        List<TicketEscalation> recentEscalations = escalationRepository.findByTicketAndEscalationLevelAndEscalatedAtAfter(
            ticket, level.getLevel(), twentyFourHoursAgo);
        
        return !recentEscalations.isEmpty();
    }

    /**
     * Send notifications for escalation
     */
    private void sendEscalationNotifications(Ticket ticket, TicketEscalation escalation, User escalationTarget) {
        String message = String.format(
            "Ticket %s (%s) has been escalated to you. Reason: %s",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            escalation.getReason()
        );

        // Notify escalation target
        notificationService.sendNotification(
            escalationTarget,
            "Ticket Escalated",
            message,
            NotificationType.ESCALATION,
            ticket
        );

        // Notify original assignee (if exists)
        if (escalation.getEscalatedFrom() != null) {
            String originalAssigneeMessage = String.format(
                "Ticket %s (%s) has been escalated from you to %s. Reason: %s",
                ticket.getTicketNumber(),
                ticket.getTitle(),
                escalationTarget.getFullName(),
                escalation.getReason()
            );
            
            notificationService.sendNotification(
                escalation.getEscalatedFrom(),
                "Ticket Escalated",
                originalAssigneeMessage,
                NotificationType.ESCALATION,
                ticket
            );
        }

        // Notify admins
        notificationService.sendAdminNotification(
            "Ticket Escalated",
            message,
            NotificationType.ESCALATION,
            ticket
        );
    }

    /**
     * Escalation levels enum
     */
    public enum EscalationLevel {
        EMERGENCY_UNASSIGNED(1),
        HIGH_NO_PROGRESS(2),
        MEDIUM_UNASSIGNED(3),
        LOW_UNASSIGNED(4),
        SLA_BREACH(5);

        private final int level;

        EscalationLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}
