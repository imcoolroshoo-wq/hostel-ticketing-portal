package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.UserRepository;
import com.hostel.repository.CategoryStaffMappingRepository;
import com.hostel.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Intelligent Ticket Assignment Service implementing the multi-dimensional mapping algorithm
 * as per IIM Trichy Hostel Ticket Management System Product Design Document
 */
@Service
@Transactional
public class TicketAssignmentService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryStaffMappingRepository categoryStaffMappingRepository;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    // Configuration constants based on product design
    private static final int MAX_JUNIOR_STAFF_TICKETS = 5;
    private static final int MAX_SENIOR_STAFF_TICKETS = 8;
    private static final int MAX_SUPERVISOR_TICKETS = 12;
    
    /**
     * Main assignment method implementing the intelligent assignment algorithm
     * as specified in the product design document
     */
    public User autoAssignTicket(Ticket ticket) {
        String category = ticket.getEffectiveCategory();
        HostelName hostelBlock = ticket.getHostelBlockEnum();
        TicketPriority priority = ticket.getPriority();
        
        // Handle custom categories - require manual admin assignment
        if (ticket.getCustomCategory() != null && !ticket.getCustomCategory().trim().isEmpty()) {
            // Custom categories are not auto-assigned
            return null;
        }
        
        // Step 1: Find eligible staff mappings using multi-dimensional approach
        List<CategoryStaffMapping> eligibleMappings = findEligibleStaffMappings(hostelBlock, category);
        
        if (eligibleMappings.isEmpty()) {
            // Step 2: Fallback scenarios
            return handleFallbackAssignment(category, hostelBlock, priority);
        }
        
        // Step 3: Calculate workload scores and select optimal staff
        User selectedStaff = selectOptimalStaff(eligibleMappings, ticket);
        
        if (selectedStaff == null) {
            // Step 4: Handle capacity overflow
            return handleCapacityOverflow(eligibleMappings, ticket);
        }
        
        return selectedStaff;
    }
    
    /**
     * Find eligible staff mappings based on hostel block and category
     * Implements the mapping priority logic from product design
     */
    private List<CategoryStaffMapping> findEligibleStaffMappings(HostelName hostelBlock, String category) {
        List<CategoryStaffMapping> mappings = new ArrayList<>();
        
        // Priority 1: Exact match (Hostel + Category)
        if (hostelBlock != null) {
            mappings.addAll(categoryStaffMappingRepository
                .findByHostelBlockAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(hostelBlock, category));
        }
        
        // Priority 2: Category match across all hostels (hostel_block = NULL)
        mappings.addAll(categoryStaffMappingRepository
            .findByHostelBlockIsNullAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(category));
        
        // Priority 3: General maintenance staff
        if (mappings.isEmpty()) {
            mappings.addAll(categoryStaffMappingRepository
                .findByHostelBlockIsNullAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc("GENERAL"));
        }
        
        // Filter by staff availability and active status
        return mappings.stream()
            .filter(mapping -> mapping.getStaff().getIsActive())
            .filter(mapping -> isStaffAvailable(mapping.getStaff()))
            .collect(Collectors.toList());
    }
    
    /**
     * Select optimal staff using workload-based algorithm
     * Implements the workload calculation from product design
     */
    private User selectOptimalStaff(List<CategoryStaffMapping> mappings, Ticket ticket) {
        Map<User, Double> workloadScores = new HashMap<>();
        
        for (CategoryStaffMapping mapping : mappings) {
            User staff = mapping.getStaff();
            
            // Skip if staff is at capacity (unless emergency)
            if (!ticket.getPriority().equals(TicketPriority.EMERGENCY) && 
                isStaffAtCapacity(staff)) {
                continue;
            }
            
            double workloadScore = calculateWorkloadScore(staff, mapping, ticket);
            workloadScores.put(staff, workloadScore);
        }
        
        if (workloadScores.isEmpty()) {
            return null;
        }
        
        // Select staff with LOWEST workload score
        return workloadScores.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    /**
     * Calculate workload score as per product design algorithm
     * Lower score = better candidate for assignment
     */
    private double calculateWorkloadScore(User staff, CategoryStaffMapping mapping, Ticket ticket) {
        // Get current workload metrics
        int activeTickets = countActiveTicketsForStaff(staff);
        double estimatedHours = calculateEstimatedRemainingHours(staff);
        double capacityUtilization = calculateCapacityUtilization(staff);
        double performanceFactor = getPerformanceFactor(staff);
        
        // Apply capacity weight from mapping
        double capacityWeight = mapping.getCapacityWeight().doubleValue();
        
        // Workload Score calculation as per product design:
        // Score = (Active_Tickets × 0.4) + (Estimated_Hours × 0.3) + 
        //         (Capacity_Utilization × 0.2) + (Performance_Factor × 0.1)
        double score = (activeTickets * 0.4) + 
                      (estimatedHours * 0.3) + 
                      (capacityUtilization * 0.2) + 
                      (performanceFactor * 0.1);
        
        // Apply capacity weight multiplier
        score = score / capacityWeight;
        
        // Priority adjustment based on mapping priority level
        score = score * (1.0 + (mapping.getPriorityLevel() - 1) * 0.1);
        
        return score;
    }
    
    /**
     * Handle fallback assignment scenarios
     */
    private User handleFallbackAssignment(String category, HostelName hostelBlock, TicketPriority priority) {
        // Fallback 1: Check category-only mappings
        List<CategoryStaffMapping> categoryMappings = categoryStaffMappingRepository
            .findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc(category);
        
        if (!categoryMappings.isEmpty()) {
            User staff = selectOptimalStaff(categoryMappings, 
                createDummyTicketForFallback(category, priority));
            if (staff != null) return staff;
        }
        
        // Fallback 2: General maintenance staff
        List<CategoryStaffMapping> generalMappings = categoryStaffMappingRepository
            .findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc("GENERAL");
        
        if (!generalMappings.isEmpty()) {
            User staff = selectOptimalStaff(generalMappings, 
                createDummyTicketForFallback("GENERAL", priority));
            if (staff != null) return staff;
        }
        
        // Fallback 3: Any available staff (emergency override)
        if (priority.equals(TicketPriority.EMERGENCY)) {
            List<User> allStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF);
            return findLeastBusyStaff(allStaff);
        }
        
        return null; // No assignment possible
    }
    
    /**
     * Handle capacity overflow situations
     */
    private User handleCapacityOverflow(List<CategoryStaffMapping> mappings, Ticket ticket) {
        // For emergency tickets, override capacity limits
        if (ticket.getPriority().equals(TicketPriority.EMERGENCY)) {
            return mappings.stream()
                .map(CategoryStaffMapping::getStaff)
                .min(Comparator.comparingInt(this::countActiveTicketsForStaff))
                .orElse(null);
        }
        
        // Create priority-based assignment queue (not implemented in this version)
        // For now, return null to indicate manual assignment needed
        return null;
    }
    
    /**
     * Check if staff member is available for assignment
     */
    private boolean isStaffAvailable(User staff) {
        // Basic availability check - can be extended with shift schedules
        return staff.getIsActive() && staff.getRole().equals(UserRole.STAFF);
    }
    
    /**
     * Check if staff member is at capacity
     */
    private boolean isStaffAtCapacity(User staff) {
        int activeTickets = countActiveTicketsForStaff(staff);
        int maxCapacity = getMaxCapacityForStaff(staff);
        return activeTickets >= maxCapacity;
    }
    
    /**
     * Get maximum capacity for staff based on their level
     */
    private int getMaxCapacityForStaff(User staff) {
        // This could be enhanced to check staff level from database
        // For now, use staff vertical as proxy
        if (staff.getStaffVertical() != null) {
            switch (staff.getStaffVertical()) {
                case HOSTEL_WARDEN:
                case BLOCK_SUPERVISOR:
                    return MAX_SUPERVISOR_TICKETS;
                case ELECTRICAL:
                case PLUMBING:
                case HVAC:
                case IT_SUPPORT:
                    return MAX_SENIOR_STAFF_TICKETS;
                default:
                    return MAX_JUNIOR_STAFF_TICKETS;
            }
        }
        return MAX_JUNIOR_STAFF_TICKETS;
    }
    
    /**
     * Count active tickets for a staff member
     */
    private int countActiveTicketsForStaff(User staff) {
        return ticketRepository.countByAssignedToAndStatusIn(staff, 
            Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
    }
    
    /**
     * Calculate estimated remaining work hours for staff
     */
    private double calculateEstimatedRemainingHours(User staff) {
        List<Ticket> activeTickets = ticketRepository.findByAssignedToAndStatusIn(staff,
            Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
        
        return activeTickets.stream()
            .mapToDouble(this::getEstimatedRemainingHours)
            .sum();
    }
    
    /**
     * Get estimated remaining hours for a ticket
     */
    private double getEstimatedRemainingHours(Ticket ticket) {
        if (ticket.getEstimatedResolutionTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (ticket.getEstimatedResolutionTime().isAfter(now)) {
                return java.time.Duration.between(now, ticket.getEstimatedResolutionTime()).toHours();
            }
        }
        
        // Default estimation based on category and priority
        if (ticket.getCategory() != null) {
            return ticket.getCategory().getEstimatedResolutionHours(ticket.getPriority());
        }
        
        return 4.0; // Default 4 hours
    }
    
    /**
     * Calculate capacity utilization for staff
     */
    private double calculateCapacityUtilization(User staff) {
        int activeTickets = countActiveTicketsForStaff(staff);
        int maxCapacity = getMaxCapacityForStaff(staff);
        return (double) activeTickets / maxCapacity;
    }
    
    /**
     * Get performance factor for staff (simplified implementation)
     */
    private double getPerformanceFactor(User staff) {
        // This would typically be calculated from historical performance data
        // For now, return a neutral factor
        return 0.5; // Neutral performance factor
    }
    
    /**
     * Find least busy staff from a list
     */
    private User findLeastBusyStaff(List<User> staffList) {
        return staffList.stream()
            .filter(this::isStaffAvailable)
            .min(Comparator.comparingInt(this::countActiveTicketsForStaff))
            .orElse(null);
    }
    
    /**
     * Create dummy ticket for fallback scenarios
     */
    private Ticket createDummyTicketForFallback(String category, TicketPriority priority) {
        Ticket dummy = new Ticket();
        dummy.setPriority(priority);
        // Set category based on string
        try {
            dummy.setCategory(TicketCategory.valueOf(category));
        } catch (IllegalArgumentException e) {
            dummy.setCustomCategory(category);
        }
        return dummy;
    }
    
    /**
     * Admin override assignment - bypass all restrictions
     */
    public boolean adminAssignTicket(Ticket ticket, User staff) {
        if (staff.getRole().equals(UserRole.STAFF) && staff.getIsActive()) {
            ticket.setAssignedTo(staff);
            ticket.setStatus(TicketStatus.ASSIGNED);
            ticket.setAssignedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }
    
    /**
     * Get workload statistics for a staff member
     */
    public WorkloadStats getStaffWorkloadStats(User staff) {
        if (!staff.getRole().equals(UserRole.STAFF)) {
            return new WorkloadStats(0, 0, 0, 0, 0.0);
        }
        
        List<Ticket> allTickets = ticketRepository.findByAssignedTo(staff);
        
        int totalTickets = allTickets.size();
        int activeTickets = (int) allTickets.stream()
            .filter(ticket -> Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD)
                .contains(ticket.getStatus()))
            .count();
        int completedTickets = (int) allTickets.stream()
            .filter(ticket -> ticket.getStatus().equals(TicketStatus.CLOSED))
            .count();
        int overdueTickets = (int) allTickets.stream()
            .filter(this::isTicketOverdue)
            .count();
        
        double avgSatisfactionRating = allTickets.stream()
            .filter(ticket -> ticket.getSatisfactionRating() != null)
            .mapToInt(Ticket::getSatisfactionRating)
            .average()
            .orElse(0.0);
        
        return new WorkloadStats(totalTickets, activeTickets, completedTickets, 
                                overdueTickets, avgSatisfactionRating);
    }
    
    /**
     * Check if ticket is overdue
     */
    private boolean isTicketOverdue(Ticket ticket) {
        return ticket.getEstimatedResolutionTime() != null &&
               ticket.getEstimatedResolutionTime().isBefore(LocalDateTime.now()) &&
               Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD)
                   .contains(ticket.getStatus());
    }
    
    /**
     * Create or update staff mapping
     */
    public CategoryStaffMapping createStaffMapping(User staff, String hostelBlock, String category, 
                                                  Integer priorityLevel, BigDecimal capacityWeight, 
                                                  Integer expertiseLevel) {
        CategoryStaffMapping mapping = new CategoryStaffMapping();
        mapping.setStaff(staff);
        mapping.setHostelBlock(hostelBlock);
        mapping.setCategory(category);
        mapping.setPriorityLevel(priorityLevel);
        mapping.setCapacityWeight(capacityWeight);
        mapping.setExpertiseLevel(expertiseLevel);
        mapping.setIsActive(true);
        
        return categoryStaffMappingRepository.save(mapping);
    }
    
    /**
     * Get all mappings for a staff member
     */
    public List<CategoryStaffMapping> getStaffMappings(User staff) {
        return categoryStaffMappingRepository.findByStaffAndIsActiveTrue(staff);
    }
    
    /**
     * Workload statistics class
     */
    public static class WorkloadStats {
        private final int totalTickets;
        private final int activeTickets;
        private final int completedTickets;
        private final int overdueTickets;
        private final double avgSatisfactionRating;
        
        public WorkloadStats(int totalTickets, int activeTickets, int completedTickets, 
                           int overdueTickets, double avgSatisfactionRating) {
            this.totalTickets = totalTickets;
            this.activeTickets = activeTickets;
            this.completedTickets = completedTickets;
            this.overdueTickets = overdueTickets;
            this.avgSatisfactionRating = avgSatisfactionRating;
        }
        
        // Getters
        public int getTotalTickets() { return totalTickets; }
        public int getActiveTickets() { return activeTickets; }
        public int getCompletedTickets() { return completedTickets; }
        public int getOverdueTickets() { return overdueTickets; }
        public double getAvgSatisfactionRating() { return avgSatisfactionRating; }
        
        public double getCompletionRate() {
            return totalTickets > 0 ? (double) completedTickets / totalTickets * 100 : 0;
        }
        
        public double getOverdueRate() {
            return activeTickets > 0 ? (double) overdueTickets / activeTickets * 100 : 0;
        }
        
        public double getWorkloadScore() {
            return activeTickets * 0.4 + overdueTickets * 0.6;
        }
    }
}