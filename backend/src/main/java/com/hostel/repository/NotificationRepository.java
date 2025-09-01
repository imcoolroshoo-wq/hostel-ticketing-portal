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
    @Query("SELECT n FROM Notification n WHERE n.type IN ('EMAIL', 'SMS') AND n.createdAt >= :recentDate ORDER BY n.createdAt ASC")
    List<Notification> findNotificationsForRetry(@Param("recentDate") LocalDateTime recentDate);
    
    /**
     * Find high priority unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND " +
           "n.type IN ('EMERGENCY', 'SLA_BREACH', 'ESCALATION') ORDER BY n.createdAt DESC")
    List<Notification> findHighPriorityUnreadNotifications(@Param("userId") UUID userId);
    
    /**
     * Find notifications by multiple types
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.type IN :types ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndTypeIn(@Param("userId") UUID userId, @Param("types") List<NotificationType> types);
    
    /**
     * Search notifications by content
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> searchNotifications(@Param("userId") UUID userId, @Param("search") String search);
    
    /**
     * Get notification statistics for user
     */
    @Query("SELECT n.type as type, COUNT(n) as count FROM Notification n WHERE n.userId = :userId GROUP BY n.type")
    List<Object[]> getNotificationStatsByUser(@Param("userId") UUID userId);
    
    /**
     * Get notification statistics by date range
     */
    @Query("SELECT DATE(n.createdAt) as date, COUNT(n) as count FROM Notification n " +
           "WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getNotificationStatsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
