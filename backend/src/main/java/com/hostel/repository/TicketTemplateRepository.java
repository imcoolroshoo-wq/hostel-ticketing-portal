package com.hostel.repository;

import com.hostel.entity.TicketTemplate;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.HostelName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TicketTemplate entity
 * Supports template management operations as per PDD Section 4.1.4
 */
@Repository
public interface TicketTemplateRepository extends JpaRepository<TicketTemplate, UUID> {
    
    /**
     * Find all active templates
     */
    List<TicketTemplate> findByIsActiveTrueOrderByUsageCountDesc();
    
    /**
     * Find templates by category
     */
    List<TicketTemplate> findByCategoryAndIsActiveTrueOrderByUsageCountDesc(TicketCategory category);
    
    /**
     * Find templates applicable to a specific hostel block
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND " +
           "(t.hostelBlock IS NULL OR t.hostelBlock = :hostelBlock) " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findByHostelBlockOrNullOrderByUsageCountDesc(@Param("hostelBlock") HostelName hostelBlock);
    
    /**
     * Find templates by category and hostel block
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND t.category = :category AND " +
           "(t.hostelBlock IS NULL OR t.hostelBlock = :hostelBlock) " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findByCategoryAndHostelBlockOrNullOrderByUsageCountDesc(
            @Param("category") TicketCategory category, 
            @Param("hostelBlock") HostelName hostelBlock);
    
    /**
     * Find template by name
     */
    Optional<TicketTemplate> findByNameAndIsActiveTrue(String name);
    
    /**
     * Find most used templates
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findMostUsedTemplates();
    
    /**
     * Find templates by name containing (case insensitive)
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("searchTerm") String searchTerm);
    
    /**
     * Find templates by description containing (case insensitive)
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND " +
           "(LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.titleTemplate) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> searchTemplates(@Param("searchTerm") String searchTerm);
    
    /**
     * Find recently used templates
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND t.lastUsedAt IS NOT NULL " +
           "ORDER BY t.lastUsedAt DESC")
    List<TicketTemplate> findRecentlyUsedTemplates();
    
    /**
     * Count templates by category
     */
    long countByCategoryAndIsActiveTrue(TicketCategory category);
    
    /**
     * Find templates created by a specific user
     */
    List<TicketTemplate> findByCreatedByIdAndIsActiveTrueOrderByCreatedAtDesc(UUID createdById);
    
    /**
     * Find templates that require photos
     */
    List<TicketTemplate> findByPhotoRequiredTrueAndIsActiveTrueOrderByUsageCountDesc();
    
    /**
     * Find templates by category group
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND t.category IN :categories " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findByCategoriesInAndIsActiveTrueOrderByUsageCountDesc(@Param("categories") List<TicketCategory> categories);
    
    /**
     * Find unused templates (never used)
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND " +
           "(t.usageCount = 0 OR t.lastUsedAt IS NULL) " +
           "ORDER BY t.createdAt DESC")
    List<TicketTemplate> findUnusedTemplates();
    
    /**
     * Find templates with specific requirements
     */
    @Query("SELECT t FROM TicketTemplate t WHERE t.isActive = true AND " +
           "(:requiresRoom = false OR t.roomNumberRequired = true) AND " +
           "(:requiresLocation = false OR t.locationDetailsRequired = true) AND " +
           "(:requiresPhoto = false OR t.photoRequired = true) " +
           "ORDER BY t.usageCount DESC")
    List<TicketTemplate> findByRequirements(
            @Param("requiresRoom") boolean requiresRoom,
            @Param("requiresLocation") boolean requiresLocation,
            @Param("requiresPhoto") boolean requiresPhoto);
}
