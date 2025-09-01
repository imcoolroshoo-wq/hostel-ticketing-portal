package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource Optimization and Capacity Planning Service implementing advanced resource management
 * as per IIM Trichy Product Design Document Section 12.5
 */
@Service
@Transactional
public class ResourceOptimizationService {

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryStaffMappingRepository mappingRepository;
    
    // Note: TicketAssignmentService available for future optimization features

    /**
     * Analyze staff utilization patterns and recommend optimizations
     */
    public ResourceOptimizationReport generateOptimizationReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        // Get all staff and their workload data
        List<User> allStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        Map<User, StaffUtilizationData> utilizationData = analyzeStaffUtilization(allStaff, startDate, endDate);
        
        // Analyze demand patterns
        DemandAnalysis demandAnalysis = analyzeDemandPatterns(startDate, endDate);
        
        // Generate capacity recommendations
        List<CapacityRecommendation> capacityRecommendations = generateCapacityRecommendations(utilizationData, demandAnalysis);
        
        // Analyze cross-training opportunities
        List<CrossTrainingRecommendation> crossTrainingRecommendations = analyzeCrossTrainingOpportunities(utilizationData, demandAnalysis);
        
        // Resource redistribution suggestions
        List<ResourceRedistribution> redistributionSuggestions = analyzeResourceRedistribution(utilizationData, demandAnalysis);
        
        // Peak time analysis
        PeakTimeAnalysis peakTimeAnalysis = analyzePeakTimes(startDate, endDate);
        
