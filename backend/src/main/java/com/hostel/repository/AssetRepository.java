package com.hostel.repository;

import com.hostel.entity.Asset;
import com.hostel.entity.AssetStatus;
import com.hostel.entity.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Asset entity operations
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    
    /**
     * Find asset by asset tag
     */
    Optional<Asset> findByAssetTag(String assetTag);
    
    /**
     * Find assets by building
     */
    List<Asset> findByBuilding(String building);
    
    /**
     * Find assets by type
     */
    List<Asset> findByType(AssetType type);
    
    /**
     * Find assets by status
     */
    List<Asset> findByStatus(AssetStatus status);
    
    /**
     * Count assets by status
     */
    long countByStatus(AssetStatus status);
    
    /**
     * Count assets by type
     */
    long countByType(AssetType type);
    
    /**
     * Count assigned assets
     */
    long countByAssignedToIsNotNull();
    
    /**
     * Find assets with filters
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "(:building IS NULL OR a.building = :building) AND " +
           "(:type IS NULL OR a.type = :type) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Asset> findByFilters(@Param("building") String building, 
                             @Param("type") AssetType type, 
                             @Param("status") AssetStatus status, 
                             Pageable pageable);
    
    /**
     * Search assets by multiple criteria
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.assetTag) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.building) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.location) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Asset> findBySearchCriteria(@Param("search") String search, Pageable pageable);
    
    /**
     * Search assets by multiple criteria (list)
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.assetTag) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.manufacturer) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.building) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.location) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Asset> findBySearchCriteria(@Param("search") String search);
    
    /**
     * Find assets requiring maintenance - simplified approach
     */
    @Query("SELECT a FROM Asset a WHERE a.status = 'MAINTENANCE_REQUIRED'")
    List<Asset> findAssetsRequiringMaintenance();
    
    /**
     * Count assets requiring maintenance - simplified approach
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.status = 'MAINTENANCE_REQUIRED'")
    long countAssetsRequiringMaintenance();
    
    /**
     * Find assets with expired warranty - using parameter approach
     */
    @Query("SELECT a FROM Asset a WHERE a.warrantyExpiry IS NOT NULL AND a.warrantyExpiry < :currentDate")
    List<Asset> findAssetsWithExpiredWarranty(@Param("currentDate") java.time.LocalDate currentDate);
    
    /**
     * Count assets with expired warranty - using parameter approach
     */
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.warrantyExpiry IS NOT NULL AND a.warrantyExpiry < :currentDate")
    long countAssetsWithExpiredWarranty(@Param("currentDate") java.time.LocalDate currentDate);
    
    /**
     * Find assets by assigned user
     */
    @Query("SELECT a FROM Asset a WHERE a.assignedTo.id = :userId")
    List<Asset> findByAssignedToId(@Param("userId") UUID userId);
    
    /**
     * Find assets by room number
     */
    List<Asset> findByRoomNumber(String roomNumber);
    
    /**
     * Find assets by building and room
     */
    List<Asset> findByBuildingAndRoomNumber(String building, String roomNumber);
}
