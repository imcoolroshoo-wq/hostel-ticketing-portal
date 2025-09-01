package com.hostel.controller;

import com.hostel.dto.DTOMapper;
import com.hostel.dto.TicketDTO;
import com.hostel.entity.Ticket;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.TicketHistory;
import com.hostel.entity.TicketPriority;
import com.hostel.entity.TicketStatus;
import com.hostel.entity.User;
import com.hostel.entity.UserRole;
import com.hostel.repository.TicketRepository;
import com.hostel.service.TicketAssignmentService;
import com.hostel.service.TicketService;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private TicketAssignmentService ticketAssignmentService;

    // Get all tickets with pagination
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<Ticket> tickets = ticketService.getAllTickets(pageable);
            
            // Create simplified response to avoid DTO issues
            List<Map<String, Object>> ticketList = tickets.getContent().stream()
                    .map(ticket -> {
                        Map<String, Object> ticketMap = new HashMap<>();
                        ticketMap.put("id", ticket.getId());
                        ticketMap.put("ticketNumber", ticket.getTicketNumber());
                        ticketMap.put("title", ticket.getTitle());
                        ticketMap.put("description", ticket.getDescription());
                        ticketMap.put("category", ticket.getCategory());
                        ticketMap.put("priority", ticket.getPriority());
                        ticketMap.put("status", ticket.getStatus());
                        ticketMap.put("hostelBlock", ticket.getHostelBlock());
                        ticketMap.put("roomNumber", ticket.getRoomNumber());
                        ticketMap.put("locationDetails", ticket.getLocationDetails());
                        ticketMap.put("createdAt", ticket.getCreatedAt());
                        ticketMap.put("updatedAt", ticket.getUpdatedAt());
                        ticketMap.put("resolvedAt", ticket.getResolvedAt());
                        
                        // Safe handling of user references with try-catch
                        try {
                            if (ticket.getCreatedBy() != null) {
                                Map<String, Object> createdBy = new HashMap<>();
                                createdBy.put("id", ticket.getCreatedBy().getId());
                                createdBy.put("email", ticket.getCreatedBy().getEmail());
                                createdBy.put("role", ticket.getCreatedBy().getRole());
                                ticketMap.put("createdBy", createdBy);
                            }
                        } catch (Exception userEx) {
                            // Skip user data if there's an issue
                            System.err.println("Error accessing createdBy user: " + userEx.getMessage());
                        }
                        
                        try {
                            if (ticket.getAssignedTo() != null) {
                                Map<String, Object> assignedTo = new HashMap<>();
                                assignedTo.put("id", ticket.getAssignedTo().getId());
                                assignedTo.put("email", ticket.getAssignedTo().getEmail());
                                assignedTo.put("role", ticket.getAssignedTo().getRole());
                                ticketMap.put("assignedTo", assignedTo);
                            }
                        } catch (Exception userEx) {
                            // Skip user data if there's an issue
                            System.err.println("Error accessing assignedTo user: " + userEx.getMessage());
                        }
                        
                        return ticketMap;
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("tickets", ticketList);
            response.put("currentPage", tickets.getNumber());
            response.put("totalItems", tickets.getTotalElements());
            response.put("totalPages", tickets.getTotalPages());
            response.put("size", tickets.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error fetching tickets: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error fetching tickets: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Get ticket by ID - Admin can view any ticket, Staff can view assigned tickets, Students can view their own tickets
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable UUID id, @RequestParam(required = false) UUID userId) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
            if (!ticketOpt.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            Ticket ticket = ticketOpt.get();
            
            // If userId is provided, check permissions
            if (userId != null) {
                User user = userService.getUserByIdDirect(userId);
                if (user != null) {
                    // Check permissions based on role
                    if (user.getRole() == UserRole.STUDENT) {
                        // Students can only view their own tickets
                        if (!ticket.getCreatedBy().getId().equals(userId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("message", "Students can only view their own tickets");
                            return ResponseEntity.status(403).body(error);
                        }
                    } else if (user.getRole() == UserRole.STAFF) {
                        // Staff can view tickets assigned to them or unassigned tickets in their category
                        if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(userId)) {
                            Map<String, String> error = new HashMap<>();
                            error.put("message", "Staff can only view tickets assigned to them");
                            return ResponseEntity.status(403).body(error);
                        }
                    }
                    // Admins can view any ticket - no additional check needed
                }
            }
            
            return ResponseEntity.ok(DTOMapper.toTicketDTO(ticket));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Create new ticket
    @PostMapping
    public ResponseEntity<?> createTicket(
            @RequestBody Ticket ticket,
            @RequestParam UUID creatorId) {
        System.out.println("[DEBUG] Incoming ticket: " + ticket);
        System.out.println("[DEBUG] Incoming creatorId: " + creatorId);
        try {
            Ticket createdTicket = ticketService.createTicket(ticket, creatorId);
            TicketDTO ticketDTO = DTOMapper.toTicketDTO(createdTicket);
            return ResponseEntity.ok(ticketDTO);
        } catch (Exception e) {
            System.err.println("Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Simple ticket creation endpoint for testing
    @PostMapping("/simple")
    public ResponseEntity<?> createSimpleTicket(
            @RequestBody Map<String, Object> ticketData,
            @RequestParam UUID creatorId) {
        try {
            // Create a completely new ticket to avoid any potential issues
            Ticket newTicket = new Ticket();
            newTicket.setTitle((String) ticketData.get("title"));
            newTicket.setDescription((String) ticketData.get("description"));
            
            // Handle category conversion
            String categoryStr = (String) ticketData.get("category");
            if (categoryStr != null) {
                try {
                    newTicket.setCategory(TicketCategory.valueOf(categoryStr));
                } catch (IllegalArgumentException e) {
                    newTicket.setCategory(TicketCategory.GENERAL);
                }
            }
            
            // Handle priority conversion
            String priorityStr = (String) ticketData.get("priority");
            if (priorityStr != null) {
                try {
                    newTicket.setPriority(TicketPriority.valueOf(priorityStr));
                } catch (IllegalArgumentException e) {
                    newTicket.setPriority(TicketPriority.MEDIUM);
                }
            }
            
            // Set creator by fetching from database first for hostel block assignment
            User creator = userService.getUserByIdDirect(creatorId);
            if (creator == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                errorResponse.put("message", "Creator user with ID " + creatorId + " not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Handle hostel block - auto-assign from user if not provided
            String hostelBlockParam = (String) ticketData.get("hostelBlock");
            if (hostelBlockParam == null || hostelBlockParam.trim().isEmpty()) {
                if (creator.getHostelBlock() != null) {
                    // Use the display name since that's what the Ticket entity expects as String
                    newTicket.setHostelBlock(creator.getHostelBlock().getDisplayName());
                } else {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Hostel block required");
                    errorResponse.put("message", "Hostel block is required. User profile doesn't have hostel block information.");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            } else {
                newTicket.setHostelBlock(hostelBlockParam);
            }
            
            newTicket.setRoomNumber((String) ticketData.get("roomNumber"));
            newTicket.setLocationDetails((String) ticketData.get("locationDetails"));
            
            // Set required fields
            newTicket.setTicketNumber("TKT-" + System.currentTimeMillis());
            newTicket.setStatus(TicketStatus.OPEN);
            newTicket.setCreatedAt(LocalDateTime.now());
            newTicket.setUpdatedAt(LocalDateTime.now());
            newTicket.setCreatedBy(creator);
            
            // Auto-assign based on category and priority
            if (newTicket.getAssignedTo() == null) {
                User autoAssignedUser = ticketAssignmentService.autoAssignTicket(newTicket);
                if (autoAssignedUser != null) {
                    newTicket.setAssignedTo(autoAssignedUser);
                    newTicket.setStatus(TicketStatus.ASSIGNED);
                }
                // If no staff members are available, leave assignedTo as null and status as OPEN
            }
            
            // Save ticket with auto-assignment
            Ticket savedTicket = ticketRepository.save(newTicket);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTicket.getId());
            response.put("ticketNumber", savedTicket.getTicketNumber());
            response.put("title", savedTicket.getTitle());
            response.put("status", savedTicket.getStatus());
            response.put("message", "Ticket created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating simple ticket: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Update ticket - Admin can update any ticket, Students can update their own tickets
    @PutMapping("/{id}")

    public ResponseEntity<?> updateTicket(
            @PathVariable UUID id,
            @RequestBody Ticket ticketDetails,
            @RequestParam UUID updatedBy) {
        try {
            // Get the current ticket
            Ticket existingTicket = ticketService.getTicketByIdDirect(id);
            if (existingTicket == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            // Get the user making the update
            User updater = userService.getUserByIdDirect(updatedBy);
            if (updater == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check permissions
            if (updater.getRole() == UserRole.STUDENT) {
                // Students can only edit their own tickets and only if not assigned
                if (!existingTicket.getCreatedBy().getId().equals(updatedBy)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Students can only edit their own tickets");
                    return ResponseEntity.status(403).body(error);
                }
                if (existingTicket.getAssignedTo() != null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Cannot edit ticket that has been assigned to staff");
                    return ResponseEntity.status(403).body(error);
                }
            }
            
            Ticket updatedTicket = ticketService.updateTicket(id, ticketDetails);
            return ResponseEntity.ok(DTOMapper.toTicketDTO(updatedTicket));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Delete ticket (soft delete - change status to cancelled)
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get tickets by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getTicketsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> tickets = ticketService.getTicketsByUser(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", tickets.getContent());
        response.put("currentPage", tickets.getNumber());
        response.put("totalItems", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    // Get tickets by assignee
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<Map<String, Object>> getTicketsByAssignee(
            @PathVariable UUID assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> tickets = ticketService.getTicketsByAssignee(assigneeId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", tickets.getContent());
        response.put("currentPage", tickets.getNumber());
        response.put("totalItems", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    // Get tickets by status
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> tickets = ticketService.getTicketsByStatus(status, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", tickets.getContent());
        response.put("currentPage", tickets.getNumber());
        response.put("totalItems", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        
        return ResponseEntity.ok(response);
    }

    // Search tickets
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTickets(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ticket> tickets = ticketService.searchTickets(query, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", tickets.getContent());
        response.put("currentPage", tickets.getNumber());
        response.put("totalItems", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        response.put("searchQuery", query);
        
        return ResponseEntity.ok(response);
    }

    // Change ticket status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> changeTicketStatus(
            @PathVariable UUID id,
            @RequestParam TicketStatus newStatus,
            @RequestParam(required = false) String comment,
            @RequestParam UUID userId) {
        try {
            Ticket updatedTicket = ticketService.changeTicketStatus(id, newStatus, comment, userId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Assign ticket
    @PatchMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable UUID id,
            @RequestParam UUID assigneeId) {
        try {
            Ticket updatedTicket = ticketService.assignTicket(id, assigneeId);
            return ResponseEntity.ok(updatedTicket);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get urgent tickets
    @GetMapping("/urgent")
    public ResponseEntity<List<Ticket>> getUrgentTickets() {
        List<Ticket> urgentTickets = ticketService.getUrgentTickets();
        return ResponseEntity.ok(urgentTickets);
    }

    // Get overdue tickets
    @GetMapping("/overdue")
    public ResponseEntity<List<Ticket>> getOverdueTickets() {
        List<Ticket> overdueTickets = ticketService.getOverdueTickets();
        return ResponseEntity.ok(overdueTickets);
    }

    // Get ticket statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by status
        stats.put("open", ticketService.getTicketCountByStatus(TicketStatus.OPEN));
        stats.put("inProgress", ticketService.getTicketCountByStatus(TicketStatus.IN_PROGRESS));
        stats.put("resolved", ticketService.getTicketCountByStatus(TicketStatus.RESOLVED));
        stats.put("closed", ticketService.getTicketCountByStatus(TicketStatus.CLOSED));
        
        // Count by priority
        stats.put("low", ticketService.getTicketCountByPriority(com.hostel.entity.TicketPriority.LOW));
        stats.put("medium", ticketService.getTicketCountByPriority(com.hostel.entity.TicketPriority.MEDIUM));
        stats.put("high", ticketService.getTicketCountByPriority(com.hostel.entity.TicketPriority.HIGH));
        stats.put("emergency", ticketService.getTicketCountByPriority(com.hostel.entity.TicketPriority.EMERGENCY));
        
        return ResponseEntity.ok(stats);
    }

    // Staff-specific endpoints
    @GetMapping("/unassigned")

    public ResponseEntity<List<TicketDTO>> getUnassignedTickets() {
        List<Ticket> tickets = ticketService.getUnassignedTickets();
        List<TicketDTO> ticketDTOs = tickets.stream()
                .map(DTOMapper::toTicketDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDTOs);
    }
    
    @GetMapping("/assigned/{staffId}")
    public ResponseEntity<List<TicketDTO>> getTicketsAssignedToStaff(@PathVariable UUID staffId) {
        List<Ticket> tickets = ticketService.getTicketsAssignedToStaff(staffId);
        List<TicketDTO> ticketDTOs = tickets.stream()
                .map(DTOMapper::toTicketDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDTOs);
    }
    
    @PostMapping("/{ticketId}/assign/{staffId}")

    public ResponseEntity<?> assignTicketToStaff(
            @PathVariable UUID ticketId, 
            @PathVariable UUID staffId,
            @RequestParam UUID requestedBy) {
        try {
            // Get the user making the request
            User requester = userService.getUserByIdDirect(requestedBy);
            if (requester == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Requester not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check permissions
            if (requester.getRole() == UserRole.STAFF) {
                // Staff can only assign tickets to themselves
                if (!staffId.equals(requestedBy)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Staff can only assign tickets to themselves");
                    return ResponseEntity.status(403).body(error);
                }
            }
            // Admins can assign to anyone (no additional check needed)
            
            Ticket ticket = ticketService.assignTicketToStaff(ticketId, staffId);
            return ResponseEntity.ok(DTOMapper.toTicketDTO(ticket));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{ticketId}/status")

    public ResponseEntity<?> updateTicketStatus(
            @PathVariable UUID ticketId,
            @RequestParam TicketStatus status,
            @RequestParam UUID updatedBy) {
        try {
            // Get the user making the request
            User updater = userService.getUserByIdDirect(updatedBy);
            if (updater == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Get the ticket
            Ticket ticket = ticketService.getTicketByIdDirect(ticketId);
            if (ticket == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            // Check permissions based on role
            if (updater.getRole() == UserRole.STUDENT) {
                // Students can only close or reopen their own tickets
                if (!ticket.getCreatedBy().getId().equals(updatedBy)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Students can only update their own tickets");
                    return ResponseEntity.status(403).body(error);
                }
                if (status != TicketStatus.CLOSED && status != TicketStatus.REOPENED) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Students can only close or reopen tickets");
                    return ResponseEntity.status(403).body(error);
                }
            }
            
            Ticket updatedTicket = ticketService.updateTicketStatus(ticketId, status, updatedBy);
            return ResponseEntity.ok(DTOMapper.toTicketDTO(updatedTicket));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/filters")
    public ResponseEntity<List<TicketDTO>> getTicketsByFilters(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) com.hostel.entity.TicketCategory category,
            @RequestParam(required = false) com.hostel.entity.TicketPriority priority,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) UUID assignedTo) {
        
        List<Ticket> tickets = ticketService.getTicketsByFilters(status, category, priority, building, assignedTo);
        List<TicketDTO> ticketDTOs = tickets.stream()
                .map(DTOMapper::toTicketDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketDTOs);
    }
    
    @GetMapping("/{id}/history")

    public ResponseEntity<?> getTicketHistory(@PathVariable UUID id, @RequestParam UUID userId) {
        try {
            // Get the user making the request
            User requester = userService.getUserByIdDirect(userId);
            if (requester == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Get the ticket
            Ticket ticket = ticketService.getTicketByIdDirect(id);
            if (ticket == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            // Check permissions based on role
            if (requester.getRole() == UserRole.STUDENT) {
                // Students can only view history of their own tickets
                if (!ticket.getCreatedBy().getId().equals(userId)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Students can only view history of their own tickets");
                    return ResponseEntity.status(403).body(error);
                }
            } else if (requester.getRole() == UserRole.STAFF) {
                // Staff can view history of tickets assigned to them
                if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(userId)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Staff can only view history of tickets assigned to them");
                    return ResponseEntity.status(403).body(error);
                }
            }
            // Admins can view any ticket history (no additional check needed)
            
            List<TicketHistory> history = ticketService.getTicketHistory(id);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Unassign ticket - Admin only
    @PatchMapping("/{id}/unassign")
    public ResponseEntity<?> unassignTicket(@PathVariable UUID id, @RequestParam UUID adminId) {
        try {
            // Verify admin permissions
            User admin = userService.getUserByIdDirect(adminId);
            if (admin == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "User not found");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (admin.getRole() != UserRole.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Only admins can unassign tickets");
                return ResponseEntity.status(403).body(error);
            }
            
            // Get the ticket
            Ticket ticket = ticketService.getTicketByIdDirect(id);
            if (ticket == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            // Check if ticket is currently assigned
            if (ticket.getAssignedTo() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket is not currently assigned");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Unassign the ticket
            Ticket unassignedTicket = ticketService.unassignTicket(id, adminId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket unassigned successfully");
            response.put("ticket", DTOMapper.toTicketDTO(unassignedTicket));
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Check for duplicate tickets
    @PostMapping("/check-duplicates")
    public ResponseEntity<?> checkDuplicates(@RequestBody Map<String, Object> requestData) {
        try {
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            UUID creatorId = UUID.fromString((String) requestData.get("creatorId"));
            
            // Use the same duplicate detection logic from TicketService
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Ticket> recentTickets = ticketRepository.findByCreatedByIdAndCreatedAtAfter(creatorId, thirtyDaysAgo);
            
            List<Map<String, Object>> similarTickets = new ArrayList<>();
            String lowerTitle = title.toLowerCase();
            String lowerDescription = description.toLowerCase();
            
            for (Ticket existingTicket : recentTickets) {
                // Skip closed tickets
                if (existingTicket.getStatus() == TicketStatus.CLOSED) {
                    continue;
                }
                
                String existingTitle = existingTicket.getTitle().toLowerCase();
                String existingDescription = existingTicket.getDescription().toLowerCase();
                
                // Simple similarity check
                double titleSimilarity = calculateSimilarity(lowerTitle, existingTitle);
                double descriptionSimilarity = calculateSimilarity(lowerDescription, existingDescription);
                
                if (titleSimilarity > 0.7 || descriptionSimilarity > 0.8) {
                    Map<String, Object> similarTicket = new HashMap<>();
                    similarTicket.put("id", existingTicket.getId());
                    similarTicket.put("ticketNumber", existingTicket.getTicketNumber());
                    similarTicket.put("title", existingTicket.getTitle());
                    similarTicket.put("status", existingTicket.getStatus());
                    similarTicket.put("similarity", Math.max(titleSimilarity, descriptionSimilarity));
                    similarTickets.add(similarTicket);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasDuplicates", !similarTickets.isEmpty());
            response.put("similarTickets", similarTickets);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Helper method for similarity calculation
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        
        for (String word : words1) {
            if (word.length() > 3) {
                set1.add(word);
            }
        }
        
        for (String word : words2) {
            if (word.length() > 3) {
                set2.add(word);
            }
        }
        
        if (set1.isEmpty() && set2.isEmpty()) return 0.0;
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    // Submit feedback for resolved ticket - Student only
    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> feedbackData,
            @RequestParam UUID studentId) {
        try {
            // Verify student permissions
            User student = userService.getUserByIdDirect(studentId);
            if (student == null || student.getRole() != UserRole.STUDENT) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Only students can submit feedback");
                return ResponseEntity.status(403).body(error);
            }
            
            // Get the ticket
            Ticket ticket = ticketService.getTicketByIdDirect(id);
            if (ticket == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(error);
            }
            
            // Verify student owns the ticket
            if (!ticket.getCreatedBy().getId().equals(studentId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Students can only provide feedback for their own tickets");
                return ResponseEntity.status(403).body(error);
            }
            
            // Verify ticket is resolved
            if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Feedback can only be submitted for resolved tickets");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Extract feedback data
            Integer rating = (Integer) feedbackData.get("rating");
            String feedback = (String) feedbackData.get("feedback");
            
            // Validate rating
            if (rating == null || rating < 1 || rating > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Rating must be between 1 and 5");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Update ticket with feedback
            ticket.setSatisfactionRating(rating);
            ticket.setFeedback(feedback);
            ticket.setUpdatedAt(LocalDateTime.now());
            
            // Auto-close ticket after feedback if it was just resolved
            if (ticket.getStatus() == TicketStatus.RESOLVED) {
                ticket.setStatus(TicketStatus.CLOSED);
                ticket.setClosedAt(LocalDateTime.now());
            }
            
            ticketRepository.save(ticket);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Feedback submitted successfully");
            response.put("ticket", DTOMapper.toTicketDTO(ticket));
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Get feedback statistics - Admin/Staff only
    @GetMapping("/feedback/stats")
    public ResponseEntity<?> getFeedbackStats(@RequestParam UUID userId) {
        try {
            User user = userService.getUserByIdDirect(userId);
            if (user == null || user.getRole() == UserRole.STUDENT) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Access denied");
                return ResponseEntity.status(403).body(error);
            }
            
            // Calculate feedback statistics
            List<Ticket> ticketsWithFeedback = ticketRepository.findTicketsWithFeedback();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFeedbacks", ticketsWithFeedback.size());
            
            if (!ticketsWithFeedback.isEmpty()) {
                double averageRating = ticketsWithFeedback.stream()
                    .mapToInt(Ticket::getSatisfactionRating)
                    .average()
                    .orElse(0.0);
                
                stats.put("averageRating", Math.round(averageRating * 100.0) / 100.0);
                
                // Rating distribution
                Map<Integer, Long> ratingDistribution = ticketsWithFeedback.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        Ticket::getSatisfactionRating,
                        java.util.stream.Collectors.counting()));
                
                stats.put("ratingDistribution", ratingDistribution);
                
                // Recent feedback
                List<Map<String, Object>> recentFeedback = ticketsWithFeedback.stream()
                    .sorted((t1, t2) -> t2.getUpdatedAt().compareTo(t1.getUpdatedAt()))
                    .limit(10)
                    .map(ticket -> {
                        Map<String, Object> feedbackInfo = new HashMap<>();
                        feedbackInfo.put("ticketNumber", ticket.getTicketNumber());
                        feedbackInfo.put("title", ticket.getTitle());
                        feedbackInfo.put("rating", ticket.getSatisfactionRating());
                        feedbackInfo.put("feedback", ticket.getFeedback());
                        feedbackInfo.put("submittedAt", ticket.getUpdatedAt());
                        feedbackInfo.put("category", ticket.getCategory());
                        return feedbackInfo;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                stats.put("recentFeedback", recentFeedback);
            } else {
                stats.put("averageRating", 0.0);
                stats.put("ratingDistribution", new HashMap<>());
                stats.put("recentFeedback", new java.util.ArrayList<>());
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Bulk operations for admin
    @PostMapping("/bulk-update")
    public ResponseEntity<?> bulkUpdateTickets(@RequestBody Map<String, Object> bulkData, @RequestParam UUID adminId) {
        try {
            // Verify admin permissions
            User admin = userService.getUserByIdDirect(adminId);
            if (admin == null || admin.getRole() != UserRole.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Only admins can perform bulk operations");
                return ResponseEntity.status(403).body(error);
            }
            
            @SuppressWarnings("unchecked")
            List<String> ticketIds = (List<String>) bulkData.get("ticketIds");
            String operation = (String) bulkData.get("operation");
            String newStatus = (String) bulkData.get("newStatus");
            String assigneeId = (String) bulkData.get("assigneeId");
            String newPriority = (String) bulkData.get("newPriority");
            
            if (ticketIds == null || ticketIds.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "No tickets selected");
                return ResponseEntity.badRequest().body(error);
            }
            
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (String ticketIdStr : ticketIds) {
                try {
                    UUID ticketId = UUID.fromString(ticketIdStr);
                    Ticket ticket = ticketService.getTicketByIdDirect(ticketId);
                    
                    if (ticket == null) {
                        results.add(Map.of(
                            "ticketId", ticketIdStr,
                            "success", false,
                            "message", "Ticket not found"
                        ));
                        failureCount++;
                        continue;
                    }
                    
                    boolean updated = false;
                    
                    // Perform the requested operation
                    switch (operation) {
                        case "UPDATE_STATUS":
                            if (newStatus != null) {
                                try {
                                    TicketStatus status = TicketStatus.valueOf(newStatus);
                                    ticket.setStatus(status);
                                    updated = true;
                                } catch (IllegalArgumentException e) {
                                    results.add(Map.of(
                                        "ticketId", ticketIdStr,
                                        "success", false,
                                        "message", "Invalid status: " + newStatus
                                    ));
                                    failureCount++;
                                    continue;
                                }
                            }
                            break;
                            
                        case "ASSIGN":
                            if (assigneeId != null) {
                                User assignee = userService.getUserByIdDirect(UUID.fromString(assigneeId));
                                if (assignee != null && assignee.getRole() == UserRole.STAFF) {
                                    ticket.setAssignedTo(assignee);
                                    ticket.setStatus(TicketStatus.ASSIGNED);
                                    updated = true;
                                } else {
                                    results.add(Map.of(
                                        "ticketId", ticketIdStr,
                                        "success", false,
                                        "message", "Invalid assignee"
                                    ));
                                    failureCount++;
                                    continue;
                                }
                            }
                            break;
                            
                        case "UPDATE_PRIORITY":
                            if (newPriority != null) {
                                try {
                                    TicketPriority priority = TicketPriority.valueOf(newPriority);
                                    ticket.setPriority(priority);
                                    updated = true;
                                } catch (IllegalArgumentException e) {
                                    results.add(Map.of(
                                        "ticketId", ticketIdStr,
                                        "success", false,
                                        "message", "Invalid priority: " + newPriority
                                    ));
                                    failureCount++;
                                    continue;
                                }
                            }
                            break;
                            
                        case "UNASSIGN":
                            ticket.setAssignedTo(null);
                            ticket.setStatus(TicketStatus.OPEN);
                            updated = true;
                            break;
                            
                        default:
                            results.add(Map.of(
                                "ticketId", ticketIdStr,
                                "success", false,
                                "message", "Unknown operation: " + operation
                            ));
                            failureCount++;
                            continue;
                    }
                    
                    if (updated) {
                        ticket.setUpdatedAt(LocalDateTime.now());
                        ticketRepository.save(ticket);
                        
                        results.add(Map.of(
                            "ticketId", ticketIdStr,
                            "success", true,
                            "message", "Updated successfully"
                        ));
                        successCount++;
                    }
                    
                } catch (Exception e) {
                    results.add(Map.of(
                        "ticketId", ticketIdStr,
                        "success", false,
                        "message", "Error: " + e.getMessage()
                    ));
                    failureCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("operation", operation);
            response.put("totalTickets", ticketIds.size());
            response.put("successCount", successCount);
            response.put("failureCount", failureCount);
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Handle OPTIONS requests for CORS preflight
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }
} 