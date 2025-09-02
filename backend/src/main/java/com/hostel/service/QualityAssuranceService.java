package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.UserRepository;
import com.hostel.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Quality Assurance Service implementing resolution verification and quality metrics
 * as per IIM Trichy Product Design Document Section 5.5
 */
@Service
@Transactional
public class QualityAssuranceService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketAttachmentRepository attachmentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PhotoDocumentationService photoDocumentationService;

    /**
     * Process ticket resolution verification workflow
     * Implements the 24-hour verification window from PDD
     */
    public void processResolutionVerification(Ticket ticket) {
        if (!ticket.getStatus().equals(TicketStatus.RESOLVED)) {
            throw new IllegalStateException("Ticket must be in RESOLVED status for verification");
        }

        // Send notification to student for verification
        String message = String.format(
            "Your ticket %s (%s) has been marked as resolved. " +
            "Please review the work completed and confirm if the issue is satisfactorily resolved. " +
            "You have 24 hours to respond. If no response is received, the ticket will be automatically closed.",
            ticket.getTicketNumber(),
            ticket.getTitle()
        );

        notificationService.sendNotification(
            ticket.getCreatedBy(),
            "Ticket Resolution - Verification Required",
            message,
            NotificationType.RESOLUTION_VERIFICATION,
            ticket
        );

        // Schedule auto-closure after 24 hours (would be handled by scheduled task)
        ticket.setEstimatedResolutionTime(LocalDateTime.now().plusHours(24));
        ticketRepository.save(ticket);
    }

    /**
     * Handle student feedback on resolved ticket
     * Implements satisfaction rating and feedback collection
     */
    public boolean processStudentFeedback(Ticket ticket, Integer satisfactionRating, String feedback, boolean isResolved) {
        if (!ticket.getStatus().equals(TicketStatus.RESOLVED)) {
            throw new IllegalStateException("Can only provide feedback on resolved tickets");
        }

        // Validate satisfaction rating
        if (satisfactionRating != null && (satisfactionRating < 1 || satisfactionRating > 5)) {
            throw new IllegalArgumentException("Satisfaction rating must be between 1 and 5");
        }

        ticket.setSatisfactionRating(satisfactionRating);
        ticket.setFeedback(feedback);

        if (isResolved) {
            // Student confirms resolution - close ticket
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());
            ticket.setActualResolutionTime(LocalDateTime.now());
            
            // Notify assigned staff of successful closure
            if (ticket.getAssignedTo() != null) {
                notificationService.sendNotification(
                    ticket.getAssignedTo(),
                    "Ticket Closed Successfully",
                    String.format("Ticket %s has been closed with satisfaction rating: %d/5",
                        ticket.getTicketNumber(), satisfactionRating != null ? satisfactionRating : 0),
                    NotificationType.TICKET_CLOSED,
                    ticket
                );
            }
        } else {
            // Student is not satisfied - reopen ticket
            ticket.setStatus(TicketStatus.REOPENED);
            
            // Create comment explaining reopening
            String reopenReason = String.format(
                "Ticket reopened by student due to unsatisfactory resolution. " +
                "Satisfaction rating: %d/5. Feedback: %s",
                satisfactionRating != null ? satisfactionRating : 0,
                feedback != null ? feedback : "No feedback provided"
            );

            // Notify supervisor for review
            notifySupervisionForReopening(ticket, reopenReason);
        }

        ticketRepository.save(ticket);
        return isResolved;
    }

    /**
     * Auto-close tickets after 24-hour verification window
     * Called by scheduled task
     */
    public void autoCloseVerifiedTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Ticket> ticketsToClose = ticketRepository.findResolvedTicketsOlderThan(cutoff);

        for (Ticket ticket : ticketsToClose) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setClosedAt(LocalDateTime.now());
            ticket.setActualResolutionTime(LocalDateTime.now());
            
            // Set default satisfaction rating if not provided
            if (ticket.getSatisfactionRating() == null) {
                ticket.setSatisfactionRating(3); // Neutral rating
                ticket.setFeedback("Auto-closed - no feedback provided within 24 hours");
            }

            ticketRepository.save(ticket);

            // Notify student of auto-closure
            notificationService.sendNotification(
                ticket.getCreatedBy(),
                "Ticket Auto-Closed",
                String.format("Ticket %s has been automatically closed as no response was received within 24 hours.",
                    ticket.getTicketNumber()),
                NotificationType.TICKET_CLOSED,
                ticket
            );
        }
    }

    /**
     * Calculate quality metrics for a staff member
     * Implements metrics from PDD Section 5.5.2
     */
    public QualityMetrics calculateStaffQualityMetrics(User staff) {
        if (!staff.getRole().equals(UserRole.STAFF)) {
            throw new IllegalArgumentException("Quality metrics can only be calculated for staff members");
        }

        List<Ticket> allTickets = ticketRepository.findByAssignedTo(staff);
        List<Ticket> closedTickets = allTickets.stream()
            .filter(ticket -> ticket.getStatus().equals(TicketStatus.CLOSED))
            .collect(Collectors.toList());

        // First-Time Resolution Rate
        long firstTimeResolutions = closedTickets.stream()
            .filter(this::isFirstTimeResolution)
            .count();
        double firstTimeResolutionRate = closedTickets.isEmpty() ? 0.0 : 
            (double) firstTimeResolutions / closedTickets.size() * 100;

        // Average Satisfaction Score
        double avgSatisfactionScore = closedTickets.stream()
            .filter(ticket -> ticket.getSatisfactionRating() != null)
            .mapToDouble(Ticket::getSatisfactionRating)
            .average()
            .orElse(0.0);

        // Resolution Time Adherence
        long onTimeResolutions = closedTickets.stream()
            .filter(this::isResolvedOnTime)
            .count();
        double resolutionTimeAdherence = closedTickets.isEmpty() ? 0.0 :
            (double) onTimeResolutions / closedTickets.size() * 100;

        // Escalation Rate
        long escalatedTickets = allTickets.stream()
            .filter(this::hasBeenEscalated)
            .count();
        double escalationRate = allTickets.isEmpty() ? 0.0 :
            (double) escalatedTickets / allTickets.size() * 100;

        // Recurring Issue Rate
        long recurringIssues = closedTickets.stream()
            .filter(this::isRecurringIssue)
            .count();
        double recurringIssueRate = closedTickets.isEmpty() ? 0.0 :
            (double) recurringIssues / closedTickets.size() * 100;

        return new QualityMetrics(
            firstTimeResolutionRate,
            avgSatisfactionScore,
            resolutionTimeAdherence,
            escalationRate,
            recurringIssueRate
        );
    }

    /**
     * Validate photo documentation requirements
     * Implements photo verification from PDD Section 12.3.1
     */
    public boolean validatePhotoDocumentation(Ticket ticket) {
        if (!requiresPhotoDocumentation(ticket)) {
            return true; // Not required for this ticket type
        }

        List<TicketAttachment> attachments = attachmentRepository.findByTicketAndAttachmentType(
            ticket, AttachmentType.BEFORE_PHOTO);
        List<TicketAttachment> afterPhotos = attachmentRepository.findByTicketAndAttachmentType(
            ticket, AttachmentType.AFTER_PHOTO);

        boolean hasBeforePhoto = !attachments.isEmpty();
        boolean hasAfterPhoto = !afterPhotos.isEmpty();

        if (!hasBeforePhoto || !hasAfterPhoto) {
            // Flag for quality review
            flagForQualityReview(ticket, "Missing required photo documentation");
            return false;
        }

        return true;
    }

    /**
     * Detect and flag unusual resolution times
     * Implements time validation from PDD Section 12.3.1
     */
    public void validateResolutionTime(Ticket ticket) {
        if (ticket.getStartedAt() == null || ticket.getResolvedAt() == null) {
            return;
        }

        long actualHours = java.time.Duration.between(ticket.getStartedAt(), ticket.getResolvedAt()).toHours();
        int expectedHours = getExpectedResolutionHours(ticket);

        // Flag if resolution time is unusually short (< 25% of expected) or long (> 200% of expected)
        if (actualHours < expectedHours * 0.25) {
            flagForQualityReview(ticket, 
                String.format("Unusually short resolution time: %d hours (expected: %d hours)", 
                    actualHours, expectedHours));
        } else if (actualHours > expectedHours * 2.0) {
            flagForQualityReview(ticket, 
                String.format("Unusually long resolution time: %d hours (expected: %d hours)", 
                    actualHours, expectedHours));
        }
    }

    /**
     * Detect recurring issues and flag for preventive action
     */
    public void detectRecurringIssues() {
        // Find tickets with similar titles/descriptions in same location within 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Ticket> recentTickets = ticketRepository.findByCreatedAtAfter(thirtyDaysAgo);

        // Group by location and category
        Map<String, List<Ticket>> locationGroups = recentTickets.stream()
            .collect(Collectors.groupingBy(ticket -> 
                ticket.getHostelBlock() + "-" + ticket.getRoomNumber()));

        for (Map.Entry<String, List<Ticket>> entry : locationGroups.entrySet()) {
            List<Ticket> locationTickets = entry.getValue();
            if (locationTickets.size() >= 3) { // 3 or more tickets in same location
                // Check for similar categories
                Map<String, Long> categoryCount = locationTickets.stream()
                    .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));

                for (Map.Entry<String, Long> categoryEntry : categoryCount.entrySet()) {
                    if (categoryEntry.getValue() >= 3) {
                        flagRecurringIssue(entry.getKey(), categoryEntry.getKey(), locationTickets);
                    }
                }
            }
        }
    }

    // Helper methods

    private void notifySupervisionForReopening(Ticket ticket, String reason) {
        // Find supervisor or escalate to admin
        List<User> supervisors = userRepository.findByRoleAndStaffVerticalInAndIsActiveTrue(
            UserRole.STAFF, 
            Arrays.asList(StaffVertical.BLOCK_SUPERVISOR, StaffVertical.HOSTEL_WARDEN)
        );

        User supervisor = supervisors.isEmpty() ? 
            userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN).stream().findFirst().orElse(null) :
            supervisors.get(0);

        if (supervisor != null) {
            notificationService.sendNotification(
                supervisor,
                "Ticket Reopened - Requires Review",
                String.format("Ticket %s has been reopened by student. Reason: %s", 
                    ticket.getTicketNumber(), reason),
                NotificationType.QUALITY_REVIEW,
                ticket
            );
        }
    }

    private boolean isFirstTimeResolution(Ticket ticket) {
        // Check if ticket was never reopened
        return ticket.getHistory().stream()
            .filter(history -> "status".equals(history.getFieldName()))
            .noneMatch(history -> TicketStatus.REOPENED.toString().equals(history.getNewValue()));
    }

    private boolean isResolvedOnTime(Ticket ticket) {
        if (ticket.getEstimatedResolutionTime() == null || ticket.getActualResolutionTime() == null) {
            return false;
        }
        return !ticket.getActualResolutionTime().isAfter(ticket.getEstimatedResolutionTime());
    }

    private boolean hasBeenEscalated(Ticket ticket) {
        return !ticket.getEscalations().isEmpty();
    }

    private boolean isRecurringIssue(Ticket ticket) {
        // Check if similar issue occurred in same location within 30 days before this ticket
        LocalDateTime thirtyDaysBefore = ticket.getCreatedAt().minusDays(30);
        List<Ticket> similarTickets = ticketRepository.findSimilarTicketsInLocation(
            ticket.getHostelBlock(), ticket.getRoomNumber(), 
            ticket.getEffectiveCategory(), thirtyDaysBefore, ticket.getCreatedAt()
        );
        return !similarTickets.isEmpty();
    }

    private boolean requiresPhotoDocumentation(Ticket ticket) {
        if (ticket.getCategory() == null) return false;
        
        // Require photos for infrastructure and structural work
        return Arrays.asList(
            TicketCategory.ELECTRICAL_ISSUES,
            TicketCategory.PLUMBING_WATER,
            TicketCategory.HVAC,
            TicketCategory.STRUCTURAL_CIVIL,
            TicketCategory.FURNITURE_FIXTURES
        ).contains(ticket.getCategory());
    }

    private void flagForQualityReview(Ticket ticket, String reason) {
        // Add comment for quality review
        // This would integrate with a quality review system
        System.out.println(String.format("QUALITY REVIEW FLAGGED - Ticket: %s, Reason: %s", 
            ticket.getTicketNumber(), reason));
        
        // Notify quality assurance team
        List<User> admins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        for (User admin : admins) {
            notificationService.sendNotification(
                admin,
                "Quality Review Required",
                String.format("Ticket %s requires quality review. Reason: %s", 
                    ticket.getTicketNumber(), reason),
                NotificationType.QUALITY_REVIEW,
                ticket
            );
        }
    }

    private void flagRecurringIssue(String location, String category, List<Ticket> tickets) {
        String message = String.format(
            "Recurring issue detected in %s for category %s. %d tickets in 30 days. " +
            "Consider preventive maintenance or infrastructure improvement.",
            location, category, tickets.size()
        );

        // Notify administration for preventive action
        List<User> admins = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
        for (User admin : admins) {
            notificationService.sendNotification(
                admin,
                "Recurring Issue Detected",
                message,
                NotificationType.RECURRING_ISSUE,
                tickets.get(0) // Reference first ticket
            );
        }
    }

    private int getExpectedResolutionHours(Ticket ticket) {
        if (ticket.getCategory() != null) {
            return ticket.getCategory().getEstimatedResolutionHours(ticket.getPriority());
        }
        return 24; // Default 24 hours for custom categories
    }

    /**
     * Quality Metrics Data Class
     */
    public static class QualityMetrics {
        private final double firstTimeResolutionRate;
        private final double avgSatisfactionScore;
        private final double resolutionTimeAdherence;
        private final double escalationRate;
        private final double recurringIssueRate;

        public QualityMetrics(double firstTimeResolutionRate, double avgSatisfactionScore,
                            double resolutionTimeAdherence, double escalationRate, double recurringIssueRate) {
            this.firstTimeResolutionRate = firstTimeResolutionRate;
            this.avgSatisfactionScore = avgSatisfactionScore;
            this.resolutionTimeAdherence = resolutionTimeAdherence;
            this.escalationRate = escalationRate;
            this.recurringIssueRate = recurringIssueRate;
        }

        public double getFirstTimeResolutionRate() { return firstTimeResolutionRate; }
        public double getAvgSatisfactionScore() { return avgSatisfactionScore; }
        public double getResolutionTimeAdherence() { return resolutionTimeAdherence; }
        public double getEscalationRate() { return escalationRate; }
        public double getRecurringIssueRate() { return recurringIssueRate; }

        public double getOverallQualityScore() {
            return (firstTimeResolutionRate * 0.3 + 
                   avgSatisfactionScore * 20 * 0.25 + 
                   resolutionTimeAdherence * 0.25 + 
                   (100 - escalationRate) * 0.1 + 
                   (100 - recurringIssueRate) * 0.1);
        }

        public String getQualityGrade() {
            double score = getOverallQualityScore();
            if (score >= 90) return "A+";
            if (score >= 80) return "A";
            if (score >= 70) return "B";
            if (score >= 60) return "C";
            return "D";
        }
    }
}
