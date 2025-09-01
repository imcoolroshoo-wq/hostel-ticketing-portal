package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Service Level Agreements (SLA) for tickets
 */
@Service
public class SLAService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Calculate and set SLA times for a ticket based on category and priority
     */
    public void calculateSLATimes(Ticket ticket) {
        LocalDateTime now = LocalDateTime.now();
        
        // Get resolution time based on category and priority
        int resolutionHours = getResolutionHours(ticket.getCategory(), ticket.getPriority());
        
        // Set estimated resolution time
        ticket.setEstimatedResolutionTime(now.plusHours(resolutionHours));
        
        // Set SLA breach time (usually 120% of estimated time)
        ticket.setSlaBreachTime(now.plusHours((long)(resolutionHours * 1.2)));
    }

    /**
     * Get resolution time in hours based on category and priority
     */
    private int getResolutionHours(TicketCategory category, TicketPriority priority) {
        Map<String, Map<String, Integer>> resolutionMatrix = getResolutionMatrix();
        
        String categoryKey = category != null ? category.name() : "GENERAL";
        String priorityKey = priority != null ? priority.name() : "MEDIUM";
        
        return resolutionMatrix
            .getOrDefault(categoryKey, resolutionMatrix.get("GENERAL"))
            .getOrDefault(priorityKey, 24); // Default 24 hours
    }

    /**
     * Resolution time matrix as defined in product design document
     */
    private Map<String, Map<String, Integer>> getResolutionMatrix() {
        Map<String, Map<String, Integer>> matrix = new HashMap<>();
        
        // Electrical Issues
        Map<String, Integer> electrical = new HashMap<>();
        electrical.put("EMERGENCY", 4);  // 2-4 hours average = 4
        electrical.put("HIGH", 8);       // 4-8 hours average = 8
        electrical.put("MEDIUM", 36);    // 1-2 days average = 36 hours
        electrical.put("LOW", 48);       // 2-3 days average = 48 hours
        matrix.put("ELECTRICAL_ISSUES", electrical);

        // Plumbing & Water
        Map<String, Integer> plumbing = new HashMap<>();
        plumbing.put("EMERGENCY", 3);    // 1-3 hours average = 3
        plumbing.put("HIGH", 6);         // 4-6 hours average = 6
        plumbing.put("MEDIUM", 36);      // 1-2 days average = 36 hours
        plumbing.put("LOW", 48);         // 2-3 days average = 48 hours
        matrix.put("PLUMBING_WATER", plumbing);

        // HVAC
        Map<String, Integer> hvac = new HashMap<>();
        hvac.put("EMERGENCY", 6);        // 2-6 hours average = 6
        hvac.put("HIGH", 12);            // 6-12 hours average = 12
        hvac.put("MEDIUM", 48);          // 1-3 days average = 48 hours
        hvac.put("LOW", 96);             // 3-5 days average = 96 hours
        matrix.put("HVAC", hvac);

        // Network & Internet
        Map<String, Integer> network = new HashMap<>();
        network.put("EMERGENCY", 2);     // 1-2 hours average = 2
        network.put("HIGH", 4);          // 2-4 hours average = 4
        network.put("MEDIUM", 8);        // 4-8 hours average = 8
        network.put("LOW", 12);          // 8-12 hours average = 12
        matrix.put("NETWORK_INTERNET", network);

        // Safety & Security
        Map<String, Integer> safety = new HashMap<>();
        safety.put("EMERGENCY", 1);      // 30 minutes = 0.5, round up to 1
        safety.put("HIGH", 2);           // 1-2 hours average = 2
        safety.put("MEDIUM", 4);         // 2-4 hours average = 4
        safety.put("LOW", 6);            // 4-6 hours average = 6
        matrix.put("SAFETY_SECURITY", safety);

        // General/Default
        Map<String, Integer> general = new HashMap<>();
        general.put("EMERGENCY", 4);
        general.put("HIGH", 8);
        general.put("MEDIUM", 24);
        general.put("LOW", 48);
        matrix.put("GENERAL", general);

        return matrix;
    }

    /**
     * Check for SLA breaches and send notifications
     */
    public void checkSLABreaches() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find tickets approaching SLA breach (75% of time elapsed)
        List<Ticket> approachingBreach = ticketRepository.findTicketsApproachingSLABreach(now);
        for (Ticket ticket : approachingBreach) {
            sendSLAWarningNotification(ticket);
        }
        
        // Find tickets that have breached SLA
        List<Ticket> breachedTickets = ticketRepository.findTicketsWithSLABreach(now);
        for (Ticket ticket : breachedTickets) {
            sendSLABreachNotification(ticket);
        }
    }

    /**
     * Send warning notification when ticket is approaching SLA breach
     */
    private void sendSLAWarningNotification(Ticket ticket) {
        String message = String.format(
            "Ticket %s (%s) is approaching SLA breach. Expected resolution: %s",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getEstimatedResolutionTime()
        );
        
        // Notify assigned staff
        if (ticket.getAssignedTo() != null) {
            notificationService.sendNotification(
                ticket.getAssignedTo(),
                "SLA Warning",
                message,
                NotificationType.SLA_WARNING,
                ticket
            );
        }
        
        // Notify admins
        notificationService.sendAdminNotification(
            "SLA Warning",
            message,
            NotificationType.SLA_WARNING,
            ticket
        );
    }

    /**
     * Send notification when ticket has breached SLA
     */
    private void sendSLABreachNotification(Ticket ticket) {
        String message = String.format(
            "Ticket %s (%s) has breached SLA! Expected resolution was: %s",
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getEstimatedResolutionTime()
        );
        
        // Notify assigned staff
        if (ticket.getAssignedTo() != null) {
            notificationService.sendNotification(
                ticket.getAssignedTo(),
                "SLA Breach",
                message,
                NotificationType.SLA_BREACH,
                ticket
            );
        }
        
        // Notify admins
        notificationService.sendAdminNotification(
            "SLA Breach",
            message,
            NotificationType.SLA_BREACH,
            ticket
        );
    }

    /**
     * Update SLA times when ticket status changes
     */
    public void updateSLAOnStatusChange(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (newStatus) {
            case ASSIGNED:
                if (ticket.getAssignedAt() == null) {
                    ticket.setAssignedAt(now);
                }
                break;
            case IN_PROGRESS:
                if (ticket.getStartedAt() == null) {
                    ticket.setStartedAt(now);
                }
                break;
            case RESOLVED:
                if (ticket.getResolvedAt() == null) {
                    ticket.setResolvedAt(now);
                    ticket.setActualResolutionTime(now);
                }
                break;
            case CLOSED:
                if (ticket.getClosedAt() == null) {
                    ticket.setClosedAt(now);
                }
                break;
        }
    }

    /**
     * Calculate SLA performance metrics
     */
    public Map<String, Object> calculateSLAMetrics(List<Ticket> tickets) {
        Map<String, Object> metrics = new HashMap<>();
        
        int totalTickets = tickets.size();
        int onTimeResolved = 0;
        int breachedSLA = 0;
        long totalResolutionTimeHours = 0;
        
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
                LocalDateTime resolvedTime = ticket.getResolvedAt();
                if (resolvedTime != null && ticket.getEstimatedResolutionTime() != null) {
                    if (resolvedTime.isBefore(ticket.getEstimatedResolutionTime()) || 
                        resolvedTime.isEqual(ticket.getEstimatedResolutionTime())) {
                        onTimeResolved++;
                    } else {
                        breachedSLA++;
                    }
                    
                    // Calculate actual resolution time
                    long resolutionHours = java.time.Duration.between(ticket.getCreatedAt(), resolvedTime).toHours();
                    totalResolutionTimeHours += resolutionHours;
                }
            }
        }
        
        metrics.put("totalTickets", totalTickets);
        metrics.put("onTimeResolved", onTimeResolved);
        metrics.put("breachedSLA", breachedSLA);
        metrics.put("slaComplianceRate", totalTickets > 0 ? (double) onTimeResolved / totalTickets * 100 : 0);
        metrics.put("averageResolutionTimeHours", totalTickets > 0 ? totalResolutionTimeHours / totalTickets : 0);
        
        return metrics;
    }
}
