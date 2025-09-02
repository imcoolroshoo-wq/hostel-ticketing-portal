package com.hostel.repository;

import com.hostel.entity.Notification;
import com.hostel.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Notification entity operations
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Find notifications by user ID
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find notifications by user ID with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find unread notifications by user ID
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find read notifications by user ID
     */
    List<Notification> findByUserIdAndIsReadTrueOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find notification by ID and user ID
     */
    Notification findByIdAndUserId(UUID notificationId, UUID userId);
    
    /**
     * Find notifications by type
     */
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);
    
    /**
     * Find notifications by user and type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type);
    
    /**
     * Find notifications by related ticket
     */
    List<Notification> findByRelatedTicketIdOrderByCreatedAtDesc(UUID ticketId);
    
    /**
     * Find notifications by user and related ticket
     */
    List<Notification> findByUserIdAndRelatedTicketIdOrderByCreatedAtDesc(UUID userId, UUID ticketId);
    
    /**
     * Count unread notifications by user
     */
    long countByUserIdAndIsReadFalse(UUID userId);
    
    /**
     * Count notifications by user
     */
    long countByUserId(UUID userId);
    
    /**
     * Count notifications by type
     */
    long countByType(NotificationType type);
    
    /**
     * Find recent notifications (last N days)
     */
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :cutoffDate ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Find notifications created between dates
     */
    List<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find notifications by user and date range
     */
    List<Notification> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Delete notifications older than specified date
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    /**
     * Find notifications that need retry (for failed external notifications)
     */
    List<Notification> findByTypeInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(List<NotificationType> types, LocalDateTime recentDate);
    
    /**
     * Find high priority unread notifications
     */
    List<Notification> findByUserIdAndIsReadFalseAndTypeInOrderByCreatedAtDesc(UUID userId, List<NotificationType> types);
    
    /**
     * Find notifications by multiple types
     */
    List<Notification> findByUserIdAndTypeInOrderByCreatedAtDesc(UUID userId, List<NotificationType> types);
    
    /**
     * Search notifications by content - using method name approach
     */
    List<Notification> findByUserIdAndTitleContainingIgnoreCaseOrMessageContainingIgnoreCaseOrderByCreatedAtDesc(UUID userId, String titleSearch, String messageSearch);
    
    // Note: Complex statistics queries temporarily removed to resolve startup issues
    // These can be re-implemented using native queries or service-level aggregation if needed
}
