package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.UserRepository;
import com.hostel.repository.AssetRepository;
import com.hostel.repository.CategoryStaffMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Advanced Analytics Service implementing comprehensive reporting and analytics
 * as per IIM Trichy Product Design Document Section 4.3.3 and Section 9
 */
@Service
public class AdvancedAnalyticsService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CategoryStaffMappingRepository mappingRepository;

    @Autowired
    private QualityAssuranceService qualityAssuranceService;

    /**
     * Generate comprehensive operational dashboard data
     * Implements dashboard metrics from PDD Section 4.3.3
     */
    public OperationalDashboard generateOperationalDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // Key Performance Indicators
        int totalActiveTickets = ticketRepository.countByStatusIn(
            Arrays.asList(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
        
        int totalTicketsThisMonth = ticketRepository.countByCreatedAtAfter(thirtyDaysAgo);
        int resolvedTicketsThisMonth = ticketRepository.countByStatusAndResolvedAtAfter(
            TicketStatus.CLOSED, thirtyDaysAgo);
        
        double resolutionRate = totalTicketsThisMonth > 0 ? 
            (double) resolvedTicketsThisMonth / totalTicketsThisMonth * 100 : 0;

        // Average resolution time
        double avgResolutionHours = calculateAverageResolutionTime(thirtyDaysAgo, now);

        // SLA compliance
        double slaCompliance = calculateSLACompliance(thirtyDaysAgo, now);

        // Staff utilization
        Map<String, Integer> staffUtilization = calculateStaffUtilization();

        // Category distribution
        Map<String, Integer> categoryDistribution = calculateCategoryDistribution(thirtyDaysAgo, now);

        // Priority distribution
        Map<String, Integer> priorityDistribution = calculatePriorityDistribution();

        // Trend data (last 7 days)
        List<DailyMetric> weeklyTrend = calculateWeeklyTrend(sevenDaysAgo, now);

        // Top issues by location
        List<LocationIssue> topLocationIssues = calculateTopLocationIssues(thirtyDaysAgo, now);

        return new OperationalDashboard(
            totalActiveTickets,
            totalTicketsThisMonth,
            resolvedTicketsThisMonth,
            resolutionRate,
            avgResolutionHours,
            slaCompliance,
            staffUtilization,
            categoryDistribution,
            priorityDistribution,
            weeklyTrend,
            topLocationIssues
        );
    }

    /**
     * Generate staff performance analytics
     * Implements performance metrics from PDD Section 4.3.3
     */
    public List<StaffPerformanceReport> generateStaffPerformanceReport(LocalDateTime fromDate, LocalDateTime toDate) {
        List<User> staffMembers = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        List<StaffPerformanceReport> reports = new ArrayList<>();

        for (User staff : staffMembers) {
            List<Ticket> staffTickets = ticketRepository.findByAssignedToAndCreatedAtBetween(staff, fromDate, toDate);
            
            int totalTickets = staffTickets.size();
            int completedTickets = (int) staffTickets.stream()
                .filter(ticket -> ticket.getStatus().equals(TicketStatus.CLOSED))
                .count();
            
            int overdueTickets = (int) staffTickets.stream()
                .filter(this::isTicketOverdue)
                .count();

            double avgSatisfactionRating = staffTickets.stream()
                .filter(ticket -> ticket.getSatisfactionRating() != null)
                .mapToDouble(Ticket::getSatisfactionRating)
                .average()
                .orElse(0.0);

            double avgResolutionTime = staffTickets.stream()
                .filter(ticket -> ticket.getStartedAt() != null && ticket.getResolvedAt() != null)
                .mapToDouble(ticket -> ChronoUnit.HOURS.between(ticket.getStartedAt(), ticket.getResolvedAt()))
                .average()
                .orElse(0.0);

            // Calculate quality metrics
            QualityAssuranceService.QualityMetrics qualityMetrics = 
                qualityAssuranceService.calculateStaffQualityMetrics(staff);

            // Calculate workload score
            double workloadScore = calculateWorkloadScore(staff);

            // Expertise areas
            List<String> expertiseAreas = getStaffExpertiseAreas(staff);

            reports.add(new StaffPerformanceReport(
                staff.getId(),
                staff.getFullName(),
                staff.getStaffVertical() != null ? staff.getStaffVertical().toString() : "General",
                totalTickets,
                completedTickets,
                overdueTickets,
                avgSatisfactionRating,
                avgResolutionTime,
                qualityMetrics,
                workloadScore,
                expertiseAreas
            ));
        }

        return reports.stream()
            .sorted(Comparator.comparingDouble(report -> report.getQualityMetrics().getOverallQualityScore()))
            .collect(Collectors.toList());
    }

    /**
     * Generate trend analysis report
     * Implements trend analysis from PDD Section 12.6.1
     */
    public TrendAnalysisReport generateTrendAnalysis(LocalDateTime fromDate, LocalDateTime toDate) {
        // Ticket volume trends
        Map<String, List<DailyMetric>> volumeTrends = calculateVolumetrends(fromDate, toDate);

        // Category trends
        Map<String, Integer> categoryTrends = calculateCategoryTrends(fromDate, toDate);

        // Resolution time trends
        List<ResolutionTimeTrend> resolutionTimeTrends = calculateResolutionTimeTrends(fromDate, toDate);

        // Satisfaction trends
        List<SatisfactionTrend> satisfactionTrends = calculateSatisfactionTrends(fromDate, toDate);

        // Seasonal patterns
        Map<String, Double> seasonalPatterns = calculateSeasonalPatterns(fromDate, toDate);

        // Predictive insights
        List<PredictiveInsight> predictiveInsights = generatePredictiveInsights(fromDate, toDate);

        return new TrendAnalysisReport(
            volumeTrends,
            categoryTrends,
            resolutionTimeTrends,
            satisfactionTrends,
            seasonalPatterns,
            predictiveInsights
        );
    }

    /**
     * Generate cost analysis report
     * Implements cost tracking from PDD Section 9.2.1
     */
    public CostAnalysisReport generateCostAnalysis(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(fromDate, toDate);

        // Total costs
        BigDecimal totalEstimatedCost = tickets.stream()
            .map(Ticket::getEstimatedCost)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualCost = tickets.stream()
            .map(Ticket::getActualCost)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cost by category
        Map<String, BigDecimal> costByCategory = tickets.stream()
            .filter(ticket -> ticket.getActualCost() != null)
            .collect(Collectors.groupingBy(
                Ticket::getEffectiveCategory,
                Collectors.reducing(BigDecimal.ZERO, Ticket::getActualCost, BigDecimal::add)
            ));

        // Cost variance analysis
        BigDecimal costVariance = totalActualCost.subtract(totalEstimatedCost);
        double variancePercentage = totalEstimatedCost.compareTo(BigDecimal.ZERO) != 0 ?
            costVariance.divide(totalEstimatedCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0;

        // High-cost tickets
        List<Ticket> highCostTickets = tickets.stream()
            .filter(ticket -> ticket.getActualCost() != null && 
                ticket.getActualCost().compareTo(BigDecimal.valueOf(5000)) > 0)
            .sorted(Comparator.comparing(Ticket::getActualCost, Comparator.reverseOrder()))
            .limit(10)
            .collect(Collectors.toList());

        // Cost efficiency metrics
        double avgCostPerTicket = tickets.size() > 0 && totalActualCost.compareTo(BigDecimal.ZERO) > 0 ?
            totalActualCost.divide(BigDecimal.valueOf(tickets.size()), 2, RoundingMode.HALF_UP).doubleValue() : 0;

        // Preventive maintenance savings (estimated)
        BigDecimal preventiveMaintenanceSavings = calculatePreventiveMaintenanceSavings(fromDate, toDate);

        return new CostAnalysisReport(
            totalEstimatedCost,
            totalActualCost,
            costByCategory,
            costVariance,
            variancePercentage,
            highCostTickets,
            avgCostPerTicket,
            preventiveMaintenanceSavings
        );
    }

    /**
     * Generate asset utilization report
     */
    public AssetUtilizationReport generateAssetUtilizationReport() {
        List<Asset> allAssets = assetRepository.findAll();
        
        Map<AssetType, Long> assetTypeDistribution = allAssets.stream()
            .collect(Collectors.groupingBy(Asset::getType, Collectors.counting()));

        Map<AssetStatus, Long> assetStatusDistribution = allAssets.stream()
            .collect(Collectors.groupingBy(Asset::getStatus, Collectors.counting()));

        List<Asset> underutilizedAssets = findUnderutilizedAssets();
        List<Asset> overutilizedAssets = findOverutilizedAssets();

        // Asset maintenance costs
        BigDecimal totalMaintenanceCost = calculateAssetMaintenanceCosts();

        // Asset age analysis
        Map<String, Integer> assetAgeDistribution = calculateAssetAgeDistribution(allAssets);

        return new AssetUtilizationReport(
            assetTypeDistribution,
            assetStatusDistribution,
            underutilizedAssets,
            overutilizedAssets,
            totalMaintenanceCost,
            assetAgeDistribution
        );
    }

    // Helper methods

    private double calculateAverageResolutionTime(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> resolvedTickets = ticketRepository.findResolvedTicketsBetween(fromDate, toDate);
        return resolvedTickets.stream()
            .filter(ticket -> ticket.getCreatedAt() != null && ticket.getResolvedAt() != null)
            .mapToDouble(ticket -> ChronoUnit.HOURS.between(ticket.getCreatedAt(), ticket.getResolvedAt()))
            .average()
            .orElse(0.0);
    }

    private double calculateSLACompliance(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(fromDate, toDate);
        long compliantTickets = tickets.stream()
            .filter(this::isSLACompliant)
            .count();
        
        return tickets.size() > 0 ? (double) compliantTickets / tickets.size() * 100 : 0;
    }

    private boolean isSLACompliant(Ticket ticket) {
        if (ticket.getEstimatedResolutionTime() == null || ticket.getActualResolutionTime() == null) {
            return false;
        }
        return !ticket.getActualResolutionTime().isAfter(ticket.getEstimatedResolutionTime());
    }

    private boolean isTicketOverdue(Ticket ticket) {
        return ticket.getEstimatedResolutionTime() != null &&
               ticket.getEstimatedResolutionTime().isBefore(LocalDateTime.now()) &&
               !Arrays.asList(TicketStatus.RESOLVED, TicketStatus.CLOSED).contains(ticket.getStatus());
    }

    private Map<String, Integer> calculateStaffUtilization() {
        List<User> staffMembers = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        Map<String, Integer> utilization = new HashMap<>();
        
        for (User staff : staffMembers) {
            int activeTickets = ticketRepository.countByAssignedToAndStatusIn(
                staff, Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
            utilization.put(staff.getFullName(), activeTickets);
        }
        
        return utilization;
    }

    private Map<String, Integer> calculateCategoryDistribution(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(fromDate, toDate);
        return tickets.stream()
            .collect(Collectors.groupingBy(
                Ticket::getEffectiveCategory,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
    }

    private Map<String, Integer> calculatePriorityDistribution() {
        List<Ticket> activeTickets = ticketRepository.findByStatusIn(
            Arrays.asList(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
        
        return activeTickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getPriority().toString(),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));
    }

    private List<DailyMetric> calculateWeeklyTrend(LocalDateTime fromDate, LocalDateTime toDate) {
        List<DailyMetric> metrics = new ArrayList<>();
        LocalDateTime currentDate = fromDate;
        
        while (!currentDate.isAfter(toDate)) {
            LocalDateTime nextDay = currentDate.plusDays(1);
            
            int created = (int) ticketRepository.countByCreatedAtBetween(currentDate, nextDay);
            int resolved = ticketRepository.countByResolvedAtBetween(currentDate, nextDay);
            
            metrics.add(new DailyMetric(currentDate.toLocalDate(), created, resolved));
            currentDate = nextDay;
        }
        
        return metrics;
    }

    private List<LocationIssue> calculateTopLocationIssues(LocalDateTime fromDate, LocalDateTime toDate) {
        List<Ticket> tickets = ticketRepository.findByCreatedAtBetween(fromDate, toDate);
        
        Map<String, Long> locationCounts = tickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getHostelBlock() + (ticket.getRoomNumber() != null ? " - " + ticket.getRoomNumber() : ""),
                Collectors.counting()
            ));
        
        return locationCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(entry -> new LocationIssue(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }

    private double calculateWorkloadScore(User staff) {
        int activeTickets = ticketRepository.countByAssignedToAndStatusIn(
            staff, Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
        int overdueTickets = (int) ticketRepository.findByAssignedTo(staff).stream()
            .filter(this::isTicketOverdue)
            .count();
        
        return activeTickets * 0.6 + overdueTickets * 0.4;
    }

    private List<String> getStaffExpertiseAreas(User staff) {
        List<CategoryStaffMapping> mappings = mappingRepository.findByStaffAndIsActiveTrue(staff);
        return mappings.stream()
            .map(CategoryStaffMapping::getCategory)
            .distinct()
            .collect(Collectors.toList());
    }

    private Map<String, List<DailyMetric>> calculateVolumetrends(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for volume trends by category, priority, etc.
        Map<String, List<DailyMetric>> trends = new HashMap<>();
        // This would be implemented based on specific requirements
        return trends;
    }

    private Map<String, Integer> calculateCategoryTrends(LocalDateTime fromDate, LocalDateTime toDate) {
        return calculateCategoryDistribution(fromDate, toDate);
    }

    private List<ResolutionTimeTrend> calculateResolutionTimeTrends(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for resolution time trends
        return new ArrayList<>();
    }

    private List<SatisfactionTrend> calculateSatisfactionTrends(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for satisfaction trends
        return new ArrayList<>();
    }

    private Map<String, Double> calculateSeasonalPatterns(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for seasonal pattern analysis
        return new HashMap<>();
    }

    private List<PredictiveInsight> generatePredictiveInsights(LocalDateTime fromDate, LocalDateTime toDate) {
        // Implementation for predictive analytics
        return new ArrayList<>();
    }

    private BigDecimal calculatePreventiveMaintenanceSavings(LocalDateTime fromDate, LocalDateTime toDate) {
        // Estimate savings from preventive maintenance
        return BigDecimal.ZERO;
    }

    private List<Asset> findUnderutilizedAssets() {
        // Find assets with low usage or maintenance
        return new ArrayList<>();
    }

    private List<Asset> findOverutilizedAssets() {
        // Find assets with high maintenance or frequent issues
        return new ArrayList<>();
    }

    private BigDecimal calculateAssetMaintenanceCosts() {
        // Calculate total maintenance costs
        return BigDecimal.ZERO;
    }

    private Map<String, Integer> calculateAssetAgeDistribution(List<Asset> assets) {
        // Calculate asset age distribution
        return new HashMap<>();
    }

    // Data classes for reports (inner classes for brevity)
    
    public static class OperationalDashboard {
        // Implementation of dashboard data structure
        private final int totalActiveTickets;
        private final int totalTicketsThisMonth;
        private final int resolvedTicketsThisMonth;
        private final double resolutionRate;
        private final double avgResolutionHours;
        private final double slaCompliance;
        private final Map<String, Integer> staffUtilization;
        private final Map<String, Integer> categoryDistribution;
        private final Map<String, Integer> priorityDistribution;
        private final List<DailyMetric> weeklyTrend;
        private final List<LocationIssue> topLocationIssues;

        public OperationalDashboard(int totalActiveTickets, int totalTicketsThisMonth, int resolvedTicketsThisMonth,
                                  double resolutionRate, double avgResolutionHours, double slaCompliance,
                                  Map<String, Integer> staffUtilization, Map<String, Integer> categoryDistribution,
                                  Map<String, Integer> priorityDistribution, List<DailyMetric> weeklyTrend,
                                  List<LocationIssue> topLocationIssues) {
            this.totalActiveTickets = totalActiveTickets;
            this.totalTicketsThisMonth = totalTicketsThisMonth;
            this.resolvedTicketsThisMonth = resolvedTicketsThisMonth;
            this.resolutionRate = resolutionRate;
            this.avgResolutionHours = avgResolutionHours;
            this.slaCompliance = slaCompliance;
            this.staffUtilization = staffUtilization;
            this.categoryDistribution = categoryDistribution;
            this.priorityDistribution = priorityDistribution;
            this.weeklyTrend = weeklyTrend;
            this.topLocationIssues = topLocationIssues;
        }

        // Getters
        public int getTotalActiveTickets() { return totalActiveTickets; }
        public int getTotalTicketsThisMonth() { return totalTicketsThisMonth; }
        public int getResolvedTicketsThisMonth() { return resolvedTicketsThisMonth; }
        public double getResolutionRate() { return resolutionRate; }
        public double getAvgResolutionHours() { return avgResolutionHours; }
        public double getSlaCompliance() { return slaCompliance; }
        public Map<String, Integer> getStaffUtilization() { return staffUtilization; }
        public Map<String, Integer> getCategoryDistribution() { return categoryDistribution; }
        public Map<String, Integer> getPriorityDistribution() { return priorityDistribution; }
        public List<DailyMetric> getWeeklyTrend() { return weeklyTrend; }
        public List<LocationIssue> getTopLocationIssues() { return topLocationIssues; }
    }

    // Additional data classes would be implemented here...
    public static class StaffPerformanceReport { /* Implementation */ 
        private final UUID staffId;
        private final String staffName;
        private final String department;
        private final int totalTickets;
        private final int completedTickets;
        private final int overdueTickets;
        private final double avgSatisfactionRating;
        private final double avgResolutionTime;
        private final QualityAssuranceService.QualityMetrics qualityMetrics;
        private final double workloadScore;
        private final List<String> expertiseAreas;

        public StaffPerformanceReport(UUID staffId, String staffName, String department, int totalTickets,
                                    int completedTickets, int overdueTickets, double avgSatisfactionRating,
                                    double avgResolutionTime, QualityAssuranceService.QualityMetrics qualityMetrics,
                                    double workloadScore, List<String> expertiseAreas) {
            this.staffId = staffId;
            this.staffName = staffName;
            this.department = department;
            this.totalTickets = totalTickets;
            this.completedTickets = completedTickets;
            this.overdueTickets = overdueTickets;
            this.avgSatisfactionRating = avgSatisfactionRating;
            this.avgResolutionTime = avgResolutionTime;
            this.qualityMetrics = qualityMetrics;
            this.workloadScore = workloadScore;
            this.expertiseAreas = expertiseAreas;
        }

        // Getters
        public UUID getStaffId() { return staffId; }
        public String getStaffName() { return staffName; }
        public String getDepartment() { return department; }
        public int getTotalTickets() { return totalTickets; }
        public int getCompletedTickets() { return completedTickets; }
        public int getOverdueTickets() { return overdueTickets; }
        public double getAvgSatisfactionRating() { return avgSatisfactionRating; }
        public double getAvgResolutionTime() { return avgResolutionTime; }
        public QualityAssuranceService.QualityMetrics getQualityMetrics() { return qualityMetrics; }
        public double getWorkloadScore() { return workloadScore; }
        public List<String> getExpertiseAreas() { return expertiseAreas; }
    }
    
    public static class TrendAnalysisReport { /* Implementation */ 
        private final Map<String, List<DailyMetric>> volumeTrends;
        private final Map<String, Integer> categoryTrends;
        private final List<ResolutionTimeTrend> resolutionTimeTrends;
        private final List<SatisfactionTrend> satisfactionTrends;
        private final Map<String, Double> seasonalPatterns;
        private final List<PredictiveInsight> predictiveInsights;

        public TrendAnalysisReport(Map<String, List<DailyMetric>> volumeTrends, Map<String, Integer> categoryTrends,
                                 List<ResolutionTimeTrend> resolutionTimeTrends, List<SatisfactionTrend> satisfactionTrends,
                                 Map<String, Double> seasonalPatterns, List<PredictiveInsight> predictiveInsights) {
            this.volumeTrends = volumeTrends;
            this.categoryTrends = categoryTrends;
            this.resolutionTimeTrends = resolutionTimeTrends;
            this.satisfactionTrends = satisfactionTrends;
            this.seasonalPatterns = seasonalPatterns;
            this.predictiveInsights = predictiveInsights;
        }

        // Getters
        public Map<String, List<DailyMetric>> getVolumeTrends() { return volumeTrends; }
        public Map<String, Integer> getCategoryTrends() { return categoryTrends; }
        public List<ResolutionTimeTrend> getResolutionTimeTrends() { return resolutionTimeTrends; }
        public List<SatisfactionTrend> getSatisfactionTrends() { return satisfactionTrends; }
        public Map<String, Double> getSeasonalPatterns() { return seasonalPatterns; }
        public List<PredictiveInsight> getPredictiveInsights() { return predictiveInsights; }
    }
    
    public static class CostAnalysisReport { /* Implementation */
        private final BigDecimal totalEstimatedCost;
        private final BigDecimal totalActualCost;
        private final Map<String, BigDecimal> costByCategory;
        private final BigDecimal costVariance;
        private final double variancePercentage;
        private final List<Ticket> highCostTickets;
        private final double avgCostPerTicket;
        private final BigDecimal preventiveMaintenanceSavings;

        public CostAnalysisReport(BigDecimal totalEstimatedCost, BigDecimal totalActualCost,
                                Map<String, BigDecimal> costByCategory, BigDecimal costVariance,
                                double variancePercentage, List<Ticket> highCostTickets,
                                double avgCostPerTicket, BigDecimal preventiveMaintenanceSavings) {
            this.totalEstimatedCost = totalEstimatedCost;
            this.totalActualCost = totalActualCost;
            this.costByCategory = costByCategory;
            this.costVariance = costVariance;
            this.variancePercentage = variancePercentage;
            this.highCostTickets = highCostTickets;
            this.avgCostPerTicket = avgCostPerTicket;
            this.preventiveMaintenanceSavings = preventiveMaintenanceSavings;
        }

        // Getters
        public BigDecimal getTotalEstimatedCost() { return totalEstimatedCost; }
        public BigDecimal getTotalActualCost() { return totalActualCost; }
        public Map<String, BigDecimal> getCostByCategory() { return costByCategory; }
        public BigDecimal getCostVariance() { return costVariance; }
        public double getVariancePercentage() { return variancePercentage; }
        public List<Ticket> getHighCostTickets() { return highCostTickets; }
        public double getAvgCostPerTicket() { return avgCostPerTicket; }
        public BigDecimal getPreventiveMaintenanceSavings() { return preventiveMaintenanceSavings; }
    }
    
    public static class AssetUtilizationReport { /* Implementation */
        private final Map<AssetType, Long> assetTypeDistribution;
        private final Map<AssetStatus, Long> assetStatusDistribution;
        private final List<Asset> underutilizedAssets;
        private final List<Asset> overutilizedAssets;
        private final BigDecimal totalMaintenanceCost;
        private final Map<String, Integer> assetAgeDistribution;

        public AssetUtilizationReport(Map<AssetType, Long> assetTypeDistribution,
                                    Map<AssetStatus, Long> assetStatusDistribution,
                                    List<Asset> underutilizedAssets, List<Asset> overutilizedAssets,
                                    BigDecimal totalMaintenanceCost, Map<String, Integer> assetAgeDistribution) {
            this.assetTypeDistribution = assetTypeDistribution;
            this.assetStatusDistribution = assetStatusDistribution;
            this.underutilizedAssets = underutilizedAssets;
            this.overutilizedAssets = overutilizedAssets;
            this.totalMaintenanceCost = totalMaintenanceCost;
            this.assetAgeDistribution = assetAgeDistribution;
        }

        // Getters
        public Map<AssetType, Long> getAssetTypeDistribution() { return assetTypeDistribution; }
        public Map<AssetStatus, Long> getAssetStatusDistribution() { return assetStatusDistribution; }
        public List<Asset> getUnderutilizedAssets() { return underutilizedAssets; }
        public List<Asset> getOverutilizedAssets() { return overutilizedAssets; }
        public BigDecimal getTotalMaintenanceCost() { return totalMaintenanceCost; }
        public Map<String, Integer> getAssetAgeDistribution() { return assetAgeDistribution; }
    }

    // Simple data classes
    public static class DailyMetric {
        private final java.time.LocalDate date;
        private final int created;
        private final int resolved;

        public DailyMetric(java.time.LocalDate date, int created, int resolved) {
            this.date = date;
            this.created = created;
            this.resolved = resolved;
        }

        public java.time.LocalDate getDate() { return date; }
        public int getCreated() { return created; }
        public int getResolved() { return resolved; }
    }

    public static class LocationIssue {
        private final String location;
        private final int issueCount;

        public LocationIssue(String location, int issueCount) {
            this.location = location;
            this.issueCount = issueCount;
        }

        public String getLocation() { return location; }
        public int getIssueCount() { return issueCount; }
    }

    public static class ResolutionTimeTrend { /* Placeholder */ }
    public static class SatisfactionTrend { /* Placeholder */ }
    public static class PredictiveInsight { /* Placeholder */ }
}
