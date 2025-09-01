package com.hostel.controller;

import com.hostel.dto.DTOMapper;
import com.hostel.dto.UserDTO;
import com.hostel.entity.*;
import com.hostel.repository.CategoryStaffMappingRepository;
import com.hostel.repository.HostelBlockRepository;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin Controller for IIM Trichy Hostel Ticket Management System
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryStaffMappingRepository categoryStaffMappingRepository;
    
    @Autowired
    private HostelBlockRepository hostelBlockRepository;
    
    // Test endpoint - no dependencies
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Admin controller is working!");
    }
    
    // Simple test without any service calls
    @GetMapping("/simple")
    public String simpleTest() {
        return "Simple test works!";
    }
    
    // Test users endpoint without database
    @GetMapping("/users-test")
    public ResponseEntity<?> testUsers() {
        try {
            System.out.println("AdminController: testUsers() called");
            return ResponseEntity.ok("Users endpoint test works!");
        } catch (Exception e) {
            System.err.println("AdminController: Error in testUsers(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error in test: " + e.getMessage());
        }
    }
    
    // Get all users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            System.out.println("AdminController: getAllUsers() called");
            List<User> users = userService.getAllUsers();
            System.out.println("AdminController: Found " + users.size() + " users");
            
            List<UserDTO> userDTOs = new java.util.ArrayList<>();
            for (User user : users) {
                try {
                    UserDTO dto = DTOMapper.toUserDTO(user);
                    userDTOs.add(dto);
                } catch (Exception e) {
                    System.err.println("Error converting user " + user.getUsername() + ": " + e.getMessage());
                    // Skip this user and continue with others
                }
            }
            
            System.out.println("AdminController: Successfully converted " + userDTOs.size() + " users to DTOs");
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            System.err.println("AdminController: Error in getAllUsers(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching users: " + e.getMessage());
        }
    }
    
    // Get user by ID
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable UUID userId) {
        try {
            System.out.println("AdminController: getUserById() called for ID: " + userId);
            User user = userService.getUserByIdDirect(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            UserDTO userDTO = DTOMapper.toUserDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            System.err.println("AdminController: Error in getUserById(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching user: " + e.getMessage());
        }
    }
    
    // Create user endpoint
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            System.out.println("AdminController: createUser() called with: " + request.getUsername());
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            UserRole role = UserRole.valueOf(request.getRole());
            user.setRole(role);
            
            user.setPhone(request.getPhone());
            if (request.getHostelBlock() != null && !request.getHostelBlock().trim().isEmpty()) {
                try {
                    user.setHostelBlock(HostelName.valueOf(request.getHostelBlock()));
                } catch (IllegalArgumentException e) {
                    // Try to find by display name
                    try {
                        user.setHostelBlock(HostelName.fromDisplayName(request.getHostelBlock()));
                    } catch (IllegalArgumentException e2) {
                        return ResponseEntity.badRequest().build(); // Invalid hostel name
                    }
                }
            }
            user.setRoomNumber(request.getRoomNumber());
            user.setIsActive(true);
            
            // Handle role-specific required fields based on database constraints
            if (role == UserRole.STUDENT) {
                // Students must have student_id
                if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
                    return ResponseEntity.badRequest().build(); // Student ID is required
                }
                user.setStudentId(request.getStudentId());
            } else if (role == UserRole.STAFF) {
                // Staff must have staff_id and staff_vertical
                if (request.getStaffId() == null || request.getStaffId().trim().isEmpty()) {
                    return ResponseEntity.badRequest().build(); // Staff ID is required
                }
                user.setStaffId(request.getStaffId());
                // Set a default staff vertical if not provided
                if (request.getStaffVertical() != null) {
                    user.setStaffVertical(StaffVertical.valueOf(request.getStaffVertical()));
                } else {
                    user.setStaffVertical(StaffVertical.GENERAL_MAINTENANCE); // Default value
                }
            } else if (role == UserRole.ADMIN) {
                // Admins can have either student_id or staff_id, but let's set staff_id for admins
                if (request.getStaffId() != null && !request.getStaffId().trim().isEmpty()) {
                    user.setStaffId(request.getStaffId());
                    user.setStaffVertical(StaffVertical.ADMIN_STAFF);
                }
            }
            
            // Set a temporary password hash to pass validation - will be overridden by userService
            user.setPasswordHash("temp");
            
            User createdUser = userService.createUser(user, request.getPassword());
            System.out.println("AdminController: User created successfully: " + createdUser.getUsername());
            return ResponseEntity.ok(DTOMapper.toUserDTO(createdUser));
        } catch (Exception e) {
            System.err.println("AdminController: Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }
    
    // Update user endpoint
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID userId, @RequestBody UpdateUserRequest request) {
        try {
            User user = userService.getUserByIdDirect(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
            if (request.getLastName() != null) user.setLastName(request.getLastName());
            if (request.getPhone() != null) user.setPhone(request.getPhone());
            if (request.getHostelBlock() != null && !request.getHostelBlock().trim().isEmpty()) {
                try {
                    user.setHostelBlock(HostelName.valueOf(request.getHostelBlock()));
                } catch (IllegalArgumentException e) {
                    // Try to find by display name
                    try {
                        user.setHostelBlock(HostelName.fromDisplayName(request.getHostelBlock()));
                    } catch (IllegalArgumentException e2) {
                        return ResponseEntity.badRequest().build(); // Invalid hostel name
                    }
                }
            }
            if (request.getRoomNumber() != null) user.setRoomNumber(request.getRoomNumber());
            if (request.getStudentId() != null) user.setStudentId(request.getStudentId());
            
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(DTOMapper.toUserDTO(updatedUser));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Toggle user status
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable UUID userId) {
        try {
            System.out.println("AdminController: toggleUserStatus() called for userId: " + userId);
            User user = userService.toggleUserStatus(userId);
            System.out.println("AdminController: User after toggle - isActive: " + user.getIsActive());
            UserDTO userDTO = DTOMapper.toUserDTO(user);
            System.out.println("AdminController: UserDTO after conversion - isActive: " + userDTO.getIsActive());
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            System.err.println("AdminController: Error in toggleUserStatus(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error toggling user status: " + e.getMessage());
        }
    }
    
    // =====================================================
    // STAFF-HOSTEL-CATEGORY MAPPING MANAGEMENT
    // =====================================================
    
    // Get all mappings
    @GetMapping("/mappings")
    public ResponseEntity<?> getAllMappings() {
        try {
            System.out.println("AdminController: getAllMappings() called");
            List<CategoryStaffMapping> mappings = categoryStaffMappingRepository.findByIsActiveTrueOrderByPriorityLevelAsc();
            System.out.println("AdminController: Found " + mappings.size() + " mappings");
            List<CategoryStaffMappingDTO> mappingDTOs = mappings.stream()
                    .map(this::toCategoryStaffMappingDTO)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(mappingDTOs);
        } catch (Exception e) {
            System.err.println("AdminController: Error in getAllMappings(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching mappings: " + e.getMessage());
        }
    }
    
    // Get mappings by staff ID
    @GetMapping("/mappings/staff/{staffId}")
    public ResponseEntity<List<CategoryStaffMappingDTO>> getMappingsByStaff(@PathVariable UUID staffId) {
        try {
            List<CategoryStaffMapping> mappings = categoryStaffMappingRepository.findByStaffIdAndIsActiveTrue(staffId);
            List<CategoryStaffMappingDTO> mappingDTOs = mappings.stream()
                    .map(this::toCategoryStaffMappingDTO)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(mappingDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get mappings by category
    @GetMapping("/mappings/category/{category}")
    public ResponseEntity<List<CategoryStaffMappingDTO>> getMappingsByCategory(@PathVariable String category) {
        try {
            List<CategoryStaffMapping> mappings = categoryStaffMappingRepository.findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc(category);
            List<CategoryStaffMappingDTO> mappingDTOs = mappings.stream()
                    .map(this::toCategoryStaffMappingDTO)
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(mappingDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Create new mapping
    @PostMapping("/mappings")
    public ResponseEntity<?> createMapping(@RequestBody CreateMappingRequest request) {
        try {
            System.out.println("AdminController: createMapping() called for staff: " + request.getStaffId());
            // Validate staff exists and is active
            User staff = userService.getUserByIdDirect(UUID.fromString(request.getStaffId()));
            if (staff == null || !staff.getIsActive()) {
                return ResponseEntity.badRequest().build();
            }
            
            // Validate staff is actually staff role
            if (staff.getRole() != UserRole.STAFF) {
                return ResponseEntity.badRequest().build();
            }
            
            // Check if mapping already exists
            CategoryStaffMapping existingMapping = categoryStaffMappingRepository
                .findByStaffAndCategoryAndIsActiveTrue(staff, request.getCategory());
            if (existingMapping != null) {
                return ResponseEntity.badRequest().build(); // Mapping already exists
            }
            
            // Create new mapping
            CategoryStaffMapping mapping = new CategoryStaffMapping();
            mapping.setStaff(staff);
            mapping.setHostelBlock(request.getHostelBlock());
            mapping.setCategory(request.getCategory());
            mapping.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
            mapping.setCapacityWeight(request.getCapacityWeight() != null ? 
                java.math.BigDecimal.valueOf(request.getCapacityWeight()) : java.math.BigDecimal.ONE);
            mapping.setExpertiseLevel(request.getExpertiseLevel() != null ? request.getExpertiseLevel() : 1);
            mapping.setIsActive(true);
            
            CategoryStaffMapping savedMapping = categoryStaffMappingRepository.save(mapping);
            System.out.println("AdminController: Mapping created successfully: " + savedMapping.getId());
            return ResponseEntity.ok(toCategoryStaffMappingDTO(savedMapping));
        } catch (Exception e) {
            System.err.println("AdminController: Error creating mapping: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating mapping: " + e.getMessage());
        }
    }
    
    // Update mapping
    @PutMapping("/mappings/{mappingId}")
    public ResponseEntity<CategoryStaffMappingDTO> updateMapping(
            @PathVariable UUID mappingId, 
            @RequestBody UpdateMappingRequest request) {
        try {
            CategoryStaffMapping mapping = categoryStaffMappingRepository.findById(mappingId).orElse(null);
            if (mapping == null || !mapping.getIsActive()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields
            if (request.getHostelBlock() != null) {
                mapping.setHostelBlock(request.getHostelBlock());
            }
            if (request.getPriorityLevel() != null) {
                mapping.setPriorityLevel(request.getPriorityLevel());
            }
            if (request.getCapacityWeight() != null) {
                mapping.setCapacityWeight(java.math.BigDecimal.valueOf(request.getCapacityWeight()));
            }
            if (request.getExpertiseLevel() != null) {
                mapping.setExpertiseLevel(request.getExpertiseLevel());
            }
            
            CategoryStaffMapping savedMapping = categoryStaffMappingRepository.save(mapping);
            return ResponseEntity.ok(toCategoryStaffMappingDTO(savedMapping));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Delete mapping (soft delete)
    @DeleteMapping("/mappings/{mappingId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable UUID mappingId) {
        try {
            CategoryStaffMapping mapping = categoryStaffMappingRepository.findById(mappingId).orElse(null);
            if (mapping == null) {
                return ResponseEntity.notFound().build();
            }
            
            mapping.setIsActive(false);
            categoryStaffMappingRepository.save(mapping);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get all available categories
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryInfo>> getAvailableCategories() {
        try {
            List<CategoryInfo> categories = java.util.Arrays.stream(TicketCategory.values())
                    .map(category -> new CategoryInfo(
                        category.name(),
                        category.getDisplayName(),
                        category.getDescription(),
                        category.getIcon(),
                        category.getCategoryGroup(),
                        category.requiresSpecializedStaff()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get all hostel blocks (legacy endpoint - still supported)
    @GetMapping("/hostel-blocks")
    public ResponseEntity<List<HostelBlockInfo>> getHostelBlocks() {
        try {
            List<HostelBlock> blocks = hostelBlockRepository.findByIsActiveTrueOrderByBlockName();
            List<HostelBlockInfo> blockInfos = blocks.stream()
                    .map(block -> new HostelBlockInfo(
                        block.getId().toString(),
                        block.getBlockName(),
                        block.getBlockCode(),
                        block.getTotalFloors(),
                        block.getTotalRooms(),
                        block.getIsFemaleBlock()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(blockInfos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get all hostel names (new enum-based endpoint)
    @GetMapping("/hostels")
    public ResponseEntity<?> getHostels() {
        try {
            System.out.println("AdminController: getHostels() called");
            List<HostelInfo> hostels = java.util.Arrays.stream(HostelName.values())
                    .map(hostel -> new HostelInfo(
                        hostel.name(),
                        hostel.getDisplayName(),
                        hostel.getCode(),
                        hostel.getFullName(),
                        hostel.isFemaleBlock()
                    ))
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("AdminController: Returning " + hostels.size() + " hostels");
            return ResponseEntity.ok(hostels);
        } catch (Exception e) {
            System.err.println("AdminController: Error in getHostels(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching hostels: " + e.getMessage());
        }
    }
    
    // Get staff members eligible for mapping (only STAFF role)
    @GetMapping("/staff")
    public ResponseEntity<?> getStaffMembers() {
        try {
            System.out.println("AdminController: getStaffMembers() called");
            List<User> staffMembers = userService.getUsersByRole(UserRole.STAFF);
            System.out.println("AdminController: Found " + staffMembers.size() + " staff members");
            List<UserDTO> staffDTOs = staffMembers.stream()
                    .filter(User::getIsActive)
                    .map(DTOMapper::toUserDTO)
                    .collect(java.util.stream.Collectors.toList());
            System.out.println("AdminController: Returning " + staffDTOs.size() + " active staff members");
            return ResponseEntity.ok(staffDTOs);
        } catch (Exception e) {
            System.err.println("AdminController: Error in getStaffMembers(): " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching staff members: " + e.getMessage());
        }
    }
    
    // Helper method to convert CategoryStaffMapping to DTO
    private CategoryStaffMappingDTO toCategoryStaffMappingDTO(CategoryStaffMapping mapping) {
        CategoryStaffMappingDTO dto = new CategoryStaffMappingDTO();
        dto.setId(mapping.getId().toString());
        dto.setStaffId(mapping.getStaff().getId().toString());
        dto.setStaffName(mapping.getStaff().getFirstName() + " " + mapping.getStaff().getLastName());
        dto.setStaffUsername(mapping.getStaff().getUsername());
        dto.setHostelBlock(mapping.getHostelBlock());
        dto.setCategory(mapping.getCategory());
        dto.setPriorityLevel(mapping.getPriorityLevel());
        dto.setCapacityWeight(mapping.getCapacityWeight().doubleValue());
        dto.setExpertiseLevel(mapping.getExpertiseLevel());
        dto.setActive(mapping.getIsActive());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setUpdatedAt(mapping.getUpdatedAt());
        
        // Add category display info
        try {
            TicketCategory ticketCategory = TicketCategory.valueOf(mapping.getCategory());
            dto.setCategoryDisplayName(ticketCategory.getDisplayName());
            dto.setCategoryIcon(ticketCategory.getIcon());
        } catch (IllegalArgumentException e) {
            // Custom category
            dto.setCategoryDisplayName(mapping.getCategory());
            dto.setCategoryIcon("🔧");
        }
        
        return dto;
    }
    
    // Request DTOs
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String role;
        private String phone;
        private String hostelBlock;
        private String roomNumber;
        private String studentId;
        private String staffId;
        private String staffVertical;
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        
        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        
        public String getStaffVertical() { return staffVertical; }
        public void setStaffVertical(String staffVertical) { this.staffVertical = staffVertical; }
    }
    
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String phone;
        private String hostelBlock;
        private String roomNumber;
        private String studentId;
        
        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        
        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        
        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
    }
    
    // =====================================================
    // MAPPING MANAGEMENT DTOs
    // =====================================================
    
    public static class CreateMappingRequest {
        private String staffId;
        private String hostelBlock; // null means all blocks
        private String category;
        private Integer priorityLevel = 1;
        private Double capacityWeight = 1.0;
        private Integer expertiseLevel = 1;
        
        // Getters and setters
        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
        
        public Double getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(Double capacityWeight) { this.capacityWeight = capacityWeight; }
        
        public Integer getExpertiseLevel() { return expertiseLevel; }
        public void setExpertiseLevel(Integer expertiseLevel) { this.expertiseLevel = expertiseLevel; }
    }
    
    public static class UpdateMappingRequest {
        private String hostelBlock;
        private Integer priorityLevel;
        private Double capacityWeight;
        private Integer expertiseLevel;
        
        // Getters and setters
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
        
        public Double getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(Double capacityWeight) { this.capacityWeight = capacityWeight; }
        
        public Integer getExpertiseLevel() { return expertiseLevel; }
        public void setExpertiseLevel(Integer expertiseLevel) { this.expertiseLevel = expertiseLevel; }
    }
    
    public static class CategoryStaffMappingDTO {
        private String id;
        private String staffId;
        private String staffName;
        private String staffUsername;
        private String hostelBlock;
        private String category;
        private String categoryDisplayName;
        private String categoryIcon;
        private Integer priorityLevel;
        private Double capacityWeight;
        private Integer expertiseLevel;
        private Boolean active;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getStaffId() { return staffId; }
        public void setStaffId(String staffId) { this.staffId = staffId; }
        
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        
        public String getStaffUsername() { return staffUsername; }
        public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }
        
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getCategoryDisplayName() { return categoryDisplayName; }
        public void setCategoryDisplayName(String categoryDisplayName) { this.categoryDisplayName = categoryDisplayName; }
        
        public String getCategoryIcon() { return categoryIcon; }
        public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
        
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
        
        public Double getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(Double capacityWeight) { this.capacityWeight = capacityWeight; }
        
        public Integer getExpertiseLevel() { return expertiseLevel; }
        public void setExpertiseLevel(Integer expertiseLevel) { this.expertiseLevel = expertiseLevel; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
    
    public static class CategoryInfo {
        private String value;
        private String displayName;
        private String description;
        private String icon;
        private String group;
        private Boolean requiresSpecializedStaff;
        
        public CategoryInfo(String value, String displayName, String description, 
                           String icon, String group, Boolean requiresSpecializedStaff) {
            this.value = value;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.group = group;
            this.requiresSpecializedStaff = requiresSpecializedStaff;
        }
        
        // Getters and setters
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public String getGroup() { return group; }
        public void setGroup(String group) { this.group = group; }
        
        public Boolean getRequiresSpecializedStaff() { return requiresSpecializedStaff; }
        public void setRequiresSpecializedStaff(Boolean requiresSpecializedStaff) { this.requiresSpecializedStaff = requiresSpecializedStaff; }
    }
    
    public static class HostelBlockInfo {
        private String id;
        private String blockName;
        private String blockCode;
        private Integer totalFloors;
        private Integer totalRooms;
        private Boolean isFemaleBlock;
        
        public HostelBlockInfo(String id, String blockName, String blockCode, 
                              Integer totalFloors, Integer totalRooms, Boolean isFemaleBlock) {
            this.id = id;
            this.blockName = blockName;
            this.blockCode = blockCode;
            this.totalFloors = totalFloors;
            this.totalRooms = totalRooms;
            this.isFemaleBlock = isFemaleBlock;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getBlockName() { return blockName; }
        public void setBlockName(String blockName) { this.blockName = blockName; }
        
        public String getBlockCode() { return blockCode; }
        public void setBlockCode(String blockCode) { this.blockCode = blockCode; }
        
        public Integer getTotalFloors() { return totalFloors; }
        public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }
        
        public Integer getTotalRooms() { return totalRooms; }
        public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }
        
        public Boolean getIsFemaleBlock() { return isFemaleBlock; }
        public void setIsFemaleBlock(Boolean isFemaleBlock) { this.isFemaleBlock = isFemaleBlock; }
    }
    
    public static class HostelInfo {
        private String value;
        private String displayName;
        private String code;
        private String fullName;
        private Boolean isFemaleBlock;
        
        public HostelInfo(String value, String displayName, String code, 
                         String fullName, Boolean isFemaleBlock) {
            this.value = value;
            this.displayName = displayName;
            this.code = code;
            this.fullName = fullName;
            this.isFemaleBlock = isFemaleBlock;
        }
        
        // Getters and setters
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public Boolean getIsFemaleBlock() { return isFemaleBlock; }
        public void setIsFemaleBlock(Boolean isFemaleBlock) { this.isFemaleBlock = isFemaleBlock; }
    }
}