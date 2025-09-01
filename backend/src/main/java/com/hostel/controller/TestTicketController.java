package com.hostel.controller;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestTicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @PostMapping("/ticket")
    public ResponseEntity<?> createTestTicket(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String priority,
            @RequestParam String hostelBlock,
            @RequestParam String roomNumber,
            @RequestParam String locationDetails,
            @RequestParam UUID creatorId) {
        try {
            // Create a new ticket
            Ticket newTicket = new Ticket();
            newTicket.setTitle(title);
            newTicket.setDescription(description);
            
            // Convert string to enum
            try {
                newTicket.setCategory(TicketCategory.valueOf(category));
            } catch (IllegalArgumentException e) {
                newTicket.setCategory(TicketCategory.GENERAL);
            }
            
            try {
                newTicket.setPriority(TicketPriority.valueOf(priority));
            } catch (IllegalArgumentException e) {
                newTicket.setPriority(TicketPriority.MEDIUM);
            }
            
            newTicket.setHostelBlock(hostelBlock);
            newTicket.setRoomNumber(roomNumber);
            newTicket.setLocationDetails(locationDetails);
            
            // Set required fields
            newTicket.setTicketNumber("TKT-" + System.currentTimeMillis());
            newTicket.setStatus(TicketStatus.OPEN);
            newTicket.setCreatedAt(LocalDateTime.now());
            newTicket.setUpdatedAt(LocalDateTime.now());
            
            // Set creator
            User creator = new User();
            creator.setId(creatorId);
            newTicket.setCreatedBy(creator);
            
            // Save ticket
            Ticket savedTicket = ticketRepository.save(newTicket);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTicket.getId());
            response.put("ticketNumber", savedTicket.getTicketNumber());
            response.put("title", savedTicket.getTitle());
            response.put("status", savedTicket.getStatus());
            response.put("category", savedTicket.getCategory());
            response.put("priority", savedTicket.getPriority());
            response.put("message", "Ticket created successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error creating test ticket: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create ticket");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
