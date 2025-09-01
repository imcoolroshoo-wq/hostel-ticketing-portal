package com.hostel.controller;

import com.hostel.entity.*;
import com.hostel.repository.CategoryStaffMappingRepository;
import com.hostel.repository.UserRepository;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dedicated Mapping Management Controller implementing Staff-Hostel-Category 
 * mapping functionality as per IIM Trichy Product Design Document Section 4.3.2
 */
@RestController
@RequestMapping("/admin/mappings")
@PreAuthorize("hasRole('ADMIN')")
public class MappingManagementController {

    @Autowired
    private CategoryStaffMappingRepository mappingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    // Note: TicketAssignmentService available for future use

    /**
     * Get all mappings with pagination and filtering
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMappings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priorityLevel") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String staffId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String hostelBlock) {
        
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<CategoryStaffMapping> mappings;
            
            // Apply filters
            if (staffId != null && !staffId.trim().isEmpty()) {
                List<CategoryStaffMapping> filteredMappings = mappingRepository.findByStaffIdAndIsActiveTrue(UUID.fromString(staffId));
                mappings = new org.springframework.data.domain.PageImpl<>(filteredMappings, pageable, filteredMappings.size());
            } else if (category != null && !category.trim().isEmpty()) {
                List<CategoryStaffMapping> filteredMappings = mappingRepository.findByCategoryAndIsActiveTrueOrderByPriorityLevelAsc(category);
                mappings = new org.springframework.data.domain.PageImpl<>(filteredMappings, pageable, filteredMappings.size());
            } else if (hostelBlock != null && !hostelBlock.trim().isEmpty()) {
                HostelName hostelName = HostelName.fromAnyName(hostelBlock);
                List<CategoryStaffMapping> filteredMappings = mappingRepository.findByHostelBlockAndCategoryAndIsActiveTrueOrderByPriorityLevelAsc(hostelName, null);
                mappings = new org.springframework.data.domain.PageImpl<>(filteredMappings, pageable, filteredMappings.size());
            } else {
                List<CategoryStaffMapping> allMappings = mappingRepository.findByIsActiveTrueOrderByPriorityLevelAsc();
                mappings = new org.springframework.data.domain.PageImpl<>(allMappings, pageable, allMappings.size());
            }
            
            List<MappingDTO> mappingDTOs = mappings.getContent().stream()
                    .map(this::toMappingDTO)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("mappings", mappingDTOs);
            response.put("currentPage", mappings.getNumber());
            response.put("totalItems", mappings.getTotalElements());
            response.put("totalPages", mappings.getTotalPages());
            response.put("pageSize", mappings.getSize());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error fetching mappings: " + e.getMessage()));
        }
    }

    /**
     * Get mapping by ID
     */
    @GetMapping("/{mappingId}")
    public ResponseEntity<MappingDTO> getMappingById(@PathVariable UUID mappingId) {
        try {
            CategoryStaffMapping mapping = mappingRepository.findById(mappingId).orElse(null);
            if (mapping == null || !mapping.getIsActive()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(toMappingDTO(mapping));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create new mapping
     */
    @PostMapping
    public ResponseEntity<MappingDTO> createMapping(@RequestBody CreateMappingRequest request) {
        try {
            // Validate staff
            User staff = userService.getUserByIdDirect(UUID.fromString(request.getStaffId()));
            if (staff == null || !staff.getIsActive() || !staff.getRole().equals(UserRole.STAFF)) {
                return ResponseEntity.badRequest().build();
            }
            
            // Check for duplicate mapping
            HostelName hostelBlock = request.getHostelBlock() != null ? 
                HostelName.fromAnyName(request.getHostelBlock()) : null;
            
            CategoryStaffMapping existingMapping = mappingRepository
                .findByStaffAndCategoryAndIsActiveTrue(staff, request.getCategory());
            if (existingMapping != null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Create mapping
            CategoryStaffMapping mapping = new CategoryStaffMapping();
            mapping.setStaff(staff);
            mapping.setHostelBlock(hostelBlock);
            mapping.setCategory(request.getCategory());
            mapping.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : 1);
            mapping.setCapacityWeight(request.getCapacityWeight() != null ? 
                BigDecimal.valueOf(request.getCapacityWeight()) : BigDecimal.ONE);
            mapping.setExpertiseLevel(request.getExpertiseLevel() != null ? request.getExpertiseLevel() : 1);
            mapping.setIsActive(true);
            
            CategoryStaffMapping savedMapping = mappingRepository.save(mapping);
            return ResponseEntity.ok(toMappingDTO(savedMapping));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update existing mapping
     */
    @PutMapping("/{mappingId}")
    public ResponseEntity<MappingDTO> updateMapping(
            @PathVariable UUID mappingId, 
            @RequestBody UpdateMappingRequest request) {
        try {
            CategoryStaffMapping mapping = mappingRepository.findById(mappingId).orElse(null);
            if (mapping == null || !mapping.getIsActive()) {
                return ResponseEntity.notFound().build();
            }
            
            // Update fields
            if (request.getHostelBlock() != null) {
                mapping.setHostelBlock(HostelName.fromAnyName(request.getHostelBlock()));
            }
            if (request.getPriorityLevel() != null) {
                mapping.setPriorityLevel(request.getPriorityLevel());
            }
            if (request.getCapacityWeight() != null) {
                mapping.setCapacityWeight(BigDecimal.valueOf(request.getCapacityWeight()));
            }
            if (request.getExpertiseLevel() != null) {
                mapping.setExpertiseLevel(request.getExpertiseLevel());
            }
            
            CategoryStaffMapping savedMapping = mappingRepository.save(mapping);
            return ResponseEntity.ok(toMappingDTO(savedMapping));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete mapping (soft delete)
     */
    @DeleteMapping("/{mappingId}")
    public ResponseEntity<Map<String, String>> deleteMapping(@PathVariable UUID mappingId) {
        try {
            CategoryStaffMapping mapping = mappingRepository.findById(mappingId).orElse(null);
            if (mapping == null) {
                return ResponseEntity.notFound().build();
            }
            
            mapping.setIsActive(false);
            mappingRepository.save(mapping);
            
            return ResponseEntity.ok(Map.of("message", "Mapping deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error deleting mapping: " + e.getMessage()));
        }
    }

    /**
     * Bulk create mappings
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createBulkMappings(@RequestBody List<CreateMappingRequest> requests) {
        List<String> successful = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        
        for (CreateMappingRequest request : requests) {
            try {
                ResponseEntity<MappingDTO> result = createMapping(request);
                if (result.getStatusCode().is2xxSuccessful()) {
                    successful.add("Mapping created for staff " + request.getStaffId() + " and category " + request.getCategory());
                } else {
                    failed.add("Failed to create mapping for staff " + request.getStaffId() + " and category " + request.getCategory());
                }
            } catch (Exception e) {
                failed.add("Error creating mapping for staff " + request.getStaffId() + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("successful", successful);
        response.put("failed", failed);
        response.put("totalProcessed", requests.size());
        response.put("successCount", successful.size());
        response.put("failedCount", failed.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get mapping effectiveness analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<MappingAnalytics> getMappingAnalytics() {
        try {
            List<CategoryStaffMapping> allMappings = mappingRepository.findByIsActiveTrueOrderByPriorityLevelAsc();
            
            Map<String, Long> categoryDistribution = allMappings.stream()
                .collect(Collectors.groupingBy(
                    CategoryStaffMapping::getCategory, 
                    Collectors.counting()
                ));
            
            Map<String, Long> hostelDistribution = allMappings.stream()
                .collect(Collectors.groupingBy(
                    mapping -> mapping.getHostelBlock() != null ? mapping.getHostelBlock().name() : "ALL",
                    Collectors.counting()
                ));
            
            long totalStaffMapped = allMappings.stream()
                .map(mapping -> mapping.getStaff().getId())
                .distinct()
                .count();
            
            long totalActiveStaff = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF).size();
            double mappingCoverage = totalActiveStaff > 0 ? 
                (double) totalStaffMapped / totalActiveStaff * 100 : 0;
            
            MappingAnalytics analytics = new MappingAnalytics(
                allMappings.size(),
                totalStaffMapped,
                mappingCoverage,
                categoryDistribution,
                hostelDistribution
            );
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Validate mapping configuration
     */
    @PostMapping("/validate")
    public ResponseEntity<MappingValidationResult> validateMappings() {
        try {
            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            // Check for categories without any mappings
            Set<String> mappedCategories = mappingRepository.findByIsActiveTrueOrderByPriorityLevelAsc()
                .stream()
                .map(CategoryStaffMapping::getCategory)
                .collect(Collectors.toSet());
            
            for (TicketCategory category : TicketCategory.values()) {
                if (!mappedCategories.contains(category.name())) {
                    warnings.add("Category " + category.getDisplayName() + " has no staff mappings");
                }
            }
            
            // Check for hostels without coverage
            List<HostelName> hostels = Arrays.asList(HostelName.values());
            Set<HostelName> mappedHostels = mappingRepository.findByIsActiveTrueOrderByPriorityLevelAsc()
                .stream()
                .map(CategoryStaffMapping::getHostelBlock)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            for (HostelName hostel : hostels) {
                if (!mappedHostels.contains(hostel)) {
                    warnings.add("Hostel " + hostel.getDisplayName() + " has limited mapping coverage");
                }
            }
            
            // Check for staff with no mappings
            List<User> staffWithoutMappings = userRepository.findByRoleAndIsActiveTrue(UserRole.STAFF)
                .stream()
                .filter(staff -> mappingRepository.findByStaffAndIsActiveTrue(staff).isEmpty())
                .collect(Collectors.toList());
            
            if (!staffWithoutMappings.isEmpty()) {
                warnings.add(staffWithoutMappings.size() + " staff members have no category mappings");
            }
            
            MappingValidationResult result = new MappingValidationResult(
                errors.isEmpty(),
                warnings,
                errors,
                "Mapping validation completed"
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Helper method to convert entity to DTO
    private MappingDTO toMappingDTO(CategoryStaffMapping mapping) {
        MappingDTO dto = new MappingDTO();
        dto.setId(mapping.getId());
        dto.setStaffId(mapping.getStaff().getId());
        dto.setStaffName(mapping.getStaff().getFirstName() + " " + mapping.getStaff().getLastName());
        dto.setStaffUsername(mapping.getStaff().getUsername());
        dto.setHostelBlock(mapping.getHostelBlock() != null ? mapping.getHostelBlock().name() : null);
        dto.setHostelBlockDisplayName(mapping.getHostelBlock() != null ? mapping.getHostelBlock().getDisplayName() : "All Hostels");
        dto.setCategory(mapping.getCategory());
        dto.setPriorityLevel(mapping.getPriorityLevel());
        dto.setCapacityWeight(mapping.getCapacityWeight());
        dto.setExpertiseLevel(mapping.getExpertiseLevel());
        dto.setIsActive(mapping.getIsActive());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setUpdatedAt(mapping.getUpdatedAt());
        return dto;
    }

    // DTOs
    public static class CreateMappingRequest {
        private String staffId;
        private String hostelBlock;
        private String category;
        private Integer priorityLevel;
        private Double capacityWeight;
        private Integer expertiseLevel;

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

    public static class MappingDTO {
        private UUID id;
        private UUID staffId;
        private String staffName;
        private String staffUsername;
        private String hostelBlock;
        private String hostelBlockDisplayName;
        private String category;
        private Integer priorityLevel;
        private BigDecimal capacityWeight;
        private Integer expertiseLevel;
        private Boolean isActive;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public UUID getStaffId() { return staffId; }
        public void setStaffId(UUID staffId) { this.staffId = staffId; }
        public String getStaffName() { return staffName; }
        public void setStaffName(String staffName) { this.staffName = staffName; }
        public String getStaffUsername() { return staffUsername; }
        public void setStaffUsername(String staffUsername) { this.staffUsername = staffUsername; }
        public String getHostelBlock() { return hostelBlock; }
        public void setHostelBlock(String hostelBlock) { this.hostelBlock = hostelBlock; }
        public String getHostelBlockDisplayName() { return hostelBlockDisplayName; }
        public void setHostelBlockDisplayName(String hostelBlockDisplayName) { this.hostelBlockDisplayName = hostelBlockDisplayName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Integer getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(Integer priorityLevel) { this.priorityLevel = priorityLevel; }
        public BigDecimal getCapacityWeight() { return capacityWeight; }
        public void setCapacityWeight(BigDecimal capacityWeight) { this.capacityWeight = capacityWeight; }
        public Integer getExpertiseLevel() { return expertiseLevel; }
        public void setExpertiseLevel(Integer expertiseLevel) { this.expertiseLevel = expertiseLevel; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class MappingAnalytics {
        private long totalMappings;
        private long staffMapped;
        private double coveragePercentage;
        private Map<String, Long> categoryDistribution;
        private Map<String, Long> hostelDistribution;

        public MappingAnalytics(long totalMappings, long staffMapped, double coveragePercentage,
                               Map<String, Long> categoryDistribution, Map<String, Long> hostelDistribution) {
            this.totalMappings = totalMappings;
            this.staffMapped = staffMapped;
            this.coveragePercentage = coveragePercentage;
            this.categoryDistribution = categoryDistribution;
            this.hostelDistribution = hostelDistribution;
        }

        // Getters
        public long getTotalMappings() { return totalMappings; }
        public long getStaffMapped() { return staffMapped; }
        public double getCoveragePercentage() { return coveragePercentage; }
        public Map<String, Long> getCategoryDistribution() { return categoryDistribution; }
        public Map<String, Long> getHostelDistribution() { return hostelDistribution; }
    }

    public static class MappingValidationResult {
        private boolean isValid;
        private List<String> warnings;
        private List<String> errors;
        private String message;

        public MappingValidationResult(boolean isValid, List<String> warnings, List<String> errors, String message) {
            this.isValid = isValid;
            this.warnings = warnings;
            this.errors = errors;
            this.message = message;
        }

        // Getters
        public boolean isValid() { return isValid; }
        public List<String> getWarnings() { return warnings; }
        public List<String> getErrors() { return errors; }
        public String getMessage() { return message; }
    }
}
