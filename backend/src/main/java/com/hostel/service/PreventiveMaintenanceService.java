package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Preventive Maintenance Service implementing proactive maintenance scheduling
 * as per IIM Trichy Product Design Document Section 12.2
 * 
 * Note: This is a framework implementation demonstrating the concepts.
 * Full functionality would require additional MaintenanceSchedule entity enhancements.
 */
@Service
@Transactional
public class PreventiveMaintenanceService {

    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    // Note: TicketService available for creating maintenance tickets
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Generate preventive maintenance recommendations
     * This demonstrates the concept - in full implementation would be more sophisticated
     */
    @Scheduled(cron = "0 0 6 * * MON") // Every Monday at 6 AM
    public void processMaintenanceRecommendations() {
        try {
            List<MaintenanceRecommendation> recommendations = generateMaintenanceRecommendations();
            
            if (!recommendations.isEmpty()) {
                sendMaintenanceRecommendations(recommendations);
            }
            
        } catch (Exception e) {
            System.err.println("Error generating maintenance recommendations: " + e.getMessage());
        }
    }

    /**
     * Generate maintenance recommendations based on asset age and usage
     */
    public List<MaintenanceRecommendation> generateMaintenanceRecommendations() {
        List<MaintenanceRecommendation> recommendations = new ArrayList<>();
        
        try {
            Optional<List<Asset>> assetsOpt = assetRepository.findAll().stream().collect(Collectors.collectingAndThen(
                Collectors.toList(), 
                Optional::of
            ));
            
            List<Asset> assets = assetsOpt.orElse(new ArrayList<>());
            
            for (Asset asset : assets) {
                // Check asset age and generate recommendations
                if (asset.getPurchaseDate() != null) {
                    long monthsSincePurchase = java.time.temporal.ChronoUnit.MONTHS.between(
                        asset.getPurchaseDate(), LocalDate.now());
                    
                    // Recommend maintenance based on asset type and age
                    List<String> maintenanceTypes = getRecommendedMaintenanceTypes(asset, monthsSincePurchase);
                    
                    for (String maintenanceType : maintenanceTypes) {
                        recommendations.add(new MaintenanceRecommendation(
                            asset,
                            maintenanceType,
                            calculateMaintenancePriority(asset, monthsSincePurchase),
                            "Based on asset age: " + monthsSincePurchase + " months"
                        ));
                    }
                }
                
                // Check for frequent repair history
                List<Ticket> assetTickets = findAssetRelatedTickets(asset);
                
                if (assetTickets.size() > 3) { // More than 3 tickets suggest maintenance need
                    recommendations.add(new MaintenanceRecommendation(
                        asset,
                        "Comprehensive Inspection",
                        MaintenancePriority.HIGH,
                        "Asset has " + assetTickets.size() + " repair tickets. Preventive maintenance recommended."
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Error generating maintenance recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }

    /**
     * Find tickets related to a specific asset
     */
    private List<Ticket> findAssetRelatedTickets(Asset asset) {
        // Search by location and asset tag
        final String searchTerm = asset.getAssetTag() != null && !asset.getAssetTag().trim().isEmpty() ? 
                                 asset.getAssetTag() : asset.getName();
        
        return ticketRepository.findAll().stream()
            .filter(ticket -> ticket.getLocationDetails() != null && 
                             ticket.getLocationDetails().contains(searchTerm))
            .filter(ticket -> ticket.getStatus() == TicketStatus.CLOSED)
            .collect(Collectors.toList());
    }

    /**
     * Get recommended maintenance types based on asset type and age
     */
    private List<String> getRecommendedMaintenanceTypes(Asset asset, long monthsSincePurchase) {
        List<String> maintenanceTypes = new ArrayList<>();
        
        // Basic maintenance recommendations based on asset type
        String assetTypeName = asset.getType().name();
        
        if (assetTypeName.contains("ELECTRICAL")) {
            if (monthsSincePurchase >= 12) maintenanceTypes.add("Electrical Safety Inspection");
            if (monthsSincePurchase >= 6) maintenanceTypes.add("Electrical Component Cleaning");
        } else if (assetTypeName.contains("PLUMBING")) {
            if (monthsSincePurchase >= 6) maintenanceTypes.add("Plumbing System Check");
            if (monthsSincePurchase >= 12) maintenanceTypes.add("Pipe Inspection");
        } else if (assetTypeName.contains("HVAC")) {
            if (monthsSincePurchase >= 3) maintenanceTypes.add("HVAC Filter Replacement");
            if (monthsSincePurchase >= 6) maintenanceTypes.add("HVAC System Cleaning");
        } else if (assetTypeName.contains("FURNITURE")) {
            if (monthsSincePurchase >= 12) maintenanceTypes.add("Furniture Inspection and Repair");
        } else if (assetTypeName.contains("ELECTRONIC") || assetTypeName.contains("IT")) {
            if (monthsSincePurchase >= 6) maintenanceTypes.add("Electronics Cleaning");
            if (monthsSincePurchase >= 12) maintenanceTypes.add("Hardware Performance Check");
        } else {
            if (monthsSincePurchase >= 12) maintenanceTypes.add("General Maintenance Inspection");
        }
        
        return maintenanceTypes;
    }

    /**
     * Calculate maintenance priority based on asset conditions
     */
    private MaintenancePriority calculateMaintenancePriority(Asset asset, long monthsSincePurchase) {
        String statusName = asset.getStatus().name();
        
        if (statusName.contains("OUT_OF_SERVICE") || statusName.contains("MAINTENANCE")) {
            return MaintenancePriority.HIGH;
        } else if (monthsSincePurchase >= 24) {
            return MaintenancePriority.MEDIUM;
        } else if (monthsSincePurchase >= 12) {
            return MaintenancePriority.LOW;
        } else {
            return MaintenancePriority.ROUTINE;
        }
    }

    /**
     * Send maintenance recommendations to appropriate staff
     */
    private void sendMaintenanceRecommendations(List<MaintenanceRecommendation> recommendations) {
        try {
            List<User> maintenanceStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF)
                .stream()
                .filter(staff -> staff.getStaffVertical() != null && 
                               (staff.getStaffVertical() == StaffVertical.GENERAL_MAINTENANCE ||
                                staff.getStaffVertical().name().contains("MAINTENANCE")))
                .collect(Collectors.toList());
            
            if (maintenanceStaff.isEmpty()) {
                maintenanceStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.ADMIN);
            }
            
            StringBuilder message = new StringBuilder("Weekly Preventive Maintenance Recommendations:\n\n");
            
            Map<MaintenancePriority, List<MaintenanceRecommendation>> priorityGroups = 
                recommendations.stream()
                    .collect(Collectors.groupingBy(MaintenanceRecommendation::getPriority));
            
            for (MaintenancePriority priority : MaintenancePriority.values()) {
                List<MaintenanceRecommendation> priorityRecs = priorityGroups.get(priority);
                if (priorityRecs != null && !priorityRecs.isEmpty()) {
                    message.append(priority.name()).append(" Priority:\n");
                    for (MaintenanceRecommendation rec : priorityRecs) {
                        message.append("- ").append(rec.getAsset().getName())
                               .append(": ").append(rec.getMaintenanceType())
                               .append(" (").append(rec.getReason()).append(")\n");
                    }
                    message.append("\n");
                }
            }
            
            for (User staff : maintenanceStaff) {
                notificationService.sendNotification(
                    staff,
                    "Preventive Maintenance Recommendations",
                    message.toString(),
                    NotificationType.SYSTEM_ALERT,
                    null
                );
            }
        } catch (Exception e) {
            System.err.println("Error sending maintenance recommendations: " + e.getMessage());
        }
    }

    /**
     * Get maintenance analytics
     */
    public MaintenanceAnalytics getMaintenanceAnalytics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        // Get maintenance-related tickets in date range
        List<Ticket> maintenanceTickets = ticketRepository.findByCreatedAtBetween(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59))
            .stream()
            .filter(ticket -> ticket.getTitle() != null && 
                             (ticket.getTitle().toLowerCase().contains("maintenance") ||
                              ticket.getTitle().toLowerCase().contains("scheduled") ||
                              ticket.getTitle().toLowerCase().contains("preventive")))
            .collect(Collectors.toList());
        
        // Calculate metrics
        long totalMaintenanceTickets = maintenanceTickets.size();
        long completedMaintenance = maintenanceTickets.stream()
            .mapToLong(ticket -> ticket.getStatus() == TicketStatus.CLOSED ? 1 : 0)
            .sum();
        
        double completionRate = totalMaintenanceTickets > 0 ? 
            (double) completedMaintenance / totalMaintenanceTickets * 100 : 0;
        
        // Calculate estimated cost savings
        double estimatedCostSavings = calculatePreventiveMaintenanceSavings(maintenanceTickets);
        
        // Asset type distribution
        Map<String, Long> assetTypeDistribution = new HashMap<>();
        try {
            assetTypeDistribution = assetRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                    asset -> asset.getType().name(),
                    Collectors.counting()
                ));
        } catch (Exception e) {
            // Handle error gracefully
        }
        
