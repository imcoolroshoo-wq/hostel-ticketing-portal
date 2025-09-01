package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service implementing Advanced Analytics functionality
 * as per IIM Trichy Hostel Ticket Management System Product Design Document Section 4.3.3
 */
@Service
public class AnalyticsService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TicketEscalationRepository ticketEscalationRepository;

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    @Autowired
    private SLAService slaService;

    /**
     * Get comprehensive dashboard analytics
     */
    public Map<String, Object> getDashboardAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Set default date range if not provided (last 30 days)
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // Get tickets within date range
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Basic metrics
        analytics.put("totalTickets", tickets.size());
        analytics.put("openTickets", tickets.stream().mapToLong(t -> t.getStatus() == TicketStatus.OPEN ? 1 : 0).sum());
        analytics.put("inProgressTickets", tickets.stream().mapToLong(t -> t.getStatus() == TicketStatus.IN_PROGRESS ? 1 : 0).sum());
        analytics.put("resolvedTickets", tickets.stream().mapToLong(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED ? 1 : 0).sum());
        
        // Status distribution
        Map<String, Long> statusDistribution = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
        analytics.put("statusDistribution", statusDistribution);
        
        // Priority distribution
        Map<String, Long> priorityDistribution = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        analytics.put("priorityDistribution", priorityDistribution);
        
        // Category distribution
        Map<String, Long> categoryDistribution = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));
        analytics.put("categoryDistribution", categoryDistribution);
        
        // Average resolution time
        double avgResolutionTime = tickets.stream()
                .filter(t -> t.getResolvedAt() != null)
                .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreatedAt(), t.getResolvedAt()))
                .average()
                .orElse(0.0);
        analytics.put("averageResolutionTimeHours", avgResolutionTime);
        
        // SLA compliance
        Map<String, Object> slaMetrics = slaService.calculateSLAMetrics(tickets);
        analytics.put("slaCompliance", slaMetrics);
        
        // Staff workload
        List<User> staff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        Map<String, Object> workloadData = new HashMap<>();
        for (User staffMember : staff) {
            long activeTickets = tickets.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(staffMember.getId()))
                    .filter(t -> t.getStatus().isActive())
                    .count();
            workloadData.put(staffMember.getFullName(), activeTickets);
        }
        analytics.put("staffWorkload", workloadData);
        
        return analytics;
    }

    /**
     * Get ticket performance metrics
     */
    public Map<String, Object> getTicketPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> metrics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Resolution rates
        long totalTickets = tickets.size();
        long resolvedTickets = tickets.stream().mapToLong(t -> 
            (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) ? 1 : 0).sum();
        double resolutionRate = totalTickets > 0 ? (double) resolvedTickets / totalTickets * 100 : 0;
        
        metrics.put("totalTickets", totalTickets);
        metrics.put("resolvedTickets", resolvedTickets);
        metrics.put("resolutionRate", resolutionRate);
        
        // Average times
        double avgFirstResponseTime = tickets.stream()
                .filter(t -> t.getAssignedTo() != null)
                .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreatedAt(), t.getUpdatedAt()))
                .average()
                .orElse(0.0);
        metrics.put("averageFirstResponseTimeHours", avgFirstResponseTime);
        
        // Reopening rate
        long reopenedTickets = tickets.stream().mapToLong(t -> t.getStatus() == TicketStatus.REOPENED ? 1 : 0).sum();
        double reopeningRate = resolvedTickets > 0 ? (double) reopenedTickets / resolvedTickets * 100 : 0;
        metrics.put("reopeningRate", reopeningRate);
        
        // Customer satisfaction
        double avgSatisfaction = tickets.stream()
                .filter(t -> t.getSatisfactionRating() != null && t.getSatisfactionRating() > 0)
                .mapToInt(Ticket::getSatisfactionRating)
                .average()
                .orElse(0.0);
        metrics.put("averageSatisfactionRating", avgSatisfaction);
        
        return metrics;
    }

    /**
     * Get staff performance analytics
     */
    public Map<String, Object> getStaffPerformanceAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<User> staff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        List<Map<String, Object>> staffPerformance = new ArrayList<>();
        
        for (User staffMember : staff) {
            Map<String, Object> performance = new HashMap<>();
            performance.put("staffId", staffMember.getId());
            performance.put("staffName", staffMember.getFullName());
            performance.put("staffVertical", staffMember.getStaffVertical());
            
            // Get tickets assigned to this staff member
            List<Ticket> assignedTickets = ticketRepository.findByAssignedToIdAndCreatedAtBetween(
                    staffMember.getId(), startDateTime, endDateTime);
            
            performance.put("assignedTickets", assignedTickets.size());
            
            long resolvedTickets = assignedTickets.stream()
                    .mapToLong(t -> (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) ? 1 : 0)
                    .sum();
            performance.put("resolvedTickets", resolvedTickets);
            
            double resolutionRate = assignedTickets.size() > 0 ? 
                    (double) resolvedTickets / assignedTickets.size() * 100 : 0;
            performance.put("resolutionRate", resolutionRate);
            
            // Average resolution time for this staff member
            double avgResolutionTime = assignedTickets.stream()
                    .filter(t -> t.getResolvedAt() != null)
                    .mapToLong(t -> ChronoUnit.HOURS.between(t.getCreatedAt(), t.getResolvedAt()))
                    .average()
                    .orElse(0.0);
            performance.put("averageResolutionTimeHours", avgResolutionTime);
            
            // Customer satisfaction for this staff member
            double avgSatisfaction = assignedTickets.stream()
                    .filter(t -> t.getSatisfactionRating() != null && t.getSatisfactionRating() > 0)
                    .mapToInt(Ticket::getSatisfactionRating)
                    .average()
                    .orElse(0.0);
            performance.put("averageSatisfactionRating", avgSatisfaction);
            
            // Current workload (active tickets)
            long activeTickets = assignedTickets.stream()
                    .mapToLong(t -> t.getStatus().isActive() ? 1 : 0)
                    .sum();
            performance.put("currentActiveTickets", activeTickets);
            
            staffPerformance.add(performance);
        }
        
        analytics.put("staffPerformance", staffPerformance);
        analytics.put("totalStaff", staff.size());
        
        return analytics;
    }

    /**
     * Get category and priority trends
     */
    public Map<String, Object> getCategoryTrends(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trends = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(90);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Category trends over time
        Map<String, Map<String, Long>> categoryTrends = new HashMap<>();
        Map<String, Long> categoryTotals = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));
        
        trends.put("categoryTotals", categoryTotals);
        
        // Priority trends
        Map<String, Long> priorityTrends = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));
        trends.put("priorityTrends", priorityTrends);
        
        // Top 10 most common issues
        Map<String, Long> commonIssues = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getTitle, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        trends.put("topIssues", commonIssues);
        
        return trends;
    }

    /**
     * Get hostel-wise analytics
     */
    public Map<String, Object> getHostelAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Group tickets by hostel block
        Map<String, List<Ticket>> ticketsByHostel = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getHostelBlock() != null ? t.getHostelBlock() : "Unknown"));
        
        Map<String, Map<String, Object>> hostelMetrics = new HashMap<>();
        
        for (Map.Entry<String, List<Ticket>> entry : ticketsByHostel.entrySet()) {
            String hostel = entry.getKey();
            List<Ticket> hostelTickets = entry.getValue();
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalTickets", hostelTickets.size());
            
            long resolvedTickets = hostelTickets.stream()
                    .mapToLong(t -> (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) ? 1 : 0)
                    .sum();
            metrics.put("resolvedTickets", resolvedTickets);
            
            double resolutionRate = hostelTickets.size() > 0 ? 
                    (double) resolvedTickets / hostelTickets.size() * 100 : 0;
            metrics.put("resolutionRate", resolutionRate);
            
            // Category breakdown for this hostel
            Map<String, Long> categoryBreakdown = hostelTickets.stream()
                    .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));
            metrics.put("categoryBreakdown", categoryBreakdown);
            
            // Average satisfaction for this hostel
            double avgSatisfaction = hostelTickets.stream()
                    .filter(t -> t.getSatisfactionRating() != null && t.getSatisfactionRating() > 0)
                    .mapToInt(Ticket::getSatisfactionRating)
                    .average()
                    .orElse(0.0);
            metrics.put("averageSatisfactionRating", avgSatisfaction);
            
            hostelMetrics.put(hostel, metrics);
        }
        
        analytics.put("hostelMetrics", hostelMetrics);
        
        return analytics;
    }

    /**
     * Get time-based trends
     */
    public Map<String, Object> getTimeTrends(String period, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trends = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(90);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        DateTimeFormatter formatter;
        switch (period.toLowerCase()) {
            case "daily":
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                break;
            case "weekly":
                formatter = DateTimeFormatter.ofPattern("yyyy-'W'ww");
                break;
            case "monthly":
            default:
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
        }
        
        Map<String, List<Ticket>> ticketsByPeriod = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCreatedAt().format(formatter)));
        
        List<Map<String, Object>> timeSeriesData = new ArrayList<>();
        
        for (Map.Entry<String, List<Ticket>> entry : ticketsByPeriod.entrySet()) {
            Map<String, Object> periodData = new HashMap<>();
            periodData.put("period", entry.getKey());
            periodData.put("totalTickets", entry.getValue().size());
            
            long resolvedTickets = entry.getValue().stream()
                    .mapToLong(t -> (t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED) ? 1 : 0)
                    .sum();
            periodData.put("resolvedTickets", resolvedTickets);
            
            timeSeriesData.add(periodData);
        }
        
        // Sort by period
        timeSeriesData.sort(Comparator.comparing(data -> (String) data.get("period")));
        
        trends.put("timeSeriesData", timeSeriesData);
        trends.put("period", period);
        
        return trends;
    }

    /**
     * Get SLA compliance analytics
     */
    public Map<String, Object> getSLAComplianceAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        Map<String, Object> slaMetrics = slaService.calculateSLAMetrics(tickets);
        analytics.putAll(slaMetrics);
        
        // SLA compliance by category
        Map<String, Map<String, Object>> categoryCompliance = new HashMap<>();
        Map<String, List<Ticket>> ticketsByCategory = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEffectiveCategory));
        
        for (Map.Entry<String, List<Ticket>> entry : ticketsByCategory.entrySet()) {
            Map<String, Object> categorySLA = slaService.calculateSLAMetrics(entry.getValue());
            categoryCompliance.put(entry.getKey(), categorySLA);
        }
        
        analytics.put("categoryCompliance", categoryCompliance);
        
        return analytics;
    }

    /**
     * Get satisfaction analytics
     */
    public Map<String, Object> getSatisfactionAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime)
                .stream()
                .filter(t -> t.getSatisfactionRating() != null && t.getSatisfactionRating() > 0)
                .collect(Collectors.toList());
        
        if (tickets.isEmpty()) {
            analytics.put("totalRatings", 0);
            analytics.put("averageRating", 0.0);
            analytics.put("ratingDistribution", new HashMap<>());
            return analytics;
        }
        
        // Average satisfaction
        double avgSatisfaction = tickets.stream()
                .mapToInt(Ticket::getSatisfactionRating)
                .average()
                .orElse(0.0);
        analytics.put("averageRating", avgSatisfaction);
        analytics.put("totalRatings", tickets.size());
        
        // Rating distribution
        Map<Integer, Long> ratingDistribution = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getSatisfactionRating, Collectors.counting()));
        analytics.put("ratingDistribution", ratingDistribution);
        
        // Satisfaction by category
        Map<String, Double> categorysatisfaction = tickets.stream()
                .collect(Collectors.groupingBy(
                        Ticket::getEffectiveCategory,
                        Collectors.averagingInt(Ticket::getSatisfactionRating)));
        analytics.put("categorysatisfaction", categorysatisfaction);
        
        return analytics;
    }

    /**
     * Get escalation analytics
     */
    public Map<String, Object> getEscalationAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<TicketEscalation> escalations = ticketEscalationRepository.findByEscalatedAtBetween(startDateTime, endDateTime);
        
        analytics.put("totalEscalations", escalations.size());
        
        // Escalations by level
        Map<Integer, Long> escalationsByLevel = escalations.stream()
                .collect(Collectors.groupingBy(TicketEscalation::getEscalationLevel, Collectors.counting()));
        analytics.put("escalationsByLevel", escalationsByLevel);
        
        // Escalation reasons
        Map<String, Long> escalationReasons = escalations.stream()
                .collect(Collectors.groupingBy(TicketEscalation::getReason, Collectors.counting()));
        analytics.put("escalationReasons", escalationReasons);
        
        return analytics;
    }

    /**
     * Get workload analytics
     */
    public Map<String, Object> getWorkloadAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        
        List<User> staff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        
        Map<String, Object> workloadDistribution = new HashMap<>();
        for (User staffMember : staff) {
            long activeTickets = ticketRepository.countByAssignedToIdAndStatusIn(
                    staffMember.getId(), 
                    Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
            workloadDistribution.put(staffMember.getFullName(), activeTickets);
        }
        
        analytics.put("currentWorkload", workloadDistribution);
        
        // Workload balance metrics
        List<Long> workloads = workloadDistribution.values().stream()
                .map(w -> (Long) w)
                .collect(Collectors.toList());
        
        if (!workloads.isEmpty()) {
            double avgWorkload = workloads.stream().mapToLong(Long::longValue).average().orElse(0);
            long maxWorkload = workloads.stream().mapToLong(Long::longValue).max().orElse(0);
            long minWorkload = workloads.stream().mapToLong(Long::longValue).min().orElse(0);
            
            analytics.put("averageWorkload", avgWorkload);
            analytics.put("maxWorkload", maxWorkload);
            analytics.put("minWorkload", minWorkload);
            analytics.put("workloadImbalance", maxWorkload - minWorkload);
        }
        
        return analytics;
    }

    /**
     * Get predictive analytics
     */
    public Map<String, Object> getPredictiveAnalytics() {
        Map<String, Object> predictions = new HashMap<>();
        
        // Simple trend-based predictions
        // In a real implementation, this would use machine learning algorithms
        
        LocalDate threeMonthsAgo = LocalDate.now().minusDays(90);
        LocalDate now = LocalDate.now();
        
        List<Ticket> recentTickets = ticketRepository.findByCreatedAtBetween(
                threeMonthsAgo.atStartOfDay(), now.atTime(23, 59, 59));
        
        // Predict ticket volume for next month
        double avgTicketsPerDay = (double) recentTickets.size() / 90;
        long predictedTicketsNextMonth = Math.round(avgTicketsPerDay * 30);
        
        predictions.put("predictedTicketsNextMonth", predictedTicketsNextMonth);
        predictions.put("averageTicketsPerDay", avgTicketsPerDay);
        
        // Predict most likely categories
        Map<String, Long> categoryTrends = recentTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));
        
        List<Map.Entry<String, Long>> topCategories = categoryTrends.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        predictions.put("topPredictedCategories", topCategories);
        
        return predictions;
    }

    /**
     * Get asset utilization analytics
     */
    public Map<String, Object> getAssetUtilizationAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Asset statistics
        long totalAssets = assetRepository.count();
        long assignedAssets = assetRepository.countByAssignedToIsNotNull();
        long unassignedAssets = totalAssets - assignedAssets;
        
        analytics.put("totalAssets", totalAssets);
        analytics.put("assignedAssets", assignedAssets);
        analytics.put("unassignedAssets", unassignedAssets);
        analytics.put("utilizationRate", totalAssets > 0 ? (double) assignedAssets / totalAssets * 100 : 0);
        
        // Assets by type
        Map<String, Long> assetsByType = new HashMap<>();
        for (AssetType type : AssetType.values()) {
            long count = assetRepository.countByType(type);
            assetsByType.put(type.name(), count);
        }
        analytics.put("assetsByType", assetsByType);
        
        // Assets by status
        Map<String, Long> assetsByStatus = new HashMap<>();
        for (AssetStatus status : AssetStatus.values()) {
            long count = assetRepository.countByStatus(status);
            assetsByStatus.put(status.name(), count);
        }
        analytics.put("assetsByStatus", assetsByStatus);
        
        // Maintenance metrics
        long maintenanceRequired = assetRepository.countAssetsRequiringMaintenance();
        long expiredWarranty = assetRepository.countAssetsWithExpiredWarranty();
        
        analytics.put("assetsRequiringMaintenance", maintenanceRequired);
        analytics.put("assetsWithExpiredWarranty", expiredWarranty);
        
        return analytics;
    }

    /**
     * Export analytics report
     */
    public byte[] exportAnalyticsReport(String format, String reportType, LocalDate startDate, LocalDate endDate) {
        // This is a simplified implementation
        // In a real system, this would generate PDF or Excel reports
        
        Map<String, Object> data;
        switch (reportType) {
            case "performance":
                data = getTicketPerformanceMetrics(startDate, endDate);
                break;
            case "staff":
                data = getStaffPerformanceAnalytics(startDate, endDate);
                break;
            default:
                data = getDashboardAnalytics(startDate, endDate);
                break;
        }
        
        String reportContent = data.toString(); // Simplified - would use proper reporting library
        return reportContent.getBytes();
    }

    /**
     * Get custom analytics
     */
    public Map<String, Object> getCustomAnalytics(Map<String, Object> queryParams) {
        // This would implement custom queries based on parameters
        // For now, return basic analytics
        return getDashboardAnalytics(null, null);
    }
}
