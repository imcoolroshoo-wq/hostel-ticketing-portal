package com.hostel.repository;

import com.hostel.entity.User;
import com.hostel.entity.UserRole;
import com.hostel.entity.StaffVertical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    // Staff vertical related queries
    List<User> findByRoleAndStaffVerticalAndIsActiveTrue(UserRole role, StaffVertical staffVertical);
    
    List<User> findByRoleAndStaffVerticalInAndIsActiveTrue(UserRole role, List<StaffVertical> staffVerticals);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.staffVertical = :vertical AND u.isActive = true")
    List<User> findActiveStaffByVertical(@Param("role") UserRole role, @Param("vertical") StaffVertical vertical);
    
    @Query("SELECT u FROM User u WHERE u.role = 'STAFF' AND u.isActive = true")
    List<User> findAllActiveStaff();
    
    @Query("SELECT u FROM User u WHERE u.staffId = :staffId AND u.isActive = true")
    Optional<User> findByStaffIdAndIsActiveTrue(@Param("staffId") String staffId);

    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    // Find supervisors by staff vertical (assuming supervisors have specific role or designation)
    @Query("SELECT u FROM User u WHERE u.role = 'STAFF' " +
           "AND u.staffVertical = :vertical " +
           "AND u.isActive = true " +
           "ORDER BY u.createdAt ASC")
    List<User> findSupervisorsByVertical(@Param("vertical") String vertical);
    
    @Modifying
    @Query("UPDATE User u SET u.isActive = :isActive, u.updatedAt = :updatedAt WHERE u.id = :userId")
    int updateUserStatus(@Param("userId") UUID userId, @Param("isActive") Boolean isActive, @Param("updatedAt") java.time.LocalDateTime updatedAt);
} 