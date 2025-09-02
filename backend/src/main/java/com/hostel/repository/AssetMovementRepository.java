package com.hostel.repository;

import com.hostel.entity.AssetMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AssetMovement entity operations
 */
@Repository
public interface AssetMovementRepository extends JpaRepository<AssetMovement, UUID> {
    
    /**
     * Find movements by asset ID ordered by moved date descending
     */
    List<AssetMovement> findByAssetIdOrderByMovedAtDesc(UUID assetId);
    
    /**
     * Find movements by asset ID
     */
    List<AssetMovement> findByAssetId(UUID assetId);
    
    /**
     * Find movements by movement type
     */
    List<AssetMovement> findByMovementType(String movementType);
    
    /**
     * Find movements within date range
     */
    @Query("SELECT am FROM AssetMovement am WHERE am.movedAt BETWEEN :startDate AND :endDate ORDER BY am.movedAt DESC")
    List<AssetMovement> findByMovedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find recent movements for asset
     */
    @Query("SELECT am FROM AssetMovement am WHERE am.asset.id = :assetId ORDER BY am.movedAt DESC")
    List<AssetMovement> findRecentMovementsByAssetId(@Param("assetId") UUID assetId);
    
    /**
     * Find movements by location
     */
    @Query("SELECT am FROM AssetMovement am WHERE am.fromLocation = :location OR am.toLocation = :location ORDER BY am.movedAt DESC")
    List<AssetMovement> findByLocation(@Param("location") String location);
}
