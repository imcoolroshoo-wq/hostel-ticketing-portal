package com.hostel.controller;

import com.hostel.dto.DTOMapper;
import com.hostel.dto.TicketDTO;
import com.hostel.entity.Ticket;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.TicketPriority;
import com.hostel.entity.TicketStatus;
import com.hostel.entity.User;
import com.hostel.entity.UserRole;
import com.hostel.repository.TicketRepository;
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

    // Get all tickets with pagination - Admin only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Ticket> tickets = ticketService.getAllTickets(pageable);
        
        List<TicketDTO> ticketDTOs = tickets.getContent().stream()
                .map(DTOMapper::toTicketDTO)
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("tickets", ticketDTOs);
        response.put("currentPage", tickets.getNumber());
        response.put("totalItems", tickets.getTotalElements());
        response.put("totalPages", tickets.getTotalPages());
        response.put("size", tickets.getSize());
        
        return ResponseEntity.ok(response);
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
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
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
            
            newTicket.setHostelBlock((String) ticketData.get("hostelBlock"));
            newTicket.setRoomNumber((String) ticketData.get("roomNumber"));
            newTicket.setLocationDetails((String) ticketData.get("locationDetails"));
            
            // Set required fields
            newTicket.setTicketNumber("TKT-" + System.currentTimeMillis());
            newTicket.setStatus(TicketStatus.OPEN);
            newTicket.setCreatedAt(LocalDateTime.now());
            newTicket.setUpdatedAt(LocalDateTime.now());
            
            // Set creator by fetching from database
            User creator = userService.getUserByIdDirect(creatorId);
            if (creator == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not found");
                errorResponse.put("message", "Creator user with ID " + creatorId + " not found");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            newTicket.setCreatedBy(creator);
            
            // Save directly without any service layer
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
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
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN') or hasRole('STUDENT')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('STUDENT')")
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

    // Handle OPTIONS requests for CORS preflight
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }
} 