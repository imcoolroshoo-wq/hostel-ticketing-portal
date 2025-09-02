package com.hostel.repository;

import com.hostel.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for MaintenanceSchedule entity operations
 */
@Repository
public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, UUID> {
    
    /**
     * Find maintenance schedules by asset ID ordered by scheduled date descending
     */
    List<MaintenanceSchedule> findByAssetIdOrderByNextDueDateDesc(UUID assetId);
    
    /**
     * Find maintenance schedules by asset ID
     */
    List<MaintenanceSchedule> findByAssetId(UUID assetId);
    
    /**
     * Find overdue maintenance schedules
     */
    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.nextDueDate < :now AND ms.status != 'COMPLETED'")
    List<MaintenanceSchedule> findOverdueMaintenanceSchedules(@Param("now") LocalDateTime now);
    
    /**
     * Find maintenance schedules due today
     */
    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE DATE(ms.nextDueDate) = :today AND ms.status != 'COMPLETED'")
    List<MaintenanceSchedule> findMaintenanceSchedulesDueToday(@Param("today") java.time.LocalDate today);
    
    /**
     * Find maintenance schedules due within date range
     */
    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.nextDueDate BETWEEN :startDate AND :endDate AND ms.status != 'COMPLETED'")
    List<MaintenanceSchedule> findMaintenanceSchedulesDueBetween(@Param("startDate") LocalDateTime startDate, 
                                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find completed maintenance schedules
     */
    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.status = 'COMPLETED' ORDER BY ms.lastPerformed DESC")
    List<MaintenanceSchedule> findCompletedMaintenanceSchedules();
    
    /**
     * Find pending maintenance schedules
     */
    @Query("SELECT ms FROM MaintenanceSchedule ms WHERE ms.status != 'COMPLETED' ORDER BY ms.nextDueDate ASC")
    List<MaintenanceSchedule> findPendingMaintenanceSchedules();
    
    /**
     * Find maintenance schedules by type
     */
    List<MaintenanceSchedule> findByMaintenanceType(String maintenanceType);
    
    /**
     * Count overdue maintenance schedules
     */
    @Query("SELECT COUNT(ms) FROM MaintenanceSchedule ms WHERE ms.nextDueDate < CURRENT_TIMESTAMP AND ms.status != 'COMPLETED'")
    long countOverdueMaintenanceSchedules();
    
    /**
     * Count pending maintenance schedules
     */
    @Query("SELECT COUNT(ms) FROM MaintenanceSchedule ms WHERE ms.status != 'COMPLETED'")
    long countPendingMaintenanceSchedules();
}
