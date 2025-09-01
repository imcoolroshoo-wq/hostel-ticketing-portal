package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Quality Metrics Service implementing comprehensive quality tracking
 * as per IIM Trichy Product Design Document Section 12.3
 */
@Service
@Transactional
public class EnhancedQualityMetricsService {

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Note: TicketHistory analysis would require TicketHistoryRepository
    // Note: Escalation analysis would use TicketEscalationRepository

    /**
     * Calculate comprehensive quality metrics for the system
     */
    public QualityMetrics calculateQualityMetrics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // First-Time Resolution Rate
        double firstTimeResolutionRate = calculateFirstTimeResolutionRate(tickets);
        
        // Student Satisfaction Score
        double avgSatisfactionScore = calculateAverageSatisfactionScore(tickets);
        
        // Resolution Time Adherence
        double resolutionTimeAdherence = calculateResolutionTimeAdherence(tickets);
        
        // Escalation Rate
        double escalationRate = calculateEscalationRate(tickets);
        
        // Recurring Issue Rate
        double recurringIssueRate = calculateRecurringIssueRate(tickets);
        
        // Quality Score by Category
        Map<String, QualityCategoryMetrics> categoryMetrics = calculateCategoryQualityMetrics(tickets);
        
        // Staff Performance Quality
        Map<String, StaffQualityMetrics> staffMetrics = calculateStaffQualityMetrics(tickets);
        
        // Trend Analysis
        QualityTrend trend = calculateQualityTrend(startDate, endDate);
        
