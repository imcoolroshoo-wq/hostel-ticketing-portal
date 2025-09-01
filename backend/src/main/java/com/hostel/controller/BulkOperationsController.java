package com.hostel.controller;

import com.hostel.entity.TicketStatus;
import com.hostel.entity.TicketPriority;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.User;
import com.hostel.service.BulkOperationsService;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Bulk Operations Controller for mass updates and operations
 * Implements bulk operations endpoints as per Product Design Document
 */
@RestController
@RequestMapping("/api/bulk")
@CrossOrigin(origins = {"http://localhost:3000", "https://hostel-ticketing-frontend.onrender.com"})
public class BulkOperationsController {

    @Autowired
    private BulkOperationsService bulkOperationsService;

    @Autowired
    private UserService userService;

    /**
     * Bulk update ticket status
     */
    @PostMapping("/tickets/status")
    public ResponseEntity<?> bulkUpdateTicketStatus(@RequestBody BulkStatusUpdateRequest request) {
        try {
            // Get the user performing the operation (this would be from authentication context in real app)
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkUpdateTicketStatus(
                request.getTicketIds(),
                request.getNewStatus(),
                request.getReason(),
                performedBy
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk status update: " + e.getMessage());
        }
    }

    /**
     * Bulk assign tickets to staff
     */
    @PostMapping("/tickets/assign")
    public ResponseEntity<?> bulkAssignTickets(@RequestBody BulkAssignmentRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkAssignTickets(
                request.getTicketIds(),
                request.getStaffId(),
                performedBy
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk assignment: " + e.getMessage());
        }
    }

    /**
     * Bulk update ticket priority
     */
    @PostMapping("/tickets/priority")
    public ResponseEntity<?> bulkUpdateTicketPriority(@RequestBody BulkPriorityUpdateRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkUpdateTicketPriority(
                request.getTicketIds(),
                request.getNewPriority(),
                request.getReason(),
                performedBy
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk priority update: " + e.getMessage());
        }
    }

    /**
     * Bulk import users from CSV
     */
    @PostMapping("/users/import")
    public ResponseEntity<?> bulkImportUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("performedById") UUID performedById) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            User performedBy = userService.getUserByIdDirect(performedById);
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkImportUsers(file, performedBy);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk user import: " + e.getMessage());
        }
    }

    /**
     * Bulk create staff mappings
     */
    @PostMapping("/mappings/create")
    public ResponseEntity<?> bulkCreateStaffMappings(@RequestBody BulkMappingCreationRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkCreateStaffMappings(
                request.getMappingRequests(),
                performedBy
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in bulk mapping creation: " + e.getMessage());
        }
    }

    /**
     * Export tickets to CSV
     */
    @PostMapping("/tickets/export")
    public ResponseEntity<String> exportTicketsToCSV(@RequestBody TicketExportRequest request) {
        try {
            String csvContent = bulkOperationsService.exportTicketsToCSV(request.getTicketIds());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "tickets_export.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error exporting tickets: " + e.getMessage());
        }
    }

    // Request/Response classes

    public static class BulkStatusUpdateRequest {
        private List<UUID> ticketIds;
        private TicketStatus newStatus;
        private String reason;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public TicketStatus getNewStatus() { return newStatus; }
        public void setNewStatus(TicketStatus newStatus) { this.newStatus = newStatus; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class BulkAssignmentRequest {
        private List<UUID> ticketIds;
        private UUID staffId;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public UUID getStaffId() { return staffId; }
        public void setStaffId(UUID staffId) { this.staffId = staffId; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class BulkPriorityUpdateRequest {
        private List<UUID> ticketIds;
        private TicketPriority newPriority;
        private String reason;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public TicketPriority getNewPriority() { return newPriority; }
        public void setNewPriority(TicketPriority newPriority) { this.newPriority = newPriority; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class BulkMappingCreationRequest {
        private List<BulkOperationsService.BulkMappingRequest> mappingRequests;
        private UUID performedById;

        public List<BulkOperationsService.BulkMappingRequest> getMappingRequests() { return mappingRequests; }
        public void setMappingRequests(List<BulkOperationsService.BulkMappingRequest> mappingRequests) { 
            this.mappingRequests = mappingRequests; 
        }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    /**
     * Bulk update ticket categories
     */
    @PostMapping("/tickets/categories")
    public ResponseEntity<?> bulkUpdateTicketCategories(@RequestBody BulkCategoryUpdateRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkUpdateTicketCategories(
                request.getTicketIds(),
                request.getNewCategory(),
                request.getReason(),
                performedBy
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Bulk auto-assign tickets
     */
    @PostMapping("/tickets/auto-assign")
    public ResponseEntity<?> bulkAutoAssignTickets(@RequestBody BulkAutoAssignRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkAutoAssignTickets(
                request.getTicketIds(),
                request.getReason(),
                performedBy
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Bulk close tickets
     */
    @PostMapping("/tickets/close")
    public ResponseEntity<?> bulkCloseTickets(@RequestBody BulkCloseRequest request) {
        try {
            User performedBy = userService.getUserByIdDirect(request.getPerformedById());
            if (performedBy == null) {
                return ResponseEntity.badRequest().body("Invalid user ID");
            }

            BulkOperationsService.BulkOperationResult result = bulkOperationsService.bulkCloseTickets(
                request.getTicketIds(),
                request.getReason(),
                performedBy
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    public static class BulkCategoryUpdateRequest {
        private List<UUID> ticketIds;
        private TicketCategory newCategory;
        private String reason;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public TicketCategory getNewCategory() { return newCategory; }
        public void setNewCategory(TicketCategory newCategory) { this.newCategory = newCategory; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class BulkAutoAssignRequest {
        private List<UUID> ticketIds;
        private String reason;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class BulkCloseRequest {
        private List<UUID> ticketIds;
        private String reason;
        private UUID performedById;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public UUID getPerformedById() { return performedById; }
        public void setPerformedById(UUID performedById) { this.performedById = performedById; }
    }

    public static class TicketExportRequest {
        private List<UUID> ticketIds;

        public List<UUID> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<UUID> ticketIds) { this.ticketIds = ticketIds; }
    }
}
