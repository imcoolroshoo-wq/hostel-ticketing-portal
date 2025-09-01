package com.hostel.repository;

import com.hostel.entity.Ticket;
import com.hostel.entity.TicketEscalation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketEscalationRepository extends JpaRepository<TicketEscalation, UUID> {
    
    // Find escalations by ticket
    List<TicketEscalation> findByTicketOrderByEscalatedAtDesc(Ticket ticket);
    
    // Find recent escalations by ticket and level
    List<TicketEscalation> findByTicketAndEscalationLevelAndEscalatedAtAfter(
        Ticket ticket, 
        @Param("level") Integer escalationLevel, 
        @Param("after") LocalDateTime after
    );
    
    // Find escalations by escalated to user
    List<TicketEscalation> findByEscalatedToIdOrderByEscalatedAtDesc(UUID userId);
    
    // Find unresolved escalations
    List<TicketEscalation> findByResolvedAtIsNullOrderByEscalatedAtDesc();
    
    // Count escalations by ticket
    int countByTicket(Ticket ticket);
    
    // Find escalations by date range
    List<TicketEscalation> findByEscalatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
