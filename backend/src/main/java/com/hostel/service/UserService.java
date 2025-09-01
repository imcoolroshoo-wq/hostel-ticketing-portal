package com.hostel.service;

import com.hostel.entity.User;
import com.hostel.entity.UserRole;
import com.hostel.entity.StaffVertical;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findActiveUsersByRole(role);
    }

    public User createUser(User user) {
        // Encode password before saving
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        
        // Set default values
        if (user.getRole() == null) {
            user.setRole(UserRole.STUDENT);
        }
        
        user.setIsActive(true);
        return userRepository.save(user);
    }

    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstName(userDetails.getFirstName());
                    existingUser.setLastName(userDetails.getLastName());
                    existingUser.setEmail(userDetails.getEmail());
                    existingUser.setPhone(userDetails.getPhone());
                    existingUser.setRoomNumber(userDetails.getRoomNumber());
                    existingUser.setHostelBlock(userDetails.getHostelBlock());
                    existingUser.setRole(userDetails.getRole());
                    
                    // Only update password if provided
                    if (userDetails.getPasswordHash() != null && !userDetails.getPasswordHash().isEmpty()) {
                        existingUser.setPasswordHash(passwordEncoder.encode(userDetails.getPasswordHash()));
                    }
                    
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPasswordHash()) && user.getIsActive();
        }
        return false;
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getStaffMembers() {
        return userRepository.findActiveUsersByRole(UserRole.STAFF);
    }

    public List<User> getAdmins() {
        return userRepository.findActiveUsersByRole(UserRole.ADMIN);
    }
    
    // Additional methods for admin functionality
    public User getUserByIdDirect(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User createUser(User user, String password) {
        // Encode password
        user.setPasswordHash(passwordEncoder.encode(password));
        
        // Set timestamps
        user.setCreatedAt(java.time.LocalDateTime.now());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }
    
    @Transactional
    public User toggleUserStatus(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Handle null values - default to true if null, then toggle
        Boolean currentStatus = user.getIsActive();
        if (currentStatus == null) {
            currentStatus = true; // Default to active if null
        }
        
        // Use direct update query to avoid validation issues
        boolean newStatus = !currentStatus;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        int updated = userRepository.updateUserStatus(userId, newStatus, now);
        if (updated == 0) {
            throw new RuntimeException("Failed to update user status");
        }
        
        // Return the updated user
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found after update"));
    }
    
    public void softDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Soft delete by setting inactive
        user.setIsActive(false);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }
    
    public List<User> getStaffByVertical(StaffVertical vertical) {
        return userRepository.findByRoleAndStaffVerticalAndIsActiveTrue(UserRole.STAFF, vertical);
    }
    
    public List<User> getAllActiveStaff() {
        return userRepository.findAllActiveStaff();
    }
    
    public Optional<User> findByStaffId(String staffId) {
        return userRepository.findByStaffIdAndIsActiveTrue(staffId);
    }
} 