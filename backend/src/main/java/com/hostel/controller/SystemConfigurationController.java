package com.hostel.controller;

import com.hostel.service.SystemConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * System Configuration Controller for system management and configuration
 * Implements system configuration endpoints as per Product Design Document
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = {"http://localhost:3000", "https://hostel-ticketing-frontend.onrender.com"})
public class SystemConfigurationController {

    @Autowired
    private SystemConfigurationService systemConfigurationService;

    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            SystemConfigurationService.SystemHealthStatus health = 
                systemConfigurationService.getSystemHealthStatus();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting system health: " + e.getMessage());
        }
    }

    /**
     * Get all system parameters
     */
    @GetMapping("/parameters")
    public ResponseEntity<?> getSystemParameters() {
        try {
            Map<String, Object> parameters = systemConfigurationService.getAllSystemParameters();
            return ResponseEntity.ok(parameters);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting system parameters: " + e.getMessage());
        }
    }

    /**
     * Get specific system parameter
     */
    @GetMapping("/parameters/{key}")
    public ResponseEntity<?> getSystemParameter(@PathVariable String key) {
        try {
            Object value = systemConfigurationService.getSystemParameter(key, Object.class);
            if (value != null) {
                return ResponseEntity.ok(new ParameterResponse(key, value));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting system parameter: " + e.getMessage());
        }
    }

    /**
     * Update system parameter
     */
    @PutMapping("/parameters/{key}")
    public ResponseEntity<?> updateSystemParameter(@PathVariable String key, @RequestBody ParameterUpdateRequest request) {
        try {
            systemConfigurationService.setSystemParameter(key, request.getValue());
            return ResponseEntity.ok(new ParameterResponse(key, request.getValue()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating system parameter: " + e.getMessage());
        }
    }

    /**
     * Initialize system parameters
     */
    @PostMapping("/parameters/initialize")
    public ResponseEntity<?> initializeSystemParameters() {
        try {
            systemConfigurationService.initializeSystemParameters();
            return ResponseEntity.ok("System parameters initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error initializing system parameters: " + e.getMessage());
        }
    }

    /**
     * Optimize system performance
     */
    @PostMapping("/optimize")
    public ResponseEntity<?> optimizeSystemPerformance() {
        try {
            SystemConfigurationService.SystemOptimizationResult result = 
                systemConfigurationService.optimizeSystemPerformance();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error optimizing system: " + e.getMessage());
        }
    }

    /**
     * Generate system configuration report
     */
    @GetMapping("/configuration-report")
    public ResponseEntity<?> getConfigurationReport() {
        try {
            SystemConfigurationService.SystemConfigurationReport report = 
                systemConfigurationService.generateConfigurationReport();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating configuration report: " + e.getMessage());
        }
    }

    /**
     * Backup system configuration
     */
    @PostMapping("/backup")
    public ResponseEntity<?> backupSystemConfiguration() {
        try {
            SystemConfigurationService.SystemBackup backup = 
                systemConfigurationService.backupSystemConfiguration();
            return ResponseEntity.ok(backup);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating system backup: " + e.getMessage());
        }
    }

    /**
     * Restore system configuration from backup
     */
    @PostMapping("/restore")
    public ResponseEntity<?> restoreSystemConfiguration(@RequestBody SystemConfigurationService.SystemBackup backup) {
        try {
            boolean success = systemConfigurationService.restoreSystemConfiguration(backup);
            if (success) {
                return ResponseEntity.ok("System configuration restored successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to restore system configuration");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error restoring system configuration: " + e.getMessage());
        }
    }

    /**
     * Get system performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<?> getSystemPerformanceMetrics() {
        try {
            SystemConfigurationService.SystemHealthStatus health = 
                systemConfigurationService.getSystemHealthStatus();
            
            SystemPerformanceMetrics metrics = new SystemPerformanceMetrics(
                health.getMemoryUsagePercent(),
                health.getActiveTicketsCount(),
                health.getAvailableStaffCount(),
                health.getSlaBreachCount(),
                health.getOverallStatus()
            );
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting performance metrics: " + e.getMessage());
        }
    }

    /**
     * Update SLA configuration
     */
    @PutMapping("/sla-config")
    public ResponseEntity<?> updateSLAConfiguration(@RequestBody SLAConfigurationRequest request) {
        try {
            systemConfigurationService.setSystemParameter("sla.emergency.hours", request.getEmergencyHours());
            systemConfigurationService.setSystemParameter("sla.high.hours", request.getHighHours());
            systemConfigurationService.setSystemParameter("sla.medium.hours", request.getMediumHours());
            systemConfigurationService.setSystemParameter("sla.low.hours", request.getLowHours());
            
            return ResponseEntity.ok("SLA configuration updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating SLA configuration: " + e.getMessage());
        }
    }

    /**
     * Update capacity configuration
     */
    @PutMapping("/capacity-config")
    public ResponseEntity<?> updateCapacityConfiguration(@RequestBody CapacityConfigurationRequest request) {
        try {
            systemConfigurationService.setSystemParameter("capacity.junior.max", request.getJuniorMax());
            systemConfigurationService.setSystemParameter("capacity.senior.max", request.getSeniorMax());
            systemConfigurationService.setSystemParameter("capacity.supervisor.max", request.getSupervisorMax());
            
            return ResponseEntity.ok("Capacity configuration updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating capacity configuration: " + e.getMessage());
        }
    }

    // Request/Response classes

    public static class ParameterResponse {
        private final String key;
        private final Object value;

        public ParameterResponse(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() { return key; }
        public Object getValue() { return value; }
    }

    public static class ParameterUpdateRequest {
        private Object value;

        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class SystemPerformanceMetrics {
        private final double memoryUsagePercent;
        private final int activeTicketsCount;
        private final int availableStaffCount;
        private final int slaBreachCount;
        private final String overallStatus;

        public SystemPerformanceMetrics(double memoryUsagePercent, int activeTicketsCount,
                                      int availableStaffCount, int slaBreachCount, String overallStatus) {
            this.memoryUsagePercent = memoryUsagePercent;
            this.activeTicketsCount = activeTicketsCount;
            this.availableStaffCount = availableStaffCount;
            this.slaBreachCount = slaBreachCount;
            this.overallStatus = overallStatus;
        }

        public double getMemoryUsagePercent() { return memoryUsagePercent; }
        public int getActiveTicketsCount() { return activeTicketsCount; }
        public int getAvailableStaffCount() { return availableStaffCount; }
        public int getSlaBreachCount() { return slaBreachCount; }
        public String getOverallStatus() { return overallStatus; }
    }

    public static class SLAConfigurationRequest {
        private Integer emergencyHours;
        private Integer highHours;
        private Integer mediumHours;
        private Integer lowHours;

        public Integer getEmergencyHours() { return emergencyHours; }
        public void setEmergencyHours(Integer emergencyHours) { this.emergencyHours = emergencyHours; }
        public Integer getHighHours() { return highHours; }
        public void setHighHours(Integer highHours) { this.highHours = highHours; }
        public Integer getMediumHours() { return mediumHours; }
        public void setMediumHours(Integer mediumHours) { this.mediumHours = mediumHours; }
        public Integer getLowHours() { return lowHours; }
        public void setLowHours(Integer lowHours) { this.lowHours = lowHours; }
    }

    public static class CapacityConfigurationRequest {
        private Integer juniorMax;
        private Integer seniorMax;
        private Integer supervisorMax;

        public Integer getJuniorMax() { return juniorMax; }
        public void setJuniorMax(Integer juniorMax) { this.juniorMax = juniorMax; }
        public Integer getSeniorMax() { return seniorMax; }
        public void setSeniorMax(Integer seniorMax) { this.seniorMax = seniorMax; }
        public Integer getSupervisorMax() { return supervisorMax; }
        public void setSupervisorMax(Integer supervisorMax) { this.supervisorMax = supervisorMax; }
    }
}