        return new ResourceOptimizationReport(
            utilizationData,
            demandAnalysis,
            capacityRecommendations,
            crossTrainingRecommendations,
            redistributionSuggestions,
            peakTimeAnalysis
        );
    }

    /**
     * Analyze staff utilization patterns
     */
    private Map<User, StaffUtilizationData> analyzeStaffUtilization(List<User> staff, LocalDate startDate, LocalDate endDate) {
        Map<User, StaffUtilizationData> utilizationData = new HashMap<>();
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        for (User staffMember : staff) {
            List<Ticket> assignedTickets = ticketRepository.findByAssignedToAndCreatedAtBetween(
                staffMember, startDateTime, endDateTime);
            
            // Calculate metrics
            int totalTickets = assignedTickets.size();
            int completedTickets = (int) assignedTickets.stream()
                .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
                .count();
            
            double avgResolutionTime = assignedTickets.stream()
                .filter(ticket -> ticket.getCreatedAt() != null && ticket.getResolvedAt() != null)
                .mapToDouble(ticket -> ChronoUnit.HOURS.between(ticket.getCreatedAt(), ticket.getResolvedAt()))
                .average()
                .orElse(0.0);
            
            // Calculate current active workload
            List<Ticket> activeTickets = ticketRepository.findByAssignedToAndStatusIn(
                staffMember, Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
            
            // Calculate utilization percentage
            int maxCapacity = getMaxCapacityForStaff(staffMember);
            double utilizationRate = (double) activeTickets.size() / maxCapacity * 100;
            
            // Calculate efficiency score
            double efficiencyScore = calculateEfficiencyScore(staffMember, assignedTickets);
            
            // Get skill categories
            List<String> skillCategories = mappingRepository.findByStaffAndIsActiveTrue(staffMember)
                .stream()
                .map(CategoryStaffMapping::getCategory)
                .collect(Collectors.toList());
            
            utilizationData.put(staffMember, new StaffUtilizationData(
                staffMember,
                totalTickets,
                completedTickets,
                activeTickets.size(),
                avgResolutionTime,
                utilizationRate,
                efficiencyScore,
                skillCategories
            ));
        }
        
        return utilizationData;
    }

    /**
     * Analyze demand patterns across categories and time periods
     */
    private DemandAnalysis analyzeDemandPatterns(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> allTickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Demand by category
        Map<String, Long> demandByCategory = allTickets.stream()
            .collect(Collectors.groupingBy(Ticket::getEffectiveCategory, Collectors.counting()));
        
        // Demand by hostel
        Map<String, Long> demandByHostel = allTickets.stream()
            .filter(ticket -> ticket.getHostelBlock() != null)
            .collect(Collectors.groupingBy(Ticket::getHostelBlock, Collectors.counting()));
        
        // Demand by day of week
        Map<DayOfWeek, Long> demandByDayOfWeek = allTickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getCreatedAt().getDayOfWeek(),
                Collectors.counting()
            ));
        
        // Demand by hour of day
        Map<Integer, Long> demandByHour = allTickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getCreatedAt().getHour(),
                Collectors.counting()
            ));
        
        // Seasonal trends (compare with previous periods)
        Map<String, Double> seasonalTrends = calculateSeasonalTrends(allTickets, startDate, endDate);
        
        return new DemandAnalysis(
            demandByCategory,
            demandByHostel,
            demandByDayOfWeek,
            demandByHour,
            seasonalTrends
        );
    }

    /**
     * Generate capacity recommendations based on utilization and demand
     */
    private List<CapacityRecommendation> generateCapacityRecommendations(
            Map<User, StaffUtilizationData> utilizationData, DemandAnalysis demandAnalysis) {
        
        List<CapacityRecommendation> recommendations = new ArrayList<>();
        
        // Identify over-utilized staff
        utilizationData.entrySet().stream()
            .filter(entry -> entry.getValue().getUtilizationRate() > 90)
            .forEach(entry -> {
                recommendations.add(new CapacityRecommendation(
                    CapacityRecommendation.Type.INCREASE_CAPACITY,
                    "Staff member " + entry.getKey().getUsername() + " is over-utilized at " + 
                    String.format("%.1f", entry.getValue().getUtilizationRate()) + "%",
                    CapacityRecommendation.Priority.HIGH,
                    "Consider hiring additional staff or redistributing workload for " + 
                    String.join(", ", entry.getValue().getSkillCategories())
                ));
            });
        
        // Identify under-utilized staff
        utilizationData.entrySet().stream()
            .filter(entry -> entry.getValue().getUtilizationRate() < 30)
            .forEach(entry -> {
                recommendations.add(new CapacityRecommendation(
                    CapacityRecommendation.Type.OPTIMIZE_UTILIZATION,
                    "Staff member " + entry.getKey().getUsername() + " is under-utilized at " + 
                    String.format("%.1f", entry.getValue().getUtilizationRate()) + "%",
                    CapacityRecommendation.Priority.MEDIUM,
                    "Consider cross-training for high-demand categories or reassigning responsibilities"
                ));
            });
        
        // Category demand vs capacity analysis
        for (Map.Entry<String, Long> entry : demandAnalysis.getDemandByCategory().entrySet()) {
            String category = entry.getKey();
            Long demand = entry.getValue();
            
            long availableStaff = mappingRepository.findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc(category).size();
            
            if (availableStaff == 0) {
                recommendations.add(new CapacityRecommendation(
                    CapacityRecommendation.Type.INCREASE_CAPACITY,
                    "No staff mapped to category: " + category,
                    CapacityRecommendation.Priority.HIGH,
                    "Assign staff members to handle " + category + " issues (" + demand + " tickets in analysis period)"
                ));
            } else if (demand / availableStaff > 10) { // More than 10 tickets per staff member
                recommendations.add(new CapacityRecommendation(
                    CapacityRecommendation.Type.INCREASE_CAPACITY,
                    "High demand for category: " + category,
                    CapacityRecommendation.Priority.MEDIUM,
                    "Consider adding more staff to " + category + " (current ratio: " + 
                    (demand / availableStaff) + " tickets per staff)"
                ));
            }
        }
        
        return recommendations;
    }

    /**
     * Analyze cross-training opportunities
     */
    private List<CrossTrainingRecommendation> analyzeCrossTrainingOpportunities(
            Map<User, StaffUtilizationData> utilizationData, DemandAnalysis demandAnalysis) {
        
        List<CrossTrainingRecommendation> recommendations = new ArrayList<>();
        
        // Find under-utilized staff and high-demand categories
        List<User> underUtilizedStaff = utilizationData.entrySet().stream()
            .filter(entry -> entry.getValue().getUtilizationRate() < 50)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        List<String> highDemandCategories = demandAnalysis.getDemandByCategory().entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (User staff : underUtilizedStaff) {
            List<String> currentSkills = utilizationData.get(staff).getSkillCategories();
            
            for (String highDemandCategory : highDemandCategories) {
                if (!currentSkills.contains(highDemandCategory)) {
                    // Check if category is related to existing skills
                    boolean isRelatedSkill = isRelatedCategory(currentSkills, highDemandCategory);
                    
                    recommendations.add(new CrossTrainingRecommendation(
                        staff,
                        highDemandCategory,
                        isRelatedSkill ? CrossTrainingRecommendation.Difficulty.EASY : CrossTrainingRecommendation.Difficulty.MEDIUM,
                        "Cross-train " + staff.getUsername() + " in " + highDemandCategory + 
                        " to optimize utilization (current: " + 
                        String.format("%.1f", utilizationData.get(staff).getUtilizationRate()) + "%)",
                        demandAnalysis.getDemandByCategory().get(highDemandCategory).intValue()
                    ));
                }
            }
        }
        
        return recommendations;
    }

    /**
     * Analyze resource redistribution opportunities
     */
    private List<ResourceRedistribution> analyzeResourceRedistribution(
            Map<User, StaffUtilizationData> utilizationData, DemandAnalysis demandAnalysis) {
        
        List<ResourceRedistribution> redistributions = new ArrayList<>();
        
        // Analyze hostel-level demand vs staff distribution
        for (Map.Entry<String, Long> entry : demandAnalysis.getDemandByHostel().entrySet()) {
            String hostel = entry.getKey();
            Long demand = entry.getValue();
            
            try {
                HostelName hostelName = HostelName.fromAnyName(hostel);
                long staffInHostel = mappingRepository.findByHostelBlockAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(hostelName, null)
                    .stream()
                    .map(CategoryStaffMapping::getStaff)
                    .distinct()
                    .count();
                
                if (staffInHostel == 0 && demand > 5) {
                    redistributions.add(new ResourceRedistribution(
                        ResourceRedistribution.Type.ASSIGN_STAFF_TO_HOSTEL,
                        "No dedicated staff for " + hostel + " despite " + demand + " tickets",
                        ResourceRedistribution.Priority.HIGH,
                        "Assign staff members to " + hostel + " for better response times"
                    ));
                }
            } catch (Exception e) {
                // Invalid hostel name, skip
            }
        }
        
        // Time-based redistribution opportunities
        Map<Integer, Long> hourlyDemand = demandAnalysis.getDemandByHour();
        int peakHour = hourlyDemand.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(9); // Default to 9 AM
        
        int lowHour = hourlyDemand.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(22); // Default to 10 PM
        
        if (hourlyDemand.get(peakHour) > hourlyDemand.get(lowHour) * 3) {
            redistributions.add(new ResourceRedistribution(
                ResourceRedistribution.Type.ADJUST_SHIFT_SCHEDULES,
                "High demand variation between peak (" + peakHour + ":00) and low (" + lowHour + ":00) hours",
                ResourceRedistribution.Priority.MEDIUM,
                "Consider adjusting staff schedules to better match demand patterns"
            ));
        }
        
        return redistributions;
    }

    /**
     * Analyze peak time patterns
     */
    private PeakTimeAnalysis analyzePeakTimes(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Ticket> allTickets = ticketRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        
        // Find peak hours
        Map<Integer, Long> hourlyDistribution = allTickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getCreatedAt().getHour(),
                Collectors.counting()
            ));
        
        List<Integer> peakHours = hourlyDistribution.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Find peak days of week
        Map<DayOfWeek, Long> dailyDistribution = allTickets.stream()
            .collect(Collectors.groupingBy(
                ticket -> ticket.getCreatedAt().getDayOfWeek(),
                Collectors.counting()
            ));
        
        List<DayOfWeek> peakDays = dailyDistribution.entrySet().stream()
            .sorted(Map.Entry.<DayOfWeek, Long>comparingByValue().reversed())
            .limit(2)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Calculate peak time capacity requirements
        long averageHourlyDemand = allTickets.size() / 24; // Simplified
        long peakHourDemand = peakHours.stream()
            .mapToLong(hour -> hourlyDistribution.getOrDefault(hour, 0L))
            .max()
            .orElse(0);
        
        double peakCapacityMultiplier = averageHourlyDemand > 0 ? 
            (double) peakHourDemand / averageHourlyDemand : 1.0;
        
        return new PeakTimeAnalysis(
            peakHours,
            peakDays,
            hourlyDistribution,
            dailyDistribution,
            peakCapacityMultiplier
        );
    }

    // Helper methods
    private int getMaxCapacityForStaff(User staff) {
        if (staff.getStaffVertical() != null) {
            switch (staff.getStaffVertical()) {
                case HOSTEL_WARDEN:
                case BLOCK_SUPERVISOR:
                    return 12;
                case ELECTRICAL:
                case PLUMBING:
                case HVAC:
                case IT_SUPPORT:
                    return 8;
                default:
                    return 5;
            }
        }
        return 5;
    }

    private double calculateEfficiencyScore(User staff, List<Ticket> tickets) {
        if (tickets.isEmpty()) return 50.0; // Neutral score
        
        double avgSatisfaction = tickets.stream()
            .filter(ticket -> ticket.getSatisfactionRating() != null)
            .mapToInt(Ticket::getSatisfactionRating)
            .average()
            .orElse(3.0);
        
        long onTimeResolutions = tickets.stream()
            .filter(ticket -> ticket.getResolvedAt() != null && ticket.getEstimatedResolutionTime() != null)
            .filter(ticket -> !ticket.getResolvedAt().isAfter(ticket.getEstimatedResolutionTime()))
            .count();
        
        double onTimeRate = tickets.size() > 0 ? (double) onTimeResolutions / tickets.size() : 0.5;
        
        // Efficiency score = (satisfaction/5 * 50) + (on-time rate * 50)
        return (avgSatisfaction / 5.0 * 50) + (onTimeRate * 50);
    }

    private boolean isRelatedCategory(List<String> currentSkills, String targetCategory) {
        // Define related categories
        Map<String, List<String>> relatedCategories = Map.of(
            "ELECTRICAL_ISSUES", Arrays.asList("GENERAL", "HVAC"),
            "PLUMBING_WATER", Arrays.asList("GENERAL", "STRUCTURAL_CIVIL"),
            "HVAC", Arrays.asList("ELECTRICAL_ISSUES", "GENERAL"),
            "NETWORK_INTERNET", Arrays.asList("COMPUTER_HARDWARE", "SECURITY_SYSTEMS"),
            "COMPUTER_HARDWARE", Arrays.asList("NETWORK_INTERNET", "AUDIO_VISUAL_EQUIPMENT")
        );
        
        return currentSkills.stream()
            .anyMatch(skill -> relatedCategories.getOrDefault(targetCategory, Collections.emptyList()).contains(skill));
    }

    private Map<String, Double> calculateSeasonalTrends(List<Ticket> tickets, LocalDate startDate, LocalDate endDate) {
        // Simplified seasonal trend calculation
        Map<String, Double> trends = new HashMap<>();
        
        // Compare current period with previous period of same length
        long periodDays = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate previousStart = startDate.minusDays(periodDays);
        LocalDate previousEnd = startDate.minusDays(1);
        
        List<Ticket> previousPeriodTickets = ticketRepository.findByCreatedAtBetween(
            previousStart.atStartOfDay(), previousEnd.atTime(23, 59, 59));
        
        double currentVolume = tickets.size();
        double previousVolume = previousPeriodTickets.size();
        
        double growthRate = previousVolume > 0 ? 
            ((currentVolume - previousVolume) / previousVolume) * 100 : 0;
        
        trends.put("volume_growth_rate", growthRate);
        
        return trends;
    }

    // Supporting classes
    public static class ResourceOptimizationReport {
        private Map<User, StaffUtilizationData> utilizationData;
        private DemandAnalysis demandAnalysis;
        private List<CapacityRecommendation> capacityRecommendations;
        private List<CrossTrainingRecommendation> crossTrainingRecommendations;
        private List<ResourceRedistribution> redistributionSuggestions;
        private PeakTimeAnalysis peakTimeAnalysis;

        public ResourceOptimizationReport(Map<User, StaffUtilizationData> utilizationData,
                                         DemandAnalysis demandAnalysis,
                                         List<CapacityRecommendation> capacityRecommendations,
                                         List<CrossTrainingRecommendation> crossTrainingRecommendations,
                                         List<ResourceRedistribution> redistributionSuggestions,
                                         PeakTimeAnalysis peakTimeAnalysis) {
            this.utilizationData = utilizationData;
            this.demandAnalysis = demandAnalysis;
            this.capacityRecommendations = capacityRecommendations;
            this.crossTrainingRecommendations = crossTrainingRecommendations;
            this.redistributionSuggestions = redistributionSuggestions;
            this.peakTimeAnalysis = peakTimeAnalysis;
        }

        // Getters
        public Map<User, StaffUtilizationData> getUtilizationData() { return utilizationData; }
        public DemandAnalysis getDemandAnalysis() { return demandAnalysis; }
        public List<CapacityRecommendation> getCapacityRecommendations() { return capacityRecommendations; }
        public List<CrossTrainingRecommendation> getCrossTrainingRecommendations() { return crossTrainingRecommendations; }
        public List<ResourceRedistribution> getRedistributionSuggestions() { return redistributionSuggestions; }
        public PeakTimeAnalysis getPeakTimeAnalysis() { return peakTimeAnalysis; }
    }

    public static class StaffUtilizationData {
        private User staff;
        private int totalTickets;
        private int completedTickets;
        private int activeTickets;
        private double avgResolutionTime;
        private double utilizationRate;
        private double efficiencyScore;
        private List<String> skillCategories;

        public StaffUtilizationData(User staff, int totalTickets, int completedTickets, int activeTickets,
                                   double avgResolutionTime, double utilizationRate, double efficiencyScore,
                                   List<String> skillCategories) {
            this.staff = staff;
            this.totalTickets = totalTickets;
            this.completedTickets = completedTickets;
            this.activeTickets = activeTickets;
            this.avgResolutionTime = avgResolutionTime;
            this.utilizationRate = utilizationRate;
            this.efficiencyScore = efficiencyScore;
            this.skillCategories = skillCategories;
        }

        // Getters
        public User getStaff() { return staff; }
        public int getTotalTickets() { return totalTickets; }
        public int getCompletedTickets() { return completedTickets; }
        public int getActiveTickets() { return activeTickets; }
        public double getAvgResolutionTime() { return avgResolutionTime; }
        public double getUtilizationRate() { return utilizationRate; }
        public double getEfficiencyScore() { return efficiencyScore; }
        public List<String> getSkillCategories() { return skillCategories; }
    }

    public static class DemandAnalysis {
        private Map<String, Long> demandByCategory;
        private Map<String, Long> demandByHostel;
        private Map<DayOfWeek, Long> demandByDayOfWeek;
        private Map<Integer, Long> demandByHour;
        private Map<String, Double> seasonalTrends;

        public DemandAnalysis(Map<String, Long> demandByCategory, Map<String, Long> demandByHostel,
                             Map<DayOfWeek, Long> demandByDayOfWeek, Map<Integer, Long> demandByHour,
                             Map<String, Double> seasonalTrends) {
            this.demandByCategory = demandByCategory;
            this.demandByHostel = demandByHostel;
            this.demandByDayOfWeek = demandByDayOfWeek;
            this.demandByHour = demandByHour;
            this.seasonalTrends = seasonalTrends;
        }

        // Getters
        public Map<String, Long> getDemandByCategory() { return demandByCategory; }
        public Map<String, Long> getDemandByHostel() { return demandByHostel; }
        public Map<DayOfWeek, Long> getDemandByDayOfWeek() { return demandByDayOfWeek; }
        public Map<Integer, Long> getDemandByHour() { return demandByHour; }
        public Map<String, Double> getSeasonalTrends() { return seasonalTrends; }
    }

    public static class CapacityRecommendation {
        private Type type;
        private String description;
        private Priority priority;
        private String actionPlan;

        public enum Type {
            INCREASE_CAPACITY, OPTIMIZE_UTILIZATION, REDISTRIBUTE_WORKLOAD
        }

        public enum Priority {
            LOW, MEDIUM, HIGH
        }

        public CapacityRecommendation(Type type, String description, Priority priority, String actionPlan) {
            this.type = type;
            this.description = description;
            this.priority = priority;
            this.actionPlan = actionPlan;
        }

        // Getters
        public Type getType() { return type; }
        public String getDescription() { return description; }
        public Priority getPriority() { return priority; }
        public String getActionPlan() { return actionPlan; }
    }

    public static class CrossTrainingRecommendation {
        private User staff;
        private String targetCategory;
        private Difficulty difficulty;
        private String description;
        private int demandCount;

        public enum Difficulty {
            EASY, MEDIUM, HARD
        }

        public CrossTrainingRecommendation(User staff, String targetCategory, Difficulty difficulty,
                                          String description, int demandCount) {
            this.staff = staff;
            this.targetCategory = targetCategory;
            this.difficulty = difficulty;
            this.description = description;
            this.demandCount = demandCount;
        }

        // Getters
        public User getStaff() { return staff; }
        public String getTargetCategory() { return targetCategory; }
        public Difficulty getDifficulty() { return difficulty; }
        public String getDescription() { return description; }
        public int getDemandCount() { return demandCount; }
    }

    public static class ResourceRedistribution {
        private Type type;
        private String description;
        private Priority priority;
        private String recommendation;

        public enum Type {
            ASSIGN_STAFF_TO_HOSTEL, ADJUST_SHIFT_SCHEDULES, CROSS_TRAIN_STAFF
        }

        public enum Priority {
            LOW, MEDIUM, HIGH
        }

        public ResourceRedistribution(Type type, String description, Priority priority, String recommendation) {
            this.type = type;
            this.description = description;
            this.priority = priority;
            this.recommendation = recommendation;
        }

        // Getters
        public Type getType() { return type; }
        public String getDescription() { return description; }
        public Priority getPriority() { return priority; }
        public String getRecommendation() { return recommendation; }
    }

    public static class PeakTimeAnalysis {
        private List<Integer> peakHours;
        private List<DayOfWeek> peakDays;
        private Map<Integer, Long> hourlyDistribution;
        private Map<DayOfWeek, Long> dailyDistribution;
        private double peakCapacityMultiplier;

        public PeakTimeAnalysis(List<Integer> peakHours, List<DayOfWeek> peakDays,
                               Map<Integer, Long> hourlyDistribution, Map<DayOfWeek, Long> dailyDistribution,
                               double peakCapacityMultiplier) {
            this.peakHours = peakHours;
            this.peakDays = peakDays;
            this.hourlyDistribution = hourlyDistribution;
            this.dailyDistribution = dailyDistribution;
            this.peakCapacityMultiplier = peakCapacityMultiplier;
        }

        // Getters
        public List<Integer> getPeakHours() { return peakHours; }
        public List<DayOfWeek> getPeakDays() { return peakDays; }
        public Map<Integer, Long> getHourlyDistribution() { return hourlyDistribution; }
        public Map<DayOfWeek, Long> getDailyDistribution() { return dailyDistribution; }
        public double getPeakCapacityMultiplier() { return peakCapacityMultiplier; }
    }
}
