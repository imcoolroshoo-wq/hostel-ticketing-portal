package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TicketAssignmentService ticketAssignmentService;

    public Page<Ticket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    public Page<Ticket> getTicketsByUser(UUID userId, Pageable pageable) {
        return ticketRepository.findByCreatedById(userId, pageable);
    }

    public Page<Ticket> getTicketsByAssignee(UUID assigneeId, Pageable pageable) {
        return ticketRepository.findByAssignedToId(assigneeId, pageable);
    }

    public Page<Ticket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    public Page<Ticket> getTicketsByPriority(TicketPriority priority, Pageable pageable) {
        return ticketRepository.findByPriority(priority, pageable);
    }

    public Page<Ticket> getTicketsByCategory(TicketCategory category, Pageable pageable) {
        return ticketRepository.findByCategory(category, pageable);
    }

    public Page<Ticket> searchTickets(String searchTerm, Pageable pageable) {
        return ticketRepository.searchTickets(searchTerm, pageable);
    }

    public Optional<Ticket> getTicketById(UUID id) {
        return ticketRepository.findById(id);
    }
    
    public Ticket getTicketByIdDirect(UUID id) {
        return ticketRepository.findById(id).orElse(null);
    }
    
    public List<TicketHistory> getTicketHistory(UUID ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            return ticketOpt.get().getHistory();
        }
        return new ArrayList<>();
    }

    public Ticket createTicket(Ticket ticket, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate ticket number if not provided
        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
            ticket.setTicketNumber(generateTicketNumber());
        }
        
        // Auto-assign hostel block from user if not provided
        if (ticket.getHostelBlock() == null || ticket.getHostelBlock().trim().isEmpty()) {
            if (creator.getHostelBlock() != null) {
                // Use the display name since that's what the Ticket entity expects as String
                ticket.setHostelBlock(creator.getHostelBlock().getDisplayName());
            } else {
                throw new RuntimeException("Hostel block is required. User profile doesn't have hostel block information.");
            }
        }
        
        ticket.setCreatedBy(creator);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Auto-assign based on category and priority
        if (ticket.getAssignedTo() == null) {
            User autoAssignedUser = ticketAssignmentService.autoAssignTicket(ticket);
            if (autoAssignedUser != null) {
                ticket.setAssignedTo(autoAssignedUser);
                ticket.setStatus(TicketStatus.ASSIGNED);
            }
            // If no staff members are available, leave assignedTo as null and status as OPEN
        }
        
        return ticketRepository.save(ticket);
    }
    
    private String generateTicketNumber() {
        // Generate ticket number in format: TKT-YYYY-XXX-XXXXXX
        // Where YYYY is year, XXX is sequential number, XXXXXX is random string
        int year = LocalDateTime.now().getYear();
        long count = ticketRepository.count() + 1;
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6);
        return String.format("TKT-%d-%03d-%s", year, count, randomSuffix);
    }

    public Ticket updateTicket(UUID id, Ticket ticketDetails) {
        return ticketRepository.findById(id)
                .map(existingTicket -> {
                    // Update basic fields
                    if (ticketDetails.getTitle() != null) {
                        existingTicket.setTitle(ticketDetails.getTitle());
                    }
                    if (ticketDetails.getDescription() != null) {
                        existingTicket.setDescription(ticketDetails.getDescription());
                    }
                    if (ticketDetails.getCategory() != null) {
                        existingTicket.setCategory(ticketDetails.getCategory());
                    }
                    if (ticketDetails.getPriority() != null) {
                        existingTicket.setPriority(ticketDetails.getPriority());
                    }
                    if (ticketDetails.getStatus() != null) {
                        existingTicket.setStatus(ticketDetails.getStatus());
                    }
                    if (ticketDetails.getAssignedTo() != null) {
                        existingTicket.setAssignedTo(ticketDetails.getAssignedTo());
                    }
                    if (ticketDetails.getLocationDetails() != null) {
                        existingTicket.setLocationDetails(ticketDetails.getLocationDetails());
                    }
                    if (ticketDetails.getRoomNumber() != null) {
                        existingTicket.setRoomNumber(ticketDetails.getRoomNumber());
                    }
                    if (ticketDetails.getHostelBlock() != null) {
                        existingTicket.setHostelBlock(ticketDetails.getHostelBlock());
                    }
                    
                    existingTicket.setUpdatedAt(LocalDateTime.now());
                    
                    return ticketRepository.save(existingTicket);
                })
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    public void deleteTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    public Ticket changeTicketStatus(UUID id, TicketStatus newStatus, String comment, UUID userId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Create history entry
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(user);
        history.setFieldName("status");
        history.setOldValue(oldStatus.name());
        history.setNewValue(newStatus.name());
        history.setChangedAt(LocalDateTime.now());
        
        // Add comment if provided
        if (comment != null && !comment.trim().isEmpty()) {
            TicketComment ticketComment = new TicketComment();
            ticketComment.setTicket(ticket);
            ticketComment.setUser(user);
            ticketComment.setComment(comment);
            ticketComment.setCreatedAt(LocalDateTime.now());
            ticketComment.setIsInternal(false);
            
            // Add to ticket's comments list
            if (ticket.getComments() == null) {
                ticket.setComments(new ArrayList<>());
            }
            ticket.getComments().add(ticketComment);
        }
        
        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(UUID ticketId, UUID assigneeId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        User oldAssignee = ticket.getAssignedTo();
        ticket.setAssignedTo(assignee);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Create history entry
        if (oldAssignee != null) {
            TicketHistory history = new TicketHistory();
            history.setTicket(ticket);
            history.setChangedBy(assignee);
            history.setFieldName("assignedTo");
            history.setOldValue(oldAssignee.getEmail());
            history.setNewValue(assignee.getEmail());
            history.setChangedAt(LocalDateTime.now());
        }
        
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getUrgentTickets() {
        return ticketRepository.findByPriorityInAndStatusIn(
                List.of(TicketPriority.HIGH, TicketPriority.EMERGENCY),
                List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)
        );
    }

    public List<Ticket> getOverdueTickets() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // Tickets not updated in 7 days
        return ticketRepository.findOverdueTickets(cutoffDate);
    }

    private User autoAssignTicket(Ticket ticket) {
        // Simple auto-assignment logic - assign to first available staff member
        List<User> staffMembers = userRepository.findActiveUsersByRole(UserRole.STAFF);
        if (!staffMembers.isEmpty()) {
            return staffMembers.get(0);
        }
        return null;
    }

    public long getTicketCountByStatus(TicketStatus status) {
        return ticketRepository.countByStatus(status);
    }

    public long getTicketCountByPriority(TicketPriority priority) {
        return ticketRepository.countByPriority(priority);
    }

    public long getTicketCountByCategory(TicketCategory category) {
        return ticketRepository.countByCategory(category);
    }
    
    // Staff-specific methods
    public List<Ticket> getUnassignedTickets() {
        return ticketRepository.findByAssignedToIsNullAndStatus(TicketStatus.OPEN);
    }
    
    public List<Ticket> getTicketsAssignedToStaff(UUID staffId) {
        return ticketRepository.findByAssignedToIdAndStatusIn(staffId, 
            List.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
    }
    
    public List<Ticket> getTicketsForStaffVertical(StaffVertical vertical) {
        List<User> staffMembers = userRepository.findByRoleAndStaffVerticalAndIsActiveTrue(UserRole.STAFF, vertical);
        List<UUID> staffIds = staffMembers.stream().map(User::getId).toList();
        return ticketRepository.findByAssignedToIdIn(staffIds);
    }
    
    public Ticket assignTicketToStaff(UUID ticketId, UUID staffId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User staff = userRepository.findById(staffId)
            .orElseThrow(() -> new RuntimeException("Staff member not found"));
        
        if (staff.getRole() != UserRole.STAFF) {
            throw new RuntimeException("User is not a staff member");
        }
        
        ticket.setAssignedTo(staff);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        return ticketRepository.save(ticket);
    }
    
    public Ticket updateTicketStatus(UUID ticketId, TicketStatus newStatus, UUID updatedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        User updater = userRepository.findById(updatedBy)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate status transition
        if (!ticket.getStatus().canTransitionTo(newStatus)) {
            throw new RuntimeException("Invalid status transition from " + 
                ticket.getStatus().getDisplayName() + " to " + newStatus.getDisplayName());
        }
        
        // Check permissions
        if (updater.getRole() == UserRole.STUDENT) {
            // Students can only close or reopen their own tickets
            if (!ticket.getCreatedBy().getId().equals(updatedBy)) {
                throw new RuntimeException("Students can only update their own tickets");
            }
            if (newStatus != TicketStatus.CLOSED && newStatus != TicketStatus.REOPENED) {
                throw new RuntimeException("Students can only close or reopen tickets");
            }
        } else if (updater.getRole() == UserRole.STAFF) {
            // Staff can only update tickets assigned to them
            if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(updatedBy)) {
                throw new RuntimeException("Staff can only update tickets assigned to them");
            }
        }
        // Admins can update any ticket
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Set resolution time if ticket is being resolved
        if (newStatus == TicketStatus.RESOLVED && ticket.getActualResolutionTime() == null) {
            ticket.setActualResolutionTime(LocalDateTime.now());
        }
        
        return ticketRepository.save(ticket);
    }
    
    public List<Ticket> getTicketsByFilters(TicketStatus status, TicketCategory category, 
                                           TicketPriority priority, String building, UUID assignedTo) {
        // This would typically use Criteria API or custom query methods
        // For now, using simple repository methods
        if (status != null && assignedTo != null) {
            return ticketRepository.findByStatusAndAssignedToId(status, assignedTo);
        } else if (status != null) {
            return ticketRepository.findByStatus(status);
        } else if (assignedTo != null) {
            return ticketRepository.findByAssignedToId(assignedTo);
        } else {
            return ticketRepository.findAll();
        }
    }

    /**
     * Unassign a ticket - Admin only operation
     * Changes ticket status from ASSIGNED back to OPEN and removes assignedTo
     */
    public Ticket unassignTicket(UUID ticketId, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can unassign tickets");
        }
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        if (ticket.getAssignedTo() == null) {
            throw new RuntimeException("Ticket is not currently assigned");
        }
        
        // Store previous assignment for history
        User previouslyAssignedTo = ticket.getAssignedTo();
        
        // Unassign the ticket
        ticket.setAssignedTo(null);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        // Create history entry
        TicketHistory historyEntry = new TicketHistory();
        historyEntry.setTicket(ticket);
        historyEntry.setAction("UNASSIGNED");
        historyEntry.setComment("Ticket unassigned by admin from " + previouslyAssignedTo.getFirstName() + " " + previouslyAssignedTo.getLastName());
        historyEntry.setCreatedBy(admin);
        historyEntry.setCreatedAt(LocalDateTime.now());
        
        if (ticket.getHistory() == null) {
            ticket.setHistory(new ArrayList<>());
        }
        ticket.getHistory().add(historyEntry);
        
        return ticketRepository.save(ticket);
    }
} 