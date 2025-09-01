package com.hostel.repository;

import com.hostel.entity.Ticket;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.TicketPriority;
import com.hostel.entity.TicketStatus;
import com.hostel.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    
    // Find tickets by user (creator)
    Page<Ticket> findByCreatedById(UUID userId, Pageable pageable);
    
    // Find tickets by user created after a certain date (for duplicate detection)
    List<Ticket> findByCreatedByIdAndCreatedAtAfter(UUID userId, LocalDateTime after);
    
    // Find tickets by assignee
    Page<Ticket> findByAssignedToId(UUID assigneeId, Pageable pageable);
    
    // Find tickets by status
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    // Find tickets by priority
    Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);
    
    // Find tickets by category
    Page<Ticket> findByCategory(TicketCategory category, Pageable pageable);
    
    // Find tickets by hostel block
    Page<Ticket> findByHostelBlock(String hostelBlock, Pageable pageable);
    
    // Find tickets by room number
    Page<Ticket> findByRoomNumber(String roomNumber, Pageable pageable);
    
    // Find tickets by multiple priorities and statuses
    @Query("SELECT t FROM Ticket t WHERE t.priority IN :priorities AND t.status IN :statuses")
    List<Ticket> findByPriorityInAndStatusIn(@Param("priorities") List<TicketPriority> priorities, @Param("statuses") List<TicketStatus> statuses);
    
    // Find tickets by status and priority
    Page<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority, Pageable pageable);
    
    // Find tickets by category and status
    Page<Ticket> findByCategoryAndStatus(TicketCategory category, TicketStatus status, Pageable pageable);
    
    // Find tickets created between dates
    Page<Ticket> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Find tickets updated between dates
    Page<Ticket> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Search tickets by title or description
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Ticket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find overdue tickets (tickets that haven't been updated in a while)
    @Query("SELECT t FROM Ticket t WHERE " +
           "t.status IN (com.hostel.entity.TicketStatus.OPEN, com.hostel.entity.TicketStatus.IN_PROGRESS) AND " +
           "t.updatedAt < :cutoffDate")
    List<Ticket> findOverdueTickets(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count tickets by status
    long countByStatus(TicketStatus status);
    
    // Count tickets by priority
    long countByPriority(TicketPriority priority);
    
    // Count tickets by category
    long countByCategory(TicketCategory category);
    
    // Count tickets by user
    long countByCreatedById(UUID userId);
    
    // Count tickets by assignee
    long countByAssignedToId(UUID assigneeId);
    
    // Find tickets that need escalation
    @Query("SELECT t FROM Ticket t WHERE " +
           "t.status = com.hostel.entity.TicketStatus.OPEN AND " +
           "t.priority IN (com.hostel.entity.TicketPriority.HIGH, com.hostel.entity.TicketPriority.EMERGENCY) AND " +
           "t.createdAt < :escalationDate")
    List<Ticket> findTicketsNeedingEscalation(@Param("escalationDate") LocalDateTime escalationDate);
    
    // Find tickets by location
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.hostelBlock) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(t.roomNumber) LIKE LOWER(CONCAT('%', :location, '%')) OR " +
           "LOWER(t.locationDetails) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Ticket> findByLocation(@Param("location") String location, Pageable pageable);
    
    // Staff-specific queries
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo IS NULL AND t.status = :status")
    List<Ticket> findByAssignedToIsNullAndStatus(@Param("status") TicketStatus status);
    
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo.id = :staffId AND t.status IN :statuses")
    List<Ticket> findByAssignedToIdAndStatusIn(@Param("staffId") UUID staffId, @Param("statuses") List<TicketStatus> statuses);
    
    List<Ticket> findByAssignedToIdIn(List<UUID> staffIds);
    
    List<Ticket> findByStatusAndAssignedToId(TicketStatus status, UUID assignedToId);
    
    List<Ticket> findByStatus(TicketStatus status);
    
    List<Ticket> findByAssignedToId(UUID assignedToId);
    
    // New methods for assignment service
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo = :assignedTo")
    List<Ticket> findByAssignedTo(@Param("assignedTo") User assignedTo);
    
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo = :assignedTo AND t.status IN :statuses")
    List<Ticket> findByAssignedToAndStatusIn(@Param("assignedTo") User assignedTo, @Param("statuses") List<TicketStatus> statuses);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo = :assignedTo AND t.status IN :statuses")
    int countByAssignedToAndStatusIn(@Param("assignedTo") User assignedTo, @Param("statuses") List<TicketStatus> statuses);
    
    // Find tickets by hostel block and category
    @Query("SELECT t FROM Ticket t WHERE t.hostelBlock = :hostelBlock AND " +
           "(t.category = :category OR t.customCategory = :category)")
    List<Ticket> findByHostelBlockAndCategory(@Param("hostelBlock") String hostelBlock, 
                                             @Param("category") String category);
    
    // SLA Management queries  
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "AND t.estimatedResolutionTime IS NOT NULL " +
           "AND :now >= (t.createdAt + (t.estimatedResolutionTime - t.createdAt) * 0.75)")
    List<Ticket> findTicketsApproachingSLABreach(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Ticket t WHERE t.status NOT IN ('RESOLVED', 'CLOSED', 'CANCELLED') " +
           "AND t.slaBreachTime IS NOT NULL " +
           "AND t.slaBreachTime <= :now")
    List<Ticket> findTicketsWithSLABreach(@Param("now") LocalDateTime now);
    
    // Escalation queries
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority " +
           "AND t.status = :status " +
           "AND t.createdAt <= :threshold")
    List<Ticket> findTicketsForTimeBasedEscalation(
        @Param("priority") TicketPriority priority,
        @Param("status") TicketStatus status,
        @Param("threshold") LocalDateTime threshold
    );
    
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority " +
           "AND t.status IN :statuses " +
           "AND t.updatedAt <= :threshold")
    List<Ticket> findTicketsForProgressEscalation(
        @Param("priority") TicketPriority priority,
        @Param("statuses") List<TicketStatus> statuses,
        @Param("threshold") LocalDateTime threshold
    );
    
    // Feedback queries
    @Query("SELECT t FROM Ticket t WHERE t.satisfactionRating IS NOT NULL")
    List<Ticket> findTicketsWithFeedback();
} 