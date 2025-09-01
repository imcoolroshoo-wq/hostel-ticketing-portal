package com.hostel.repository;

import com.hostel.entity.CategoryStaffMapping;
import com.hostel.entity.HostelName;
import com.hostel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryStaffMappingRepository extends JpaRepository<CategoryStaffMapping, UUID> {

    /**
     * Find all staff members mapped to a specific category, ordered by priority
     */
    @Query("SELECT csm FROM CategoryStaffMapping csm WHERE csm.category = :category AND csm.isActive = true ORDER BY csm.priorityLevel ASC")
    List<CategoryStaffMapping> findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc(@Param("category") String category);
    
    /**
     * Find staff mappings by hostel block and category
     */
    @Query("SELECT csm FROM CategoryStaffMapping csm WHERE csm.hostelBlock = :hostelBlock AND csm.category = :category AND csm.isActive = true ORDER BY csm.priorityLevel ASC")
    List<CategoryStaffMapping> findByHostelBlockAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(@Param("hostelBlock") HostelName hostelBlock, @Param("category") String category);
    
    /**
     * Find staff mappings for category across all hostels (hostel_block is NULL)
     */
    @Query("SELECT csm FROM CategoryStaffMapping csm WHERE csm.hostelBlock IS NULL AND csm.category = :category AND csm.isActive = true ORDER BY csm.priorityLevel ASC")
    List<CategoryStaffMapping> findByHostelBlockIsNullAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(@Param("category") String category);

    /**
     * Find all categories mapped to a specific staff member
     */
    @Query("SELECT csm FROM CategoryStaffMapping csm WHERE csm.staff.id = :staffId AND csm.isActive = true")
    List<CategoryStaffMapping> findByStaffIdAndIsActiveTrue(@Param("staffId") UUID staffId);

    /**
     * Find all active mappings for a staff member
     */
    List<CategoryStaffMapping> findByStaffAndIsActiveTrue(User staff);

    /**
     * Find mapping by staff and category
     */
    CategoryStaffMapping findByStaffAndCategoryAndIsActiveTrue(User staff, String category);

    /**
     * Find all active mappings
     */
    List<CategoryStaffMapping> findByIsActiveTrueOrderByPriorityLevelAsc();

    /**
     * Check if a staff member is mapped to a category
     */
    @Query("SELECT COUNT(csm) > 0 FROM CategoryStaffMapping csm WHERE csm.staff.id = :staffId AND csm.category = :category AND csm.isActive = true")
    boolean existsByStaffIdAndCategoryAndIsActiveTrue(@Param("staffId") UUID staffId, @Param("category") String category);

    /**
     * Get all distinct categories that have staff mappings
     */
    @Query("SELECT DISTINCT csm.category FROM CategoryStaffMapping csm WHERE csm.isActive = true ORDER BY csm.category")
    List<String> findDistinctCategoriesByIsActiveTrue();

    /**
     * Get staff members for multiple categories
     */
    @Query("SELECT csm FROM CategoryStaffMapping csm WHERE csm.category IN :categories AND csm.isActive = true ORDER BY csm.priorityLevel ASC")
    List<CategoryStaffMapping> findByCategoryInAndIsActiveTrueOrderByPriorityLevelAsc(@Param("categories") List<String> categories);
}
