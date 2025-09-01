package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.UserRepository;
import com.hostel.repository.CategoryStaffMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bulk Operations Service implementing mass updates and operations
 * as per IIM Trichy Product Design Document Section 4.1.4
 */
@Service
@Transactional
public class BulkOperationsService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryStaffMappingRepository mappingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TicketAssignmentService assignmentService;

    /**
     * Bulk update ticket status
     * Implements bulk operations from PDD Section 4.1.4
     */
    public BulkOperationResult bulkUpdateTicketStatus(List<UUID> ticketIds, TicketStatus newStatus, 
                                                     String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();

        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }

                // Validate status transition
                if (!isValidStatusTransition(ticket.getStatus(), newStatus)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + 
                        ": Invalid status transition from " + ticket.getStatus() + " to " + newStatus);
                    continue;
                }

                // Update status
                TicketStatus oldStatus = ticket.getStatus();
                ticket.setStatus(newStatus);
                ticket.setUpdatedAt(LocalDateTime.now());

                // Set appropriate timestamps
                switch (newStatus) {
                    case ASSIGNED:
                        ticket.setAssignedAt(LocalDateTime.now());
                        break;
                    case IN_PROGRESS:
                        ticket.setStartedAt(LocalDateTime.now());
                        break;
                    case RESOLVED:
                        ticket.setResolvedAt(LocalDateTime.now());
                        break;
                    case CLOSED:
                        ticket.setClosedAt(LocalDateTime.now());
                        break;
                }

                ticketRepository.save(ticket);

                // Create history entry
                createBulkHistoryEntry(ticket, oldStatus, newStatus, reason, performedBy);

                // Send notification
                notifyTicketStatusChange(ticket, oldStatus, newStatus, reason);

                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Status updated to " + newStatus);

            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": " + e.getMessage());
            }
        }

        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }

    /**
     * Bulk assign tickets to staff members
     */
    public BulkOperationResult bulkAssignTickets(List<UUID> ticketIds, UUID staffId, User performedBy) {
        User staff = userRepository.findById(staffId).orElse(null);
        if (staff == null || !staff.getRole().equals(UserRole.STAFF)) {
            return new BulkOperationResult(Collections.emptyList(), 
                Arrays.asList("Invalid staff member specified"));
        }

        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();

        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }

                if (!ticket.canBeAssigned()) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + 
                        ": Cannot be assigned in current status " + ticket.getStatus());
                    continue;
                }

                // Check staff capacity
                if (isStaffAtCapacity(staff)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + 
                        ": Staff member is at capacity");
                    continue;
                }

                // Assign ticket
                ticket.setAssignedTo(staff);
                ticket.setStatus(TicketStatus.ASSIGNED);
                ticket.setAssignedAt(LocalDateTime.now());
                ticket.setUpdatedAt(LocalDateTime.now());

                ticketRepository.save(ticket);

                // Create history entry
                createBulkHistoryEntry(ticket, ticket.getStatus(), TicketStatus.ASSIGNED, 
                    "Bulk assignment", performedBy);

                // Send notification
                notificationService.sendNotification(
                    staff,
                    "Ticket Assigned",
                    String.format("Ticket %s has been assigned to you via bulk operation", 
                        ticket.getTicketNumber()),
                    NotificationType.TICKET_ASSIGNED,
                    ticket
                );

                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Assigned to " + staff.getFullName());

            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": " + e.getMessage());
            }
        }

        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }

    /**
     * Bulk update ticket priority
     */
    public BulkOperationResult bulkUpdateTicketPriority(List<UUID> ticketIds, TicketPriority newPriority, 
                                                       String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();

        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }

                TicketPriority oldPriority = ticket.getPriority();
                ticket.setPriority(newPriority);
                ticket.setUpdatedAt(LocalDateTime.now());

                ticketRepository.save(ticket);

                // Create history entry
                createBulkHistoryEntry(ticket, null, null, 
                    String.format("Priority changed from %s to %s: %s", oldPriority, newPriority, reason), 
                    performedBy);

                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + 
                    ": Priority updated from " + oldPriority + " to " + newPriority);

            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": " + e.getMessage());
            }
        }

        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }

    /**
     * Bulk import users from CSV file
     * Implements bulk user operations from PDD Section 4.3.1
     */
    public BulkOperationResult bulkImportUsers(MultipartFile csvFile, User performedBy) {
        List<String> successfulImports = new ArrayList<>();
        List<String> failedImports = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            int lineNumber = 0;
            
            // Skip header line
            reader.readLine();
            lineNumber++;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    String[] fields = line.split(",");
                    if (fields.length < 6) {
                        failedImports.add("Line " + lineNumber + ": Insufficient fields");
                        continue;
                    }

                    // Parse user data: username,email,firstName,lastName,role,hostelBlock,roomNumber
                    String username = fields[0].trim();
                    String email = fields[1].trim();
                    String firstName = fields[2].trim();
                    String lastName = fields[3].trim();
                    String roleStr = fields[4].trim();
                    String hostelBlock = fields.length > 5 ? fields[5].trim() : null;
                    String roomNumber = fields.length > 6 ? fields[6].trim() : null;

                    // Validate required fields
                    if (username.isEmpty() || email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                        failedImports.add("Line " + lineNumber + ": Missing required fields");
                        continue;
                    }

                    // Parse role
                    UserRole role;
                    try {
                        role = UserRole.valueOf(roleStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        failedImports.add("Line " + lineNumber + ": Invalid role " + roleStr);
                        continue;
                    }

                    // Check for existing user
                    if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
                        failedImports.add("Line " + lineNumber + ": User already exists (username or email)");
                        continue;
                    }

                    // Create user
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRole(role);
                    user.setPasswordHash("$2a$12$defaultPassword"); // Default password, should be changed
                    
                    if (hostelBlock != null && !hostelBlock.isEmpty()) {
                        try {
                            user.setHostelBlock(HostelName.fromAnyName(hostelBlock));
                        } catch (Exception e) {
                            // Invalid hostel block, continue without setting it
                        }
                    }
                    
                    if (roomNumber != null && !roomNumber.isEmpty()) {
                        user.setRoomNumber(roomNumber);
                    }

                    userRepository.save(user);
                    successfulImports.add("Line " + lineNumber + ": User " + username + " created successfully");

                } catch (Exception e) {
                    failedImports.add("Line " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            failedImports.add("Error reading CSV file: " + e.getMessage());
        }

        return new BulkOperationResult(successfulImports, failedImports);
    }

    /**
     * Bulk create staff mappings
     */
    public BulkOperationResult bulkCreateStaffMappings(List<BulkMappingRequest> mappingRequests, User performedBy) {
        List<String> successfulCreations = new ArrayList<>();
        List<String> failedCreations = new ArrayList<>();

        for (BulkMappingRequest request : mappingRequests) {
            try {
                User staff = userRepository.findById(UUID.fromString(request.getStaffId())).orElse(null);
                if (staff == null || !staff.getRole().equals(UserRole.STAFF)) {
                    failedCreations.add("Invalid staff ID: " + request.getStaffId());
                    continue;
                }

                // Check if mapping already exists
                CategoryStaffMapping existingMapping = mappingRepository
                    .findByStaffAndCategoryAndIsActiveTrue(staff, request.getCategory());
                if (existingMapping != null) {
                    failedCreations.add("Mapping already exists for staff " + staff.getUsername() + 
                        " and category " + request.getCategory());
                    continue;
                }

                // Create mapping
                CategoryStaffMapping mapping = new CategoryStaffMapping();
                mapping.setStaff(staff);
                mapping.setHostelBlockString(request.getHostelBlock());
                mapping.setCategory(request.getCategory());
                mapping.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
                mapping.setCapacityWeight(request.getCapacityWeight() != null ? 
                    java.math.BigDecimal.valueOf(request.getCapacityWeight()) : java.math.BigDecimal.ONE);
                mapping.setExpertiseLevel(request.getExpertiseLevel() != null ? request.getExpertiseLevel() : 1);
                mapping.setIsActive(true);

                mappingRepository.save(mapping);
                successfulCreations.add("Mapping created for " + staff.getUsername() + 
                    " - " + request.getCategory() + " in " + request.getHostelBlock());

            } catch (Exception e) {
                failedCreations.add("Error creating mapping: " + e.getMessage());
            }
        }

        return new BulkOperationResult(successfulCreations, failedCreations);
    }

    /**
     * Bulk export tickets to CSV
     */
    public String exportTicketsToCSV(List<UUID> ticketIds) {
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("Ticket Number,Title,Category,Priority,Status,Created By,Assigned To,")
           .append("Hostel Block,Room Number,Created At,Resolved At,Satisfaction Rating\n");

        for (UUID ticketId : ticketIds) {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket != null) {
                csv.append(escapeCsvField(ticket.getTicketNumber())).append(",")
                   .append(escapeCsvField(ticket.getTitle())).append(",")
                   .append(escapeCsvField(ticket.getEffectiveCategory())).append(",")
                   .append(escapeCsvField(ticket.getPriority().toString())).append(",")
                   .append(escapeCsvField(ticket.getStatus().toString())).append(",")
                   .append(escapeCsvField(ticket.getCreatedBy().getFullName())).append(",")
                   .append(escapeCsvField(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "")).append(",")
                   .append(escapeCsvField(ticket.getHostelBlock())).append(",")
                   .append(escapeCsvField(ticket.getRoomNumber() != null ? ticket.getRoomNumber() : "")).append(",")
                   .append(escapeCsvField(ticket.getCreatedAt().toString())).append(",")
                   .append(escapeCsvField(ticket.getResolvedAt() != null ? ticket.getResolvedAt().toString() : "")).append(",")
                   .append(ticket.getSatisfactionRating() != null ? ticket.getSatisfactionRating() : "")
                   .append("\n");
            }
        }

        return csv.toString();
    }

    // Helper methods

    private boolean isValidStatusTransition(TicketStatus from, TicketStatus to) {
        // Implement status transition validation based on PDD workflow rules
        switch (from) {
            case OPEN:
                return Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.CANCELLED).contains(to);
            case ASSIGNED:
                return Arrays.asList(TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD, TicketStatus.CANCELLED).contains(to);
            case IN_PROGRESS:
                return Arrays.asList(TicketStatus.ON_HOLD, TicketStatus.RESOLVED, TicketStatus.CANCELLED).contains(to);
            case ON_HOLD:
                return Arrays.asList(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CANCELLED).contains(to);
            case RESOLVED:
                return Arrays.asList(TicketStatus.CLOSED, TicketStatus.REOPENED).contains(to);
            case CLOSED:
                return Arrays.asList(TicketStatus.REOPENED).contains(to);
            case CANCELLED:
                return Arrays.asList(TicketStatus.OPEN).contains(to);
            case REOPENED:
                return Arrays.asList(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED).contains(to);
            default:
                return false;
        }
    }

    private boolean isStaffAtCapacity(User staff) {
        // Use assignment service to check capacity
        return assignmentService.getStaffWorkloadStats(staff).getActiveTickets() >= getMaxCapacityForStaff(staff);
    }

    private int getMaxCapacityForStaff(User staff) {
        // Same logic as in TicketAssignmentService
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

    private void createBulkHistoryEntry(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, 
                                       String reason, User performedBy) {
        // This would integrate with TicketHistory entity
        // For now, just log the change
        System.out.println(String.format(
            "BULK OPERATION - Ticket %s: %s -> %s by %s. Reason: %s",
            ticket.getTicketNumber(),
            oldStatus != null ? oldStatus : "N/A",
            newStatus != null ? newStatus : "N/A",
            performedBy.getFullName(),
            reason
        ));
    }

    private void notifyTicketStatusChange(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, String reason) {
        String message = String.format(
            "Your ticket %s status has been updated from %s to %s via bulk operation. Reason: %s",
            ticket.getTicketNumber(), oldStatus, newStatus, reason
        );

        notificationService.sendNotification(
            ticket.getCreatedBy(),
            "Ticket Status Updated",
            message,
            NotificationType.STATUS_CHANGE,
            ticket
        );
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    /**
     * Bulk update ticket categories
     */
    public BulkOperationResult bulkUpdateTicketCategories(List<UUID> ticketIds, TicketCategory newCategory, 
                                                         String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();
        
        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }
                
                if (!canUserUpdateTicket(performedBy, ticket)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Insufficient permissions");
                    continue;
                }
                
                TicketCategory oldCategory = ticket.getCategory();
                ticket.setCategory(newCategory);
                
                // Reassign if category changed and ticket is assigned
                if (ticket.isAssigned() && oldCategory != newCategory) {
                    User newAssignee = assignmentService.autoAssignTicket(ticket);
                    if (newAssignee != null && !newAssignee.equals(ticket.getAssignedTo())) {
                        ticket.setAssignedTo(newAssignee);
                        ticket.setAssignedAt(LocalDateTime.now());
                        
                        // Notify new assignee
                        notificationService.sendNotification(
                                newAssignee,
                                "Ticket Reassigned Due to Category Change",
                                String.format("Ticket %s has been reassigned to you due to category change to %s",
                                        ticket.getTicketNumber(), newCategory.getDisplayName()),
                                NotificationType.TICKET_ASSIGNMENT,
                                ticket
                        );
                    }
                }
                
                ticketRepository.save(ticket);
                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Category updated to " + newCategory.getDisplayName());
                
            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": Error - " + e.getMessage());
            }
        }
        
        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }
    
    /**
     * Bulk update ticket priorities
     */
    public BulkOperationResult bulkUpdateTicketPriorities(List<UUID> ticketIds, TicketPriority newPriority, 
                                                         String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();
        
        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }
                
                if (!canUserUpdateTicket(performedBy, ticket)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Insufficient permissions");
                    continue;
                }
                
                TicketPriority oldPriority = ticket.getPriority();
                ticket.setPriority(newPriority);
                ticketRepository.save(ticket);
                
                // Notify assigned staff if priority increased
                if (ticket.isAssigned() && newPriority.isHigherThan(oldPriority)) {
                    notificationService.sendNotification(
                            ticket.getAssignedTo(),
                            "Ticket Priority Increased",
                            String.format("Priority of ticket %s has been increased to %s via bulk operation",
                                    ticket.getTicketNumber(), newPriority.getDisplayName()),
                            NotificationType.STATUS_UPDATE,
                            ticket
                    );
                }
                
                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Priority updated to " + newPriority.getDisplayName());
                
            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": Error - " + e.getMessage());
            }
        }
        
        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }
    
    /**
     * Bulk auto-assign tickets based on intelligent assignment algorithm
     */
    public BulkOperationResult bulkAutoAssignTickets(List<UUID> ticketIds, String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();
        
        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }
                
                if (!canUserUpdateTicket(performedBy, ticket)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Insufficient permissions");
                    continue;
                }
                
                if (!ticket.canBeAssigned()) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Cannot be assigned in current status");
                    continue;
                }
                
                User assignedStaff = assignmentService.autoAssignTicket(ticket);
                if (assignedStaff != null) {
                    ticket.setAssignedTo(assignedStaff);
                    ticket.setStatus(TicketStatus.ASSIGNED);
                    ticket.setAssignedAt(LocalDateTime.now());
                    
                    ticketRepository.save(ticket);
                    
                    // Send notification
                    notificationService.sendNotification(
                            assignedStaff,
                            "Auto-Assignment - New Ticket",
                            String.format("Ticket %s has been auto-assigned to you via bulk operation",
                                    ticket.getTicketNumber()),
                            NotificationType.TICKET_ASSIGNMENT,
                            ticket
                    );
                    
                    successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Auto-assigned to " + assignedStaff.getFullName());
                } else {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": No suitable staff found for assignment");
                }
                
            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": Error - " + e.getMessage());
            }
        }
        
        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }
    
    /**
     * Bulk close tickets with validation
     */
    public BulkOperationResult bulkCloseTickets(List<UUID> ticketIds, String reason, User performedBy) {
        List<String> successfulUpdates = new ArrayList<>();
        List<String> failedUpdates = new ArrayList<>();
        
        for (UUID ticketId : ticketIds) {
            try {
                Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
                if (ticket == null) {
                    failedUpdates.add("Ticket " + ticketId + ": Not found");
                    continue;
                }
                
                if (!canUserUpdateTicket(performedBy, ticket)) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Insufficient permissions");
                    continue;
                }
                
                if (!ticket.isResolved()) {
                    failedUpdates.add("Ticket " + ticket.getTicketNumber() + ": Cannot close non-resolved ticket");
                    continue;
                }
                
                ticket.setStatus(TicketStatus.CLOSED);
                ticket.setClosedAt(LocalDateTime.now());
                
                ticketRepository.save(ticket);
                
                // Notify ticket creator
                notificationService.sendNotification(
                        ticket.getCreatedBy(),
                        "Ticket Closed",
                        String.format("Your ticket %s has been closed via bulk operation. Reason: %s",
                                ticket.getTicketNumber(), reason != null ? reason : "Bulk closure"),
                        NotificationType.STATUS_UPDATE,
                        ticket
                );
                
                successfulUpdates.add("Ticket " + ticket.getTicketNumber() + ": Successfully closed");
                
            } catch (Exception e) {
                failedUpdates.add("Ticket " + ticketId + ": Error - " + e.getMessage());
            }
        }
        
        return new BulkOperationResult(successfulUpdates, failedUpdates);
    }

    // Data classes

    public static class BulkOperationResult {
        private final List<String> successfulOperations;
        private final List<String> failedOperations;

        public BulkOperationResult(List<String> successfulOperations, List<String> failedOperations) {
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
        }

        public List<String> getSuccessfulOperations() { return successfulOperations; }
        public List<String> getFailedOperations() { return failedOperations; }
        public int getSuccessCount() { return successfulOperations.size(); }
        public int getFailureCount() { return failedOperations.size(); }
        public int getTotalCount() { return successfulOperations.size() + failedOperations.size(); }
        public double getSuccessRate() { 
            return getTotalCount() > 0 ? (double) getSuccessCount() / getTotalCount() * 100 : 0;
        }
    }

    public static class BulkMappingRequest {
        private String staffId;
        private String hostelBlock;
        private String category;
        private Integer priorityLevel;
        private Double capacityWeight;
        private Integer expertiseLevel;

        // Getters and setters
        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
        public Double getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(Double capacityWeight) { this.capacityWeight = capacityWeight; }
        public Integer getExpertiseLevel() { return expertiseLevel; }
        public void setExpertiseLevel(Integer expertiseLevel) { this.expertiseLevel = expertiseLevel; }
    }
}