        return new QualityMetrics(
            firstTimeResolutionRate,
            avgSatisfactionScore,
            resolutionTimeAdherence,
            escalationRate,
            recurringIssueRate,
            categoryMetrics,
            staffMetrics,
            trend
        );
    }

    /**
     * Calculate First-Time Resolution Rate
     * Percentage of issues resolved without reopening
     */
    private double calculateFirstTimeResolutionRate(List<Ticket> tickets) {
        List<Ticket> resolvedTickets = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
            .collect(Collectors.toList());
        
        if (resolvedTickets.isEmpty()) return 0.0;
        
        long firstTimeResolved = resolvedTickets.stream()
            .mapToLong(ticket -> wasResolvedFirstTime(ticket) ? 1 : 0)
            .sum();
        
        return (double) firstTimeResolved / resolvedTickets.size() * 100;
    }

    /**
     * Check if ticket was resolved on first attempt (not reopened)
     * Note: Simplified implementation - would use TicketHistory in complete system
     */
    private boolean wasResolvedFirstTime(Ticket ticket) {
        // Simplified check - if current status is CLOSED and no reopened status in history
        // In a complete implementation, this would query TicketHistory
        return ticket.getStatus() == TicketStatus.CLOSED;
    }

    /**
     * Calculate average satisfaction score
     */
    private double calculateAverageSatisfactionScore(List<Ticket> tickets) {
        List<Integer> ratings = tickets.stream()
            .map(Ticket::getSatisfactionRating)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (ratings.isEmpty()) return 0.0;
        
        return ratings.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

    /**
     * Calculate resolution time adherence (SLA compliance)
     */
    private double calculateResolutionTimeAdherence(List<Ticket> tickets) {
        List<Ticket> resolvedTickets = tickets.stream()
            .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
            .filter(ticket -> ticket.getEstimatedResolutionTime() != null)
            .collect(Collectors.toList());
        
        if (resolvedTickets.isEmpty()) return 0.0;
        
        long adherentTickets = resolvedTickets.stream()
            .mapToLong(ticket -> isResolvedWithinSLA(ticket) ? 1 : 0)
            .sum();
        
        return (double) adherentTickets / resolvedTickets.size() * 100;
    }

    /**
     * Check if ticket was resolved within SLA
     */
    private boolean isResolvedWithinSLA(Ticket ticket) {
        if (ticket.getResolvedAt() == null || ticket.getEstimatedResolutionTime() == null) {
            return false;
        }
        
        return ticket.getResolvedAt().isBefore(ticket.getEstimatedResolutionTime()) ||
               ticket.getResolvedAt().isEqual(ticket.getEstimatedResolutionTime());
    }

    /**
     * Calculate escalation rate
     */
    private double calculateEscalationRate(List<Ticket> tickets) {
        if (tickets.isEmpty()) return 0.0;
        
        long escalatedTickets = tickets.stream()
            .mapToLong(ticket -> hasEscalation(ticket) ? 1 : 0)
            .sum();
        
        return (double) escalatedTickets / tickets.size() * 100;
    }

    /**
     * Check if ticket has been escalated
     * Note: Simplified implementation - would query TicketEscalation table
     */
    private boolean hasEscalation(Ticket ticket) {
        // Simplified check - in complete implementation would query escalation history
        return false; // Placeholder - would check escalation records
    }

    /**
     * Calculate recurring issue rate
     */
    private double calculateRecurringIssueRate(List<Ticket> tickets) {
        if (tickets.isEmpty()) return 0.0;
        
        Map<String, List<Ticket>> locationGroups = tickets.stream()
            .filter(ticket -> ticket.getRoomNumber() != null)
            .collect(Collectors.groupingBy(
                ticket -> ticket.getHostelBlock() + "-" + ticket.getRoomNumber()
            ));
        
        long recurringIssues = 0;
        
        for (Map.Entry<String, List<Ticket>> entry : locationGroups.entrySet()) {
            List<Ticket> locationTickets = entry.getValue();
            
            // Group by category
            Map<String, List<Ticket>> categoryGroups = locationTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEffectiveCategory));
            
            for (List<Ticket> categoryTickets : categoryGroups.values()) {
                if (categoryTickets.size() > 1) {
                    // Check if tickets occurred within 30 days of each other
                    categoryTickets.sort(Comparator.comparing(Ticket::getCreatedAt));
                    
                    for (int i = 1; i < categoryTickets.size(); i++) {
                        Ticket previous = categoryTickets.get(i - 1);
                        Ticket current = categoryTickets.get(i);
                        
                        long daysBetween = ChronoUnit.DAYS.between(
                            previous.getCreatedAt().toLocalDate(),
                            current.getCreatedAt().toLocalDate()
                        );
                        
                        if (daysBetween <= 30) {
                            recurringIssues++;
                        }
                    }
                }
            }
        }
        
        return (double) recurringIssues / tickets.size() * 100;
    }

    /**
     * Calculate quality metrics by category
     */
    private Map<String, QualityCategoryMetrics> calculateCategoryQualityMetrics(List<Ticket> tickets) {
        Map<String, List<Ticket>> categoryGroups = tickets.stream()
            .collect(Collectors.groupingBy(Ticket::getEffectiveCategory));
        
        Map<String, QualityCategoryMetrics> categoryMetrics = new HashMap<>();
        
        for (Map.Entry<String, List<Ticket>> entry : categoryGroups.entrySet()) {
            String category = entry.getKey();
            List<Ticket> categoryTickets = entry.getValue();
            
            double avgSatisfaction = calculateAverageSatisfactionScore(categoryTickets);
            double firstTimeResolution = calculateFirstTimeResolutionRate(categoryTickets);
            double slaAdherence = calculateResolutionTimeAdherence(categoryTickets);
            double avgResolutionHours = calculateAverageResolutionTime(categoryTickets);
            
            categoryMetrics.put(category, new QualityCategoryMetrics(
                avgSatisfaction,
                firstTimeResolution,
                slaAdherence,
                avgResolutionHours,
                categoryTickets.size()
            ));
        }
        
        return categoryMetrics;
    }

    /**
     * Calculate staff quality metrics
     */
    private Map<String, StaffQualityMetrics> calculateStaffQualityMetrics(List<Ticket> tickets) {
        Map<UUID, List<Ticket>> staffGroups = tickets.stream()
            .filter(ticket -> ticket.getAssignedTo() != null)
            .collect(Collectors.groupingBy(ticket -> ticket.getAssignedTo().getId()));
        
        Map<String, StaffQualityMetrics> staffMetrics = new HashMap<>();
        
        for (Map.Entry<UUID, List<Ticket>> entry : staffGroups.entrySet()) {
            UUID staffId = entry.getKey();
            List<Ticket> staffTickets = entry.getValue();
            
            User staff = userRepository.findById(staffId).orElse(null);
            if (staff == null) continue;
            
            double avgSatisfaction = calculateAverageSatisfactionScore(staffTickets);
            double firstTimeResolution = calculateFirstTimeResolutionRate(staffTickets);
            double slaAdherence = calculateResolutionTimeAdherence(staffTickets);
            double avgResolutionHours = calculateAverageResolutionTime(staffTickets);
            int totalTicketsHandled = staffTickets.size();
            double productivity = calculateStaffProductivity(staff, staffTickets);
            
            // Calculate quality score (weighted average)
            double qualityScore = (avgSatisfaction * 0.3) + 
                                 (firstTimeResolution * 0.25) + 
                                 (slaAdherence * 0.25) + 
                                 (productivity * 0.2);
            
            staffMetrics.put(staff.getUsername(), new StaffQualityMetrics(
                avgSatisfaction,
                firstTimeResolution,
                slaAdherence,
                avgResolutionHours,
                totalTicketsHandled,
                productivity,
                qualityScore
            ));
        }
        
        return staffMetrics;
    }

    /**
     * Calculate average resolution time in hours
     */
    private double calculateAverageResolutionTime(List<Ticket> tickets) {
        List<Double> resolutionTimes = tickets.stream()
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getResolvedAt() != null)
            .map(ticket -> (double) ChronoUnit.HOURS.between(ticket.getCreatedAt(), ticket.getResolvedAt()))
            .collect(Collectors.toList());
        
        if (resolutionTimes.isEmpty()) return 0.0;
        
        return resolutionTimes.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    /**
     * Calculate staff productivity score
     */
    private double calculateStaffProductivity(User staff, List<Ticket> staffTickets) {
        if (staffTickets.isEmpty()) return 0.0;
        
        long resolvedTickets = staffTickets.stream()
            .mapToLong(ticket -> ticket.getStatus() == TicketStatus.CLOSED ? 1 : 0)
            .sum();
        
        // Calculate tickets per day
        LocalDateTime earliestTicket = staffTickets.stream()
            .map(Ticket::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        
        long daysSinceFirst = ChronoUnit.DAYS.between(earliestTicket.toLocalDate(), LocalDate.now());
        if (daysSinceFirst == 0) daysSinceFirst = 1;
        
        double ticketsPerDay = (double) resolvedTickets / daysSinceFirst;
        
        // Normalize to 0-100 scale (assuming 3 tickets per day is excellent)
        return Math.min(100.0, ticketsPerDay / 3.0 * 100);
    }

    /**
     * Calculate quality trend over time
     */
    private QualityTrend calculateQualityTrend(LocalDate startDate, LocalDate endDate) {
        List<QualityDataPoint> dataPoints = new ArrayList<>();
        
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(endDate)) weekEnd = endDate;
            
            List<Ticket> weekTickets = ticketRepository.findByCreatedAtBetween(
                current.atStartOfDay(),
                weekEnd.atTime(23, 59, 59)
            );
            
            if (!weekTickets.isEmpty()) {
                double satisfaction = calculateAverageSatisfactionScore(weekTickets);
                double firstTimeResolution = calculateFirstTimeResolutionRate(weekTickets);
                double slaAdherence = calculateResolutionTimeAdherence(weekTickets);
                
                dataPoints.add(new QualityDataPoint(
                    current,
                    satisfaction,
                    firstTimeResolution,
                    slaAdherence,
                    weekTickets.size()
                ));
            }
            
            current = current.plusDays(7);
        }
        
        return new QualityTrend(dataPoints);
    }

    /**
     * Generate quality improvement recommendations
     */
    public List<QualityRecommendation> generateQualityRecommendations(QualityMetrics metrics) {
        List<QualityRecommendation> recommendations = new ArrayList<>();
        
        // First-time resolution recommendations
        if (metrics.getFirstTimeResolutionRate() < 90.0) {
            recommendations.add(new QualityRecommendation(
                "Improve First-Time Resolution",
                QualityRecommendation.Priority.HIGH,
                "First-time resolution rate is below target (90%). Consider additional staff training and better diagnostic procedures."
            ));
        }
        
        // Satisfaction score recommendations
        if (metrics.getAverageSatisfactionScore() < 4.0) {
            recommendations.add(new QualityRecommendation(
                "Enhance Customer Satisfaction",
                QualityRecommendation.Priority.HIGH,
                "Satisfaction score is below target (4.0/5.0). Focus on communication and service quality improvements."
            ));
        }
        
        // SLA adherence recommendations
        if (metrics.getResolutionTimeAdherence() < 95.0) {
            recommendations.add(new QualityRecommendation(
                "Improve SLA Compliance",
                QualityRecommendation.Priority.MEDIUM,
                "SLA adherence is below target (95%). Review capacity planning and assignment algorithms."
            ));
        }
        
        // Escalation rate recommendations
        if (metrics.getEscalationRate() > 10.0) {
            recommendations.add(new QualityRecommendation(
                "Reduce Escalation Rate",
                QualityRecommendation.Priority.MEDIUM,
                "Escalation rate is above target (10%). Improve initial assignment accuracy and staff skills."
            ));
        }
        
        // Recurring issues recommendations
        if (metrics.getRecurringIssueRate() > 5.0) {
            recommendations.add(new QualityRecommendation(
                "Address Recurring Issues",
                QualityRecommendation.Priority.HIGH,
                "Recurring issue rate is above target (5%). Implement preventive maintenance and root cause analysis."
            ));
        }
        
        // Category-specific recommendations
        for (Map.Entry<String, QualityCategoryMetrics> entry : metrics.getCategoryMetrics().entrySet()) {
            String category = entry.getKey();
            QualityCategoryMetrics categoryMetrics = entry.getValue();
            
            if (categoryMetrics.getAverageSatisfaction() < 3.5) {
                recommendations.add(new QualityRecommendation(
                    "Improve " + category + " Service Quality",
                    QualityRecommendation.Priority.MEDIUM,
                    "Category " + category + " has low satisfaction scores. Review processes and staff training for this category."
                ));
            }
        }
        
        return recommendations;
    }

    // Supporting classes
    public static class QualityMetrics {
        private double firstTimeResolutionRate;
        private double averageSatisfactionScore;
        private double resolutionTimeAdherence;
        private double escalationRate;
        private double recurringIssueRate;
        private Map<String, QualityCategoryMetrics> categoryMetrics;
        private Map<String, StaffQualityMetrics> staffMetrics;
        private QualityTrend trend;

        public QualityMetrics(double firstTimeResolutionRate, double averageSatisfactionScore,
                             double resolutionTimeAdherence, double escalationRate, double recurringIssueRate,
                             Map<String, QualityCategoryMetrics> categoryMetrics,
                             Map<String, StaffQualityMetrics> staffMetrics, QualityTrend trend) {
            this.firstTimeResolutionRate = firstTimeResolutionRate;
            this.averageSatisfactionScore = averageSatisfactionScore;
            this.resolutionTimeAdherence = resolutionTimeAdherence;
            this.escalationRate = escalationRate;
            this.recurringIssueRate = recurringIssueRate;
            this.categoryMetrics = categoryMetrics;
            this.staffMetrics = staffMetrics;
            this.trend = trend;
        }

        // Getters
        public double getFirstTimeResolutionRate() { return firstTimeResolutionRate; }
        public double getAverageSatisfactionScore() { return averageSatisfactionScore; }
        public double getResolutionTimeAdherence() { return resolutionTimeAdherence; }
        public double getEscalationRate() { return escalationRate; }
        public double getRecurringIssueRate() { return recurringIssueRate; }
        public Map<String, QualityCategoryMetrics> getCategoryMetrics() { return categoryMetrics; }
        public Map<String, StaffQualityMetrics> getStaffMetrics() { return staffMetrics; }
        public QualityTrend getTrend() { return trend; }
    }

    public static class QualityCategoryMetrics {
        private double averageSatisfaction;
        private double firstTimeResolutionRate;
        private double slaAdherence;
        private double averageResolutionHours;
        private int totalTickets;

        public QualityCategoryMetrics(double averageSatisfaction, double firstTimeResolutionRate,
                                     double slaAdherence, double averageResolutionHours, int totalTickets) {
            this.averageSatisfaction = averageSatisfaction;
            this.firstTimeResolutionRate = firstTimeResolutionRate;
            this.slaAdherence = slaAdherence;
            this.averageResolutionHours = averageResolutionHours;
            this.totalTickets = totalTickets;
        }

        // Getters
        public double getAverageSatisfaction() { return averageSatisfaction; }
        public double getFirstTimeResolutionRate() { return firstTimeResolutionRate; }
        public double getSlaAdherence() { return slaAdherence; }
        public double getAverageResolutionHours() { return averageResolutionHours; }
        public int getTotalTickets() { return totalTickets; }
    }

    public static class StaffQualityMetrics {
        private double averageSatisfaction;
        private double firstTimeResolutionRate;
        private double slaAdherence;
        private double averageResolutionHours;
        private int totalTicketsHandled;
        private double productivity;
        private double qualityScore;

        public StaffQualityMetrics(double averageSatisfaction, double firstTimeResolutionRate,
                                  double slaAdherence, double averageResolutionHours, int totalTicketsHandled,
                                  double productivity, double qualityScore) {
            this.averageSatisfaction = averageSatisfaction;
            this.firstTimeResolutionRate = firstTimeResolutionRate;
            this.slaAdherence = slaAdherence;
            this.averageResolutionHours = averageResolutionHours;
            this.totalTicketsHandled = totalTicketsHandled;
            this.productivity = productivity;
            this.qualityScore = qualityScore;
        }

        // Getters
        public double getAverageSatisfaction() { return averageSatisfaction; }
        public double getFirstTimeResolutionRate() { return firstTimeResolutionRate; }
        public double getSlaAdherence() { return slaAdherence; }
        public double getAverageResolutionHours() { return averageResolutionHours; }
        public int getTotalTicketsHandled() { return totalTicketsHandled; }
        public double getProductivity() { return productivity; }
        public double getQualityScore() { return qualityScore; }
    }

    public static class QualityTrend {
        private List<QualityDataPoint> dataPoints;

        public QualityTrend(List<QualityDataPoint> dataPoints) {
            this.dataPoints = dataPoints;
        }

        public List<QualityDataPoint> getDataPoints() { return dataPoints; }
    }

    public static class QualityDataPoint {
        private LocalDate date;
        private double satisfaction;
        private double firstTimeResolution;
        private double slaAdherence;
        private int ticketCount;

        public QualityDataPoint(LocalDate date, double satisfaction, double firstTimeResolution,
                               double slaAdherence, int ticketCount) {
            this.date = date;
            this.satisfaction = satisfaction;
            this.firstTimeResolution = firstTimeResolution;
            this.slaAdherence = slaAdherence;
            this.ticketCount = ticketCount;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public double getSatisfaction() { return satisfaction; }
        public double getFirstTimeResolution() { return firstTimeResolution; }
        public double getSlaAdherence() { return slaAdherence; }
        public int getTicketCount() { return ticketCount; }
    }

    public static class QualityRecommendation {
        private String title;
        private Priority priority;
        private String description;

        public enum Priority {
            LOW, MEDIUM, HIGH
        }

        public QualityRecommendation(String title, Priority priority, String description) {
            this.title = title;
            this.priority = priority;
            this.description = description;
        }

        // Getters
        public String getTitle() { return title; }
        public Priority getPriority() { return priority; }
        public String getDescription() { return description; }
    }
}
