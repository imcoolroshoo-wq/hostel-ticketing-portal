package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Escalation Service implementing multi-tier escalation hierarchy
 * as per IIM Trichy Product Design Document Section 5.3
 */
@Service
@Transactional
public class EnhancedEscalationService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TicketEscalationRepository escalationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EnhancedNotificationService notificationService;
    
    @Autowired
    private TicketAssignmentService assignmentService;
    
    @Autowired
    private SystemConfigurationService configService;
    
    /**
     * Automatically check for and process escalations
     * Runs every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes
    public void processAutomaticEscalations() {
        try {
            List<Ticket> candidateTickets = findEscalationCandidates();
            
            for (Ticket ticket : candidateTickets) {
                processTicketEscalation(ticket);
            }
        } catch (Exception e) {
            System.err.println("Error processing automatic escalations: " + e.getMessage());
        }
    }
    
    /**
     * Process escalation for a specific ticket
     */
    public EscalationResult processTicketEscalation(Ticket ticket) {
        try {
            EscalationLevel currentLevel = determineCurrentEscalationLevel(ticket);
            EscalationReason reason = determineEscalationReason(ticket, currentLevel);
            
            if (reason == EscalationReason.NO_ESCALATION_NEEDED) {
                return new EscalationResult(false, "No escalation needed", currentLevel, null);
            }
            
            EscalationLevel nextLevel = currentLevel.getNextLevel();
            if (nextLevel == null) {
                return new EscalationResult(false, "Maximum escalation level reached", currentLevel, null);
            }
            
            return performEscalation(ticket, currentLevel, nextLevel, reason);
            
        } catch (Exception e) {
            return new EscalationResult(false, "Error during escalation: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Manually escalate a ticket
     */
    public EscalationResult manualEscalation(Ticket ticket, User escalatedBy, String reason) {
        EscalationLevel currentLevel = determineCurrentEscalationLevel(ticket);
        EscalationLevel nextLevel = currentLevel.getNextLevel();
        
        if (nextLevel == null) {
            return new EscalationResult(false, "Maximum escalation level reached", currentLevel, null);
        }
        
        EscalationReason escalationReason = EscalationReason.MANUAL_ESCALATION;
        
        return performEscalation(ticket, currentLevel, nextLevel, escalationReason, escalatedBy, reason);
    }
    
    /**
     * Perform the actual escalation
     */
    private EscalationResult performEscalation(Ticket ticket, EscalationLevel fromLevel, 
                                             EscalationLevel toLevel, EscalationReason reason) {
        return performEscalation(ticket, fromLevel, toLevel, reason, null, null);
    }
    
    /**
     * Perform the actual escalation with optional manual parameters
     */
    private EscalationResult performEscalation(Ticket ticket, EscalationLevel fromLevel, 
                                             EscalationLevel toLevel, EscalationReason reason,
                                             User escalatedBy, String manualReason) {
        try {
            // Create escalation record
            TicketEscalation escalation = new TicketEscalation();
            escalation.setTicket(ticket);
            escalation.setFromLevel(fromLevel);
            escalation.setToLevel(toLevel);
            escalation.setReason(reason);
            escalation.setEscalatedBy(escalatedBy);
            escalation.setNotes(manualReason);
            escalation.setEscalatedAt(LocalDateTime.now());
            escalation.setStatus(EscalationStatus.ACTIVE);
            
            escalationRepository.save(escalation);
            
            // Update ticket priority if needed
            updateTicketPriorityOnEscalation(ticket, toLevel);
            
            // Reassign ticket to appropriate level staff
            reassignTicketForEscalation(ticket, toLevel);
            
            // Send notifications
            sendEscalationNotifications(ticket, escalation);
            
            // Update ticket escalation level
            updateTicketEscalationLevel(ticket, toLevel);
            
            String message = String.format("Ticket escalated from %s to %s due to %s", 
                    fromLevel.getDisplayName(), toLevel.getDisplayName(), reason.getDisplayName());
            
            return new EscalationResult(true, message, toLevel, escalation);
            
        } catch (Exception e) {
            return new EscalationResult(false, "Escalation failed: " + e.getMessage(), fromLevel, null);
        }
    }
    
    /**
     * Find tickets that are candidates for escalation
     */
    private List<Ticket> findEscalationCandidates() {
        // Get all active tickets
        List<TicketStatus> activeStatuses = Arrays.asList(
            TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD
        );
        
        return ticketRepository.findByStatusIn(activeStatuses).stream()
                .filter(this::shouldTicketBeEscalated)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a ticket should be escalated
     */
    private boolean shouldTicketBeEscalated(Ticket ticket) {
        EscalationLevel currentLevel = determineCurrentEscalationLevel(ticket);
        EscalationReason reason = determineEscalationReason(ticket, currentLevel);
        
        return reason != EscalationReason.NO_ESCALATION_NEEDED && currentLevel.canEscalate();
    }
    
    /**
     * Determine current escalation level of a ticket
     */
    private EscalationLevel determineCurrentEscalationLevel(Ticket ticket) {
        // Get the latest escalation for this ticket
        Optional<TicketEscalation> latestEscalation = escalationRepository
                .findFirstByTicketOrderByEscalatedAtDesc(ticket);
        
        if (latestEscalation.isPresent()) {
            return latestEscalation.get().getToLevel();
        }
        
        // If no escalation exists, determine based on assignment
        if (ticket.getAssignedTo() != null) {
            StaffVertical vertical = ticket.getAssignedTo().getStaffVertical();
            
            if (vertical != null) {
                switch (vertical) {
                    case BLOCK_SUPERVISOR:
                    case MAINTENANCE_SUPERVISOR:
                        return EscalationLevel.TEAM_LEAD;
                    case HOSTEL_WARDEN:
                    case ASSISTANT_WARDEN:
                        return EscalationLevel.DEPARTMENT_HEAD;
                    case CHIEF_WARDEN:
                    case ADMIN_OFFICER:
                        return EscalationLevel.HOSTEL_ADMINISTRATION;
                    default:
                        return EscalationLevel.STAFF_MEMBER;
                }
            }
        }
        
        return EscalationLevel.STAFF_MEMBER;
    }
    
    /**
     * Determine if escalation is needed and why
     */
    private EscalationReason determineEscalationReason(Ticket ticket, EscalationLevel currentLevel) {
        LocalDateTime now = LocalDateTime.now();
        
        // Time-based escalation
        if (isTimeBasedEscalationNeeded(ticket, currentLevel, now)) {
            return EscalationReason.TIME_THRESHOLD_EXCEEDED;
        }
        
        // SLA breach escalation
        if (ticket.getSlaBreachTime() != null && now.isAfter(ticket.getSlaBreachTime())) {
            return EscalationReason.SLA_BREACH;
        }
        
        // Priority-based escalation
        if (isPriorityBasedEscalationNeeded(ticket, currentLevel)) {
            return EscalationReason.HIGH_PRIORITY;
        }
        
        // Staff unavailability escalation
        if (isStaffUnavailabilityEscalationNeeded(ticket)) {
            return EscalationReason.STAFF_UNAVAILABLE;
        }
        
        // Customer complaint escalation
        if (hasRecentCustomerComplaints(ticket)) {
            return EscalationReason.CUSTOMER_COMPLAINT;
        }
        
        return EscalationReason.NO_ESCALATION_NEEDED;
    }
    
    /**
     * Check if time-based escalation is needed
     */
    private boolean isTimeBasedEscalationNeeded(Ticket ticket, EscalationLevel currentLevel, LocalDateTime now) {
        int thresholdHours = currentLevel.getEscalationThresholdHours(ticket.getPriority());
        
        // Get the time when ticket reached this level
        LocalDateTime levelStartTime = getLevelStartTime(ticket, currentLevel);
        
        return now.isAfter(levelStartTime.plusHours(thresholdHours));
    }
    
    /**
     * Get the time when ticket reached the current level
     */
    private LocalDateTime getLevelStartTime(Ticket ticket, EscalationLevel level) {
        if (level == EscalationLevel.STAFF_MEMBER) {
            return ticket.getAssignedAt() != null ? ticket.getAssignedAt() : ticket.getCreatedAt();
        }
        
        Optional<TicketEscalation> escalation = escalationRepository
                .findFirstByTicketAndToLevelOrderByEscalatedAtDesc(ticket, level);
        
        return escalation.map(TicketEscalation::getEscalatedAt)
                .orElse(ticket.getCreatedAt());
    }
    
    /**
     * Check if priority-based escalation is needed
     */
    private boolean isPriorityBasedEscalationNeeded(Ticket ticket, EscalationLevel currentLevel) {
        if (ticket.getPriority() == TicketPriority.EMERGENCY) {
            return currentLevel.getLevel() < EscalationLevel.DEPARTMENT_HEAD.getLevel();
        }
        
        if (ticket.getPriority() == TicketPriority.HIGH) {
            return currentLevel.getLevel() < EscalationLevel.TEAM_LEAD.getLevel();
        }
        
        return false;
    }
    
    /**
     * Check if staff unavailability escalation is needed
     */
    private boolean isStaffUnavailabilityEscalationNeeded(Ticket ticket) {
        if (ticket.getAssignedTo() == null) return false;
        
        // Check if assigned staff is inactive or overloaded
        User assignedStaff = ticket.getAssignedTo();
        
        if (!assignedStaff.getIsActive()) {
            return true;
        }
        
        // Check if staff is overloaded (simplified check)
        long activeTickets = ticketRepository.countByAssignedToAndStatusIn(
                assignedStaff,
                Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS)
        );
        
        return activeTickets > 10; // Threshold for overload
    }
    
    /**
     * Check if there are recent customer complaints
     */
    private boolean hasRecentCustomerComplaints(Ticket ticket) {
        // Check if ticket has been reopened recently
        return ticket.getStatus() == TicketStatus.REOPENED;
    }
    
    /**
     * Update ticket priority based on escalation level
     */
    private void updateTicketPriorityOnEscalation(Ticket ticket, EscalationLevel toLevel) {
        if (toLevel.isCritical() && ticket.getPriority().getLevel() < TicketPriority.HIGH.getLevel()) {
            ticket.setPriority(TicketPriority.HIGH);
            ticketRepository.save(ticket);
        }
    }
    
    /**
     * Reassign ticket to appropriate level staff
     */
    private void reassignTicketForEscalation(Ticket ticket, EscalationLevel toLevel) {
        StaffVertical[] targetRoles = toLevel.getNotificationRoles();
        
        if (targetRoles.length > 0) {
            List<User> availableStaff = userRepository.findByStaffVerticalInAndIsActiveTrue(
                    Arrays.asList(targetRoles));
            
            if (!availableStaff.isEmpty()) {
                // Use assignment service to find best staff member
                User newAssignee = findBestStaffForEscalation(availableStaff, ticket);
                
                if (newAssignee != null && !newAssignee.equals(ticket.getAssignedTo())) {
                    ticket.setAssignedTo(newAssignee);
                    ticket.setAssignedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                }
            }
        }
    }
    
    /**
     * Find best staff member for escalated ticket
     */
    private User findBestStaffForEscalation(List<User> availableStaff, Ticket ticket) {
        // Simple selection based on workload - can be enhanced
        return availableStaff.stream()
                .min(Comparator.comparingLong(staff -> 
                    ticketRepository.countByAssignedToAndStatusIn(
                        staff, Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS)
                    )
                ))
                .orElse(null);
    }
    
    /**
     * Send escalation notifications
     */
    private void sendEscalationNotifications(Ticket ticket, TicketEscalation escalation) {
        String title = String.format("Ticket Escalated to %s", escalation.getToLevel().getDisplayName());
        String message = String.format(
                "Ticket %s has been escalated to %s level due to %s. Immediate attention required.",
                ticket.getTicketNumber(),
                escalation.getToLevel().getDisplayName(),
                escalation.getReason().getDisplayName()
        );
        
        // Notify assigned staff
        if (ticket.getAssignedTo() != null) {
            notificationService.sendTicketEscalationNotification(ticket, escalation.getToLevel());
        }
        
        // Notify relevant administrators
        List<User> admins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        notificationService.sendBulkNotifications(title, message, admins, NotificationType.ESCALATION, ticket);
        
        // Notify ticket creator about escalation
        notificationService.sendTicketStatusNotification(ticket, ticket.getStatus(), 
                "Your ticket has been escalated for faster resolution");
    }
    
    /**
     * Update ticket escalation level (if you have such field)
     */
    private void updateTicketEscalationLevel(Ticket ticket, EscalationLevel level) {
        // This would update a field on the ticket entity if it exists
        // For now, we track escalation level through the TicketEscalation entity
    }
    
    /**
     * Get escalation history for a ticket
     */
    public List<TicketEscalation> getEscalationHistory(Ticket ticket) {
        return escalationRepository.findByTicketOrderByEscalatedAtDesc(ticket);
    }
    
    /**
     * Get current escalation status
     */
    public EscalationStatus getCurrentEscalationStatus(Ticket ticket) {
        List<TicketEscalation> escalations = getEscalationHistory(ticket);
        
        if (escalations.isEmpty()) {
            return null;
        }
        
        TicketEscalation latest = escalations.get(0);
        EscalationLevel currentLevel = latest.getToLevel();
        
        if (currentLevel.isCritical()) {
            return EscalationStatus.CRITICAL;
        } else if (currentLevel.getLevel() >= 2) {
            return EscalationStatus.ESCALATED;
        } else {
            return EscalationStatus.NORMAL;
        }
    }
    
    /**
     * Resolve escalation when ticket is resolved
     */
    public void resolveEscalation(Ticket ticket) {
        List<TicketEscalation> activeEscalations = escalationRepository
                .findByTicketAndStatus(ticket, EscalationStatus.ACTIVE);
        
        for (TicketEscalation escalation : activeEscalations) {
            escalation.setStatus(EscalationStatus.RESOLVED);
            escalation.setResolvedAt(LocalDateTime.now());
            escalationRepository.save(escalation);
        }
    }
    
    // Enums for escalation reasons and status
    
    public enum EscalationReason {
        TIME_THRESHOLD_EXCEEDED("Time Threshold Exceeded", "⏰"),
        SLA_BREACH("SLA Breach", "🚨"),
        HIGH_PRIORITY("High Priority Issue", "🔥"),
        STAFF_UNAVAILABLE("Staff Unavailable", "👥"),
        CUSTOMER_COMPLAINT("Customer Complaint", "😠"),
        MANUAL_ESCALATION("Manual Escalation", "👤"),
        NO_ESCALATION_NEEDED("No Escalation Needed", "✅");
        
        private final String displayName;
        private final String icon;
        
        EscalationReason(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    public enum EscalationStatus {
        NORMAL("Normal", "Normal operation level", "🟢"),
        ESCALATED("Escalated", "Ticket has been escalated", "🟡"),
        CRITICAL("Critical", "Critical escalation level", "🔴"),
        ACTIVE("Active", "Escalation is active", "🔄"),
        RESOLVED("Resolved", "Escalation has been resolved", "✅");
        
        private final String displayName;
        private final String description;
        private final String icon;
        
        EscalationStatus(String displayName, String description, String icon) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
    
    // Result class
    
    public static class EscalationResult {
        private final boolean success;
        private final String message;
        private final EscalationLevel level;
        private final TicketEscalation escalation;
        
        public EscalationResult(boolean success, String message, EscalationLevel level, TicketEscalation escalation) {
            this.success = success;
            this.message = message;
            this.level = level;
            this.escalation = escalation;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public EscalationLevel getLevel() { return level; }
        public TicketEscalation getEscalation() { return escalation; }
    }
}
