package com.hostel.repository;

import com.hostel.entity.HostelBlock;
import com.hostel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelBlockRepository extends JpaRepository<HostelBlock, UUID> {
    
    /**
     * Find hostel block by block name
     */
    Optional<HostelBlock> findByBlockName(String blockName);
    
    /**
     * Find hostel block by block code
     */
    Optional<HostelBlock> findByBlockCode(String blockCode);
    
    /**
     * Find all active hostel blocks
     */
    List<HostelBlock> findByIsActiveTrueOrderByBlockName();
    
    /**
     * Find female hostel blocks
     */
    List<HostelBlock> findByIsFemaleBlockTrueAndIsActiveTrueOrderByBlockName();
    
    /**
     * Find male hostel blocks
     */
    List<HostelBlock> findByIsFemaleBlockFalseAndIsActiveTrueOrderByBlockName();
    
    /**
     * Find hostel blocks with disabled access
     */
    List<HostelBlock> findByHasDisabledAccessTrueAndIsActiveTrueOrderByBlockName();
    
    /**
     * Find hostel block by warden
     */
    Optional<HostelBlock> findByWardenAndIsActiveTrue(User warden);
    
    /**
     * Find hostel blocks without warden
     */
    List<HostelBlock> findByWardenIsNullAndIsActiveTrueOrderByBlockName();
    
    /**
     * Get total capacity across all active blocks
     */
    @Query("SELECT SUM(hb.totalRooms) FROM HostelBlock hb WHERE hb.isActive = true")
    Integer getTotalCapacity();
    
    /**
     * Get occupancy statistics
     */
    @Query("SELECT hb.blockName, hb.totalRooms, COUNT(u.id) as occupiedRooms " +
           "FROM HostelBlock hb LEFT JOIN User u ON u.hostelBlock = hb.blockName AND u.isActive = true " +
           "WHERE hb.isActive = true " +
           "GROUP BY hb.id, hb.blockName, hb.totalRooms " +
           "ORDER BY hb.blockName")
    List<Object[]> getOccupancyStatistics();
    
    /**
     * Find blocks with available rooms
     */
    @Query("SELECT hb FROM HostelBlock hb WHERE hb.isActive = true AND " +
           "hb.totalRooms > (SELECT COUNT(u) FROM User u WHERE u.hostelBlock = hb.blockName AND u.isActive = true)")
    List<HostelBlock> findBlocksWithAvailableRooms();
    
    /**
     * Check if block name exists
     */
    boolean existsByBlockNameAndIsActiveTrue(String blockName);
    
    /**
     * Check if block code exists
     */
    boolean existsByBlockCodeAndIsActiveTrue(String blockCode);
}