        // Maintenance type distribution (simulated)
        Map<String, Long> maintenanceTypeDistribution = Map.of(
            "Electrical Maintenance", 5L,
            "Plumbing Maintenance", 3L,
            "HVAC Maintenance", 4L,
            "General Maintenance", 8L
        );
        
        return new MaintenanceAnalytics(
            totalMaintenanceTickets,
            0L, // Would come from actual schedule tracking
            completedMaintenance,
            completionRate,
            estimatedCostSavings,
            assetTypeDistribution,
            maintenanceTypeDistribution
        );
    }

    /**
     * Calculate estimated cost savings from preventive maintenance
     */
    private double calculatePreventiveMaintenanceSavings(List<Ticket> maintenanceTickets) {
        // Simplified calculation: assume preventive maintenance costs 30% of reactive maintenance
        // and each prevented breakdown saves on average ₹5000 in emergency repairs
        
        long preventiveMaintenance = maintenanceTickets.stream()
            .mapToLong(ticket -> ticket.getTitle() != null && 
                                ticket.getTitle().toLowerCase().contains("scheduled") ? 1 : 0)
            .sum();
        
        return preventiveMaintenance * 5000 * 0.7; // 70% cost savings per preventive maintenance
    }

    // Supporting classes
    public enum MaintenancePriority {
        ROUTINE, LOW, MEDIUM, HIGH
    }

    public static class MaintenanceAnalytics {
        private long totalSchedules;
        private long overdueSchedules;
        private long completedMaintenance;
        private double completionRate;
        private double estimatedCostSavings;
        private Map<String, Long> assetTypeDistribution;
        private Map<String, Long> maintenanceTypeDistribution;

        public MaintenanceAnalytics(long totalSchedules, long overdueSchedules, long completedMaintenance,
                                   double completionRate, double estimatedCostSavings,
                                   Map<String, Long> assetTypeDistribution, Map<String, Long> maintenanceTypeDistribution) {
            this.totalSchedules = totalSchedules;
            this.overdueSchedules = overdueSchedules;
            this.completedMaintenance = completedMaintenance;
            this.completionRate = completionRate;
            this.estimatedCostSavings = estimatedCostSavings;
            this.assetTypeDistribution = assetTypeDistribution;
            this.maintenanceTypeDistribution = maintenanceTypeDistribution;
        }

        // Getters
        public long getTotalSchedules() { return totalSchedules; }
        public long getOverdueSchedules() { return overdueSchedules; }
        public long getCompletedMaintenance() { return completedMaintenance; }
        public double getCompletionRate() { return completionRate; }
        public double getEstimatedCostSavings() { return estimatedCostSavings; }
        public Map<String, Long> getAssetTypeDistribution() { return assetTypeDistribution; }
        public Map<String, Long> getMaintenanceTypeDistribution() { return maintenanceTypeDistribution; }
    }

    public static class MaintenanceRecommendation {
        private Asset asset;
        private String maintenanceType;
        private MaintenancePriority priority;
        private String reason;

        public MaintenanceRecommendation(Asset asset, String maintenanceType, MaintenancePriority priority, String reason) {
            this.asset = asset;
            this.maintenanceType = maintenanceType;
            this.priority = priority;
            this.reason = reason;
        }

        // Getters
        public Asset getAsset() { return asset; }
        public String getMaintenanceType() { return maintenanceType; }
        public MaintenancePriority getPriority() { return priority; }
        public String getReason() { return reason; }
    }
}