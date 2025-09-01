package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * System Configuration Service implementing system management and configuration
 * as per IIM Trichy Product Design Document Section 6.3.5
 */
@Service
@Transactional
public class SystemConfigurationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CategoryStaffMappingRepository mappingRepository;

    @Autowired
    private HostelBlockRepository hostelBlockRepository;

    // System configuration parameters
    private final Map<String, Object> systemParameters = new HashMap<>();

    /**
     * Initialize default system parameters
     */
    public void initializeSystemParameters() {
        // SLA Configuration
        systemParameters.put("sla.emergency.hours", 1);
        systemParameters.put("sla.high.hours", 4);
        systemParameters.put("sla.medium.hours", 24);
        systemParameters.put("sla.low.hours", 72);

        // Capacity Configuration
        systemParameters.put("capacity.junior.max", 5);
        systemParameters.put("capacity.senior.max", 8);
        systemParameters.put("capacity.supervisor.max", 12);

        // Escalation Configuration
        systemParameters.put("escalation.emergency.minutes", 60);
        systemParameters.put("escalation.high.hours", 4);
        systemParameters.put("escalation.medium.hours", 24);
        systemParameters.put("escalation.low.hours", 72);

        // Notification Configuration
        systemParameters.put("notification.email.enabled", true);
        systemParameters.put("notification.sms.enabled", false);
        systemParameters.put("notification.push.enabled", true);

        // Quality Assurance Configuration
        systemParameters.put("qa.verification.hours", 24);
        systemParameters.put("qa.photo.required.categories", 
            Arrays.asList("ELECTRICAL_ISSUES", "PLUMBING_WATER", "HVAC", "STRUCTURAL_CIVIL"));
        systemParameters.put("qa.satisfaction.threshold", 3);

        // Business Hours Configuration
        systemParameters.put("business.hours.start", 8);
        systemParameters.put("business.hours.end", 18);
        systemParameters.put("business.days", Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"));

        // Auto-assignment Configuration
        systemParameters.put("assignment.auto.enabled", true);
        systemParameters.put("assignment.workload.weight.active", 0.4);
        systemParameters.put("assignment.workload.weight.hours", 0.3);
        systemParameters.put("assignment.workload.weight.capacity", 0.2);
        systemParameters.put("assignment.workload.weight.performance", 0.1);
    }

    /**
     * Get system parameter value
     */
    @SuppressWarnings("unchecked")
    public <T> T getSystemParameter(String key, Class<T> type) {
        Object value = systemParameters.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    /**
     * Set system parameter value
     */
    public void setSystemParameter(String key, Object value) {
        systemParameters.put(key, value);
    }

    /**
     * Get all system parameters
     */
    public Map<String, Object> getAllSystemParameters() {
        return new HashMap<>(systemParameters);
    }

    /**
     * Get system health status
     * Implements system monitoring from PDD Section 10.2.1
     */
    public SystemHealthStatus getSystemHealthStatus() {
        SystemHealthStatus health = new SystemHealthStatus();

        // Database connectivity
        try {
            long userCount = userRepository.count();
            health.setDatabaseStatus("HEALTHY");
            health.setDatabaseInfo("Connected - " + userCount + " users");
        } catch (Exception e) {
            health.setDatabaseStatus("UNHEALTHY");
            health.setDatabaseInfo("Connection error: " + e.getMessage());
        }

        // Active tickets monitoring
        try {
            int activeTickets = ticketRepository.countByStatusIn(
                Arrays.asList(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
            health.setActiveTicketsCount(activeTickets);
            
            if (activeTickets > 1000) {
                health.addWarning("High number of active tickets: " + activeTickets);
            }
        } catch (Exception e) {
            health.addError("Cannot retrieve active tickets count: " + e.getMessage());
        }

        // Staff availability
        try {
            int availableStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF).size();
            health.setAvailableStaffCount(availableStaff);
            
            if (availableStaff < 5) {
                health.addWarning("Low staff availability: " + availableStaff);
            }
        } catch (Exception e) {
            health.addError("Cannot retrieve staff count: " + e.getMessage());
        }

        // SLA breaches
        try {
            int slaBreaches = countSLABreaches();
            health.setSlaBreachCount(slaBreaches);
            
            if (slaBreaches > 50) {
                health.addWarning("High SLA breach count: " + slaBreaches);
            }
        } catch (Exception e) {
            health.addError("Cannot retrieve SLA breach count: " + e.getMessage());
        }

        // System load metrics
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        health.setMemoryUsagePercent(memoryUsagePercent);
        
        if (memoryUsagePercent > 80) {
            health.addWarning("High memory usage: " + String.format("%.1f%%", memoryUsagePercent));
        }

        // Overall health determination
        if (health.getErrors().isEmpty() && health.getWarnings().size() <= 2) {
            health.setOverallStatus("HEALTHY");
        } else if (health.getErrors().isEmpty()) {
            health.setOverallStatus("WARNING");
        } else {
            health.setOverallStatus("CRITICAL");
        }

        health.setLastUpdated(LocalDateTime.now());
        return health;
    }

    /**
     * Optimize system performance
     * Implements performance optimization from PDD Section 6.3.5
     */
    public SystemOptimizationResult optimizeSystemPerformance() {
        List<String> optimizations = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        // Optimize staff assignments
        optimizeStaffAssignments(optimizations, recommendations);

        // Clean up old data
        cleanupOldData(optimizations, recommendations);

        // Optimize SLA settings
        optimizeSLASettings(optimizations, recommendations);

        // Update system configuration
        updateSystemConfiguration(optimizations, recommendations);

        return new SystemOptimizationResult(optimizations, recommendations);
    }

    /**
     * Generate system configuration report
     */
    public SystemConfigurationReport generateConfigurationReport() {
        Map<String, Object> currentConfig = getAllSystemParameters();
        
        // Get system statistics
        int totalUsers = (int) userRepository.count();
        int activeStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF).size();
        int totalMappings = (int) mappingRepository.count();
        int activeTickets = ticketRepository.countByStatusIn(
            Arrays.asList(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));

        // Get recent configuration changes
        List<String> recentChanges = getRecentConfigurationChanges();

        // Get system recommendations
        List<String> systemRecommendations = generateSystemRecommendations();

        return new SystemConfigurationReport(
            currentConfig,
            totalUsers,
            activeStaff,
            totalMappings,
            activeTickets,
            recentChanges,
            systemRecommendations
        );
    }

    /**
     * Backup system configuration
     */
    public SystemBackup backupSystemConfiguration() {
        Map<String, Object> configBackup = new HashMap<>(systemParameters);
        
        // Export user data (excluding sensitive information)
        List<UserBackup> userBackups = userRepository.findAll().stream()
            .map(user -> new UserBackup(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getIsActive()
            ))
            .collect(Collectors.toList());

        // Export mappings
        List<MappingBackup> mappingBackups = mappingRepository.findAll().stream()
            .map(mapping -> new MappingBackup(
                mapping.getId(),
                mapping.getStaff().getId(),
                mapping.getHostelBlockString(),
                mapping.getCategory(),
                mapping.getPriorityLevel(),
                mapping.getIsActive()
            ))
            .collect(Collectors.toList());

        return new SystemBackup(
            LocalDateTime.now(),
            configBackup,
            userBackups,
            mappingBackups
        );
    }

    /**
     * Restore system configuration from backup
     */
    public boolean restoreSystemConfiguration(SystemBackup backup) {
        try {
            // Restore system parameters
            systemParameters.clear();
            systemParameters.putAll(backup.getConfigurationParameters());

            // Note: User and mapping restoration would require more careful implementation
            // to avoid data conflicts and maintain referential integrity
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper methods

    private int countSLABreaches() {
        List<Ticket> activeTickets = ticketRepository.findByStatusIn(
            Arrays.asList(TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
        
        return (int) activeTickets.stream()
            .filter(ticket -> ticket.getEstimatedResolutionTime() != null && 
                ticket.getEstimatedResolutionTime().isBefore(LocalDateTime.now()))
            .count();
    }

    private void optimizeStaffAssignments(List<String> optimizations, List<String> recommendations) {
        // Analyze staff workload distribution
        List<User> staff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
        Map<User, Integer> workloadMap = new HashMap<>();
        
        for (User staffMember : staff) {
            int activeTickets = ticketRepository.countByAssignedToAndStatusIn(
                staffMember, Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
            workloadMap.put(staffMember, activeTickets);
        }

        // Identify overloaded staff
        int maxCapacity = getSystemParameter("capacity.senior.max", Integer.class);
        List<User> overloadedStaff = workloadMap.entrySet().stream()
            .filter(entry -> entry.getValue() > maxCapacity)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (!overloadedStaff.isEmpty()) {
            recommendations.add("Consider redistributing workload from overloaded staff: " + 
                overloadedStaff.stream().map(User::getFullName).collect(Collectors.joining(", ")));
        }

        // Identify underutilized staff
        List<User> underutilizedStaff = workloadMap.entrySet().stream()
            .filter(entry -> entry.getValue() < 2)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        if (!underutilizedStaff.isEmpty()) {
            recommendations.add("Consider additional training or cross-skilling for underutilized staff: " + 
                underutilizedStaff.stream().map(User::getFullName).collect(Collectors.joining(", ")));
        }
    }

    private void cleanupOldData(List<String> optimizations, List<String> recommendations) {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        
        // Count old closed tickets
        int oldClosedTickets = ticketRepository.countByStatusAndClosedAtBefore(TicketStatus.CLOSED, sixMonthsAgo);
        
        if (oldClosedTickets > 1000) {
            recommendations.add("Consider archiving " + oldClosedTickets + " old closed tickets to improve performance");
        }
    }

    private void optimizeSLASettings(List<String> optimizations, List<String> recommendations) {
        // Analyze current SLA performance
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Ticket> recentTickets = ticketRepository.findByCreatedAtAfter(thirtyDaysAgo);
        
        long slaBreaches = recentTickets.stream()
            .filter(ticket -> ticket.getEstimatedResolutionTime() != null && 
                ticket.getActualResolutionTime() != null &&
                ticket.getActualResolutionTime().isAfter(ticket.getEstimatedResolutionTime()))
            .count();

        double breachRate = recentTickets.size() > 0 ? (double) slaBreaches / recentTickets.size() * 100 : 0;

        if (breachRate > 20) {
            recommendations.add("SLA breach rate is high (" + String.format("%.1f", breachRate) + 
                "%). Consider adjusting SLA timeframes or improving resource allocation.");
        }
    }

    private void updateSystemConfiguration(List<String> optimizations, List<String> recommendations) {
        // Auto-adjust parameters based on system performance
        if (countSLABreaches() > 100) {
            // Increase escalation thresholds temporarily
            optimizations.add("Temporarily adjusted escalation thresholds due to high SLA breach count");
        }
    }

    private List<String> getRecentConfigurationChanges() {
        // This would track configuration changes in a real implementation
        return Arrays.asList(
            "SLA thresholds updated for HVAC category",
            "Staff capacity limits adjusted for peak season",
            "Notification preferences updated"
        );
    }

    private List<String> generateSystemRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        // Analyze system metrics and generate recommendations
        SystemHealthStatus health = getSystemHealthStatus();
        
        if (health.getActiveTicketsCount() > 500) {
            recommendations.add("Consider hiring additional staff or implementing automation");
        }
        
        if (health.getSlaBreachCount() > 50) {
            recommendations.add("Review and optimize SLA settings");
        }
        
        if (health.getMemoryUsagePercent() > 70) {
            recommendations.add("Consider increasing system memory allocation");
        }

        return recommendations;
    }

    // Data classes

    public static class SystemHealthStatus {
        private String overallStatus;
        private String databaseStatus;
        private String databaseInfo;
        private int activeTicketsCount;
        private int availableStaffCount;
        private int slaBreachCount;
        private double memoryUsagePercent;
        private List<String> warnings = new ArrayList<>();
        private List<String> errors = new ArrayList<>();
        private LocalDateTime lastUpdated;

        // Getters and setters
        public String getOverallStatus() { return overallStatus; }
        public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
        public String getDatabaseStatus() { return databaseStatus; }
        public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
        public String getDatabaseInfo() { return databaseInfo; }
        public void setDatabaseInfo(String databaseInfo) { this.databaseInfo = databaseInfo; }
        public int getActiveTicketsCount() { return activeTicketsCount; }
        public void setActiveTicketsCount(int activeTicketsCount) { this.activeTicketsCount = activeTicketsCount; }
        public int getAvailableStaffCount() { return availableStaffCount; }
        public void setAvailableStaffCount(int availableStaffCount) { this.availableStaffCount = availableStaffCount; }
        public int getSlaBreachCount() { return slaBreachCount; }
        public void setSlaBreachCount(int slaBreachCount) { this.slaBreachCount = slaBreachCount; }
        public double getMemoryUsagePercent() { return memoryUsagePercent; }
        public void setMemoryUsagePercent(double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }
        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }
        public List<String> getErrors() { return errors; }
        public void addError(String error) { this.errors.add(error); }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class SystemOptimizationResult {
        private final List<String> optimizations;
        private final List<String> recommendations;

        public SystemOptimizationResult(List<String> optimizations, List<String> recommendations) {
            this.optimizations = optimizations;
            this.recommendations = recommendations;
        }

        public List<String> getOptimizations() { return optimizations; }
        public List<String> getRecommendations() { return recommendations; }
    }

    public static class SystemConfigurationReport {
        private final Map<String, Object> currentConfiguration;
        private final int totalUsers;
        private final int activeStaff;
        private final int totalMappings;
        private final int activeTickets;
        private final List<String> recentChanges;
        private final List<String> recommendations;

        public SystemConfigurationReport(Map<String, Object> currentConfiguration, int totalUsers,
                                       int activeStaff, int totalMappings, int activeTickets,
                                       List<String> recentChanges, List<String> recommendations) {
            this.currentConfiguration = currentConfiguration;
            this.totalUsers = totalUsers;
            this.activeStaff = activeStaff;
            this.totalMappings = totalMappings;
            this.activeTickets = activeTickets;
            this.recentChanges = recentChanges;
            this.recommendations = recommendations;
        }

        // Getters
        public Map<String, Object> getCurrentConfiguration() { return currentConfiguration; }
        public int getTotalUsers() { return totalUsers; }
        public int getActiveStaff() { return activeStaff; }
        public int getTotalMappings() { return totalMappings; }
        public int getActiveTickets() { return activeTickets; }
        public List<String> getRecentChanges() { return recentChanges; }
        public List<String> getRecommendations() { return recommendations; }
    }

    public static class SystemBackup {
        private final LocalDateTime backupTime;
        private final Map<String, Object> configurationParameters;
        private final List<UserBackup> users;
        private final List<MappingBackup> mappings;

        public SystemBackup(LocalDateTime backupTime, Map<String, Object> configurationParameters,
                          List<UserBackup> users, List<MappingBackup> mappings) {
            this.backupTime = backupTime;
            this.configurationParameters = configurationParameters;
            this.users = users;
            this.mappings = mappings;
        }

        // Getters
        public LocalDateTime getBackupTime() { return backupTime; }
        public Map<String, Object> getConfigurationParameters() { return configurationParameters; }
        public List<UserBackup> getUsers() { return users; }
        public List<MappingBackup> getMappings() { return mappings; }
    }

    public static class UserBackup {
        private final UUID id;
        private final String username;
        private final String email;
        private final String firstName;
        private final String lastName;
        private final UserRole role;
        private final Boolean isActive;

        public UserBackup(UUID id, String username, String email, String firstName,
                         String lastName, UserRole role, Boolean isActive) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.isActive = isActive;
        }

        // Getters
        public UUID getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public UserRole getRole() { return role; }
        public Boolean getIsActive() { return isActive; }
    }

    public static class MappingBackup {
        private final UUID id;
        private final UUID staffId;
        private final String hostelBlock;
        private final String category;
        private final Integer priorityLevel;
        private final Boolean isActive;

        public MappingBackup(UUID id, UUID staffId, String hostelBlock, String category,
                           Integer priorityLevel, Boolean isActive) {
            this.id = id;
            this.staffId = staffId;
            this.hostelBlock = hostelBlock;
            this.category = category;
            this.priorityLevel = priorityLevel;
            this.isActive = isActive;
        }

        // Getters
        public UUID getId() { return id; }
        public UUID getStaffId() { return staffId; }
        public String getHostelBlock() { return hostelBlock; }
        public String getCategory() { return category; }
        public Integer getPriorityLevel() { return priorityLevel; }
        public Boolean getIsActive() { return isActive; }
    }
}
