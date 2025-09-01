package com.hostel.controller;

import com.hostel.entity.*;
import com.hostel.service.EscalationService;
import com.hostel.service.TicketService;
import com.hostel.service.UserService;
import com.hostel.repository.TicketEscalationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Escalation Controller for managing ticket escalations
 * Implements escalation management as per Product Design Document Section 5.3
 */
@RestController
@RequestMapping("/api/escalations")
@CrossOrigin(origins = {"http://localhost:3000", "https://hostel-ticketing-frontend.onrender.com"})
public class EscalationController {

    @Autowired
    private EscalationService escalationService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @Autowired
    private TicketEscalationRepository escalationRepository;

    /**
     * Get all escalations with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<?> getAllEscalations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID ticketId,
            @RequestParam(required = false) UUID userId) {
        try {
            // In a real implementation, this would use proper pagination
            // For now, return all escalations with basic filtering
            List<TicketEscalation> escalations;
            
            if (ticketId != null) {
                escalations = escalationRepository.findByTicketId(ticketId);
            } else if (userId != null) {
                escalations = escalationRepository.findByEscalatedToId(userId);
            } else {
                escalations = escalationRepository.findAll();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("escalations", escalations);
            response.put("totalElements", escalations.size());
            response.put("currentPage", page);
            response.put("totalPages", (escalations.size() + size - 1) / size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching escalations: " + e.getMessage());
        }
    }

    /**
     * Get escalation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEscalationById(@PathVariable UUID id) {
        try {
            TicketEscalation escalation = escalationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Escalation not found"));
            return ResponseEntity.ok(escalation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching escalation: " + e.getMessage());
        }
    }

    /**
     * Manual escalation of a ticket
     */
    @PostMapping("/manual")
    public ResponseEntity<?> escalateTicketManually(@RequestBody ManualEscalationRequest request) {
        try {
            Ticket ticket = ticketService.getTicketById(request.getTicketId());
            if (ticket == null) {
                return ResponseEntity.badRequest().body("Ticket not found");
            }

            User escalatedTo = userService.getUserById(request.getEscalatedToId())
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            EscalationService.EscalationLevel level = EscalationService.EscalationLevel.valueOf(request.getLevel());
            
            escalationService.escalateTicket(ticket, request.getReason(), level);

            return ResponseEntity.ok("Ticket escalated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error escalating ticket: " + e.getMessage());
        }
    }

    /**
     * Process automatic escalations (typically called by scheduler)
     */
    @PostMapping("/process-automatic")
    public ResponseEntity<?> processAutomaticEscalations() {
        try {
            escalationService.processAutomaticEscalations();
            return ResponseEntity.ok("Automatic escalations processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing escalations: " + e.getMessage());
        }
    }

    /**
     * Resolve an escalation
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveEscalation(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        try {
            TicketEscalation escalation = escalationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Escalation not found"));

            escalation.resolve();
            escalationRepository.save(escalation);

            return ResponseEntity.ok("Escalation resolved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resolving escalation: " + e.getMessage());
        }
    }

    /**
     * Get escalations for a specific ticket
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<?> getEscalationsForTicket(@PathVariable UUID ticketId) {
        try {
            List<TicketEscalation> escalations = escalationRepository.findByTicketIdOrderByEscalatedAtDesc(ticketId);
            return ResponseEntity.ok(escalations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching ticket escalations: " + e.getMessage());
        }
    }

    /**
     * Get escalations assigned to a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEscalationsForUser(@PathVariable UUID userId) {
        try {
            List<TicketEscalation> escalations = escalationRepository.findByEscalatedToIdAndResolvedAtIsNull(userId);
            return ResponseEntity.ok(escalations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching user escalations: " + e.getMessage());
        }
    }

    /**
     * Get escalation statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getEscalationStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Total escalations
            long totalEscalations = escalationRepository.count();
            stats.put("totalEscalations", totalEscalations);
            
            // Active escalations
            long activeEscalations = escalationRepository.countByResolvedAtIsNull();
            stats.put("activeEscalations", activeEscalations);
            
            // Escalations by level
            Map<String, Long> escalationsByLevel = new HashMap<>();
            for (EscalationService.EscalationLevel level : EscalationService.EscalationLevel.values()) {
                long count = escalationRepository.countByEscalationLevel(level.getLevel());
                escalationsByLevel.put(level.name(), count);
            }
            stats.put("escalationsByLevel", escalationsByLevel);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching escalation statistics: " + e.getMessage());
        }
    }

    /**
     * Get overdue escalations
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueEscalations() {
        try {
            List<TicketEscalation> allActiveEscalations = escalationRepository.findByResolvedAtIsNull();
            List<TicketEscalation> overdueEscalations = allActiveEscalations.stream()
                    .filter(TicketEscalation::isOverdue)
                    .toList();
            return ResponseEntity.ok(overdueEscalations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching overdue escalations: " + e.getMessage());
        }
    }

    // Inner class for manual escalation request
    public static class ManualEscalationRequest {
        private UUID ticketId;
        private UUID escalatedToId;
        private String reason;
        private String level;

        // Getters and setters
        public UUID getTicketId() { return ticketId; }
        public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
        public UUID getEscalatedToId() { return escalatedToId; }
        public void setEscalatedToId(UUID escalatedToId) { this.escalatedToId = escalatedToId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }
}
