package com.hostel.service;

import com.hostel.entity.Asset;
import com.hostel.entity.AssetMovement;
import com.hostel.entity.AssetStatus;
import com.hostel.entity.AssetType;
import com.hostel.entity.MaintenanceSchedule;
import com.hostel.entity.User;
import com.hostel.repository.AssetRepository;
import com.hostel.repository.AssetMovementRepository;
import com.hostel.repository.MaintenanceScheduleRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Asset Management Service implementing Asset Management functionality
 * as per IIM Trichy Hostel Ticket Management System Product Design Document Section 4.3.4
 */
@Service
@Transactional
public class AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMovementRepository assetMovementRepository;

    @Autowired
    private MaintenanceScheduleRepository maintenanceScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all assets with filtering
     */
    public Page<Asset> getAllAssets(Pageable pageable, String building, String type, String status, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return assetRepository.findBySearchCriteria(search.trim(), pageable);
        }
        
        // Apply filters
        AssetType assetType = null;
        AssetStatus assetStatus = null;
        
        try {
            if (type != null && !type.trim().isEmpty()) {
                assetType = AssetType.valueOf(type.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Invalid type, ignore filter
        }
        
        try {
            if (status != null && !status.trim().isEmpty()) {
                assetStatus = AssetStatus.valueOf(status.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            // Invalid status, ignore filter
        }
        
        return assetRepository.findByFilters(building, assetType, assetStatus, pageable);
    }

    /**
     * Get asset by ID
     */
    public Optional<Asset> getAssetById(UUID assetId) {
        return assetRepository.findById(assetId);
    }

    /**
     * Get asset by tag
     */
    public Optional<Asset> getAssetByTag(String assetTag) {
        return assetRepository.findByAssetTag(assetTag);
    }

    /**
     * Create new asset
     */
    public Asset createAsset(Asset asset) {
        // Validate asset tag uniqueness
        if (assetRepository.findByAssetTag(asset.getAssetTag()).isPresent()) {
            throw new RuntimeException("Asset tag already exists: " + asset.getAssetTag());
        }
        
        // Set default values
        if (asset.getStatus() == null) {
            asset.setStatus(AssetStatus.ACTIVE);
        }
        
        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());
        
        Asset savedAsset = assetRepository.save(asset);
        
        // Create initial movement record
        AssetMovement initialMovement = new AssetMovement();
        initialMovement.setAsset(savedAsset);
        initialMovement.setMovementType("CREATED");
        initialMovement.setFromLocation("NEW");
        initialMovement.setToLocation(asset.getLocation());
        initialMovement.setMovedAt(LocalDateTime.now());
        initialMovement.setNotes("Asset created");
        assetMovementRepository.save(initialMovement);
        
        return savedAsset;
    }

    /**
     * Update asset
     */
    public Asset updateAsset(UUID assetId, Asset assetDetails) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        // Track location changes
        String oldLocation = asset.getLocation();
        String newLocation = assetDetails.getLocation();
        
        // Update asset fields
        asset.setName(assetDetails.getName());
        asset.setDescription(assetDetails.getDescription());
        asset.setType(assetDetails.getType());
        asset.setStatus(assetDetails.getStatus());
        asset.setBuilding(assetDetails.getBuilding());
        asset.setRoomNumber(assetDetails.getRoomNumber());
        asset.setLocation(assetDetails.getLocation());
        asset.setManufacturer(assetDetails.getManufacturer());
        asset.setModel(assetDetails.getModel());
        asset.setSerialNumber(assetDetails.getSerialNumber());
        asset.setPurchaseDate(assetDetails.getPurchaseDate());
        asset.setWarrantyExpiry(assetDetails.getWarrantyExpiry());
        asset.setPurchasePrice(assetDetails.getPurchasePrice());
        asset.setCurrentValue(assetDetails.getCurrentValue());
        asset.setUpdatedAt(LocalDateTime.now());
        
        Asset savedAsset = assetRepository.save(asset);
        
        // Create movement record if location changed
        if (!oldLocation.equals(newLocation)) {
            AssetMovement movement = new AssetMovement();
            movement.setAsset(savedAsset);
            movement.setMovementType("RELOCATED");
            movement.setFromLocation(oldLocation);
            movement.setToLocation(newLocation);
            movement.setMovedAt(LocalDateTime.now());
            movement.setNotes("Asset relocated via update");
            assetMovementRepository.save(movement);
        }
        
        return savedAsset;
    }

    /**
     * Delete asset (soft delete)
     */
    public void deleteAsset(UUID assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        asset.setStatus(AssetStatus.DISPOSED);
        asset.setUpdatedAt(LocalDateTime.now());
        assetRepository.save(asset);
        
        // Create disposal movement record
        AssetMovement movement = new AssetMovement();
        movement.setAsset(asset);
        movement.setMovementType("DISPOSED");
        movement.setFromLocation(asset.getLocation());
        movement.setToLocation("DISPOSED");
        movement.setMovedAt(LocalDateTime.now());
        movement.setNotes("Asset disposed/deleted");
        assetMovementRepository.save(movement);
    }

    /**
     * Get assets by building
     */
    public List<Asset> getAssetsByBuilding(String building) {
        return assetRepository.findByBuilding(building);
    }

    /**
     * Get assets by type
     */
    public List<Asset> getAssetsByType(AssetType type) {
        return assetRepository.findByType(type);
    }

    /**
     * Get assets by status
     */
    public List<Asset> getAssetsByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    /**
     * Assign asset to user
     */
    public Asset assignAsset(UUID assetId, UUID userId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        User previousAssignee = asset.getAssignedTo();
        asset.setAssignedTo(user);
        asset.setUpdatedAt(LocalDateTime.now());
        
        Asset savedAsset = assetRepository.save(asset);
        
        // Create assignment movement record
        AssetMovement movement = new AssetMovement();
        movement.setAsset(savedAsset);
        movement.setMovementType("ASSIGNED");
        movement.setFromLocation(previousAssignee != null ? previousAssignee.getFullName() : "UNASSIGNED");
        movement.setToLocation(user.getFullName());
        movement.setMovedAt(LocalDateTime.now());
        movement.setNotes("Asset assigned to " + user.getFullName());
        assetMovementRepository.save(movement);
        
        return savedAsset;
    }

    /**
     * Unassign asset
     */
    public Asset unassignAsset(UUID assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        User previousAssignee = asset.getAssignedTo();
        asset.setAssignedTo(null);
        asset.setUpdatedAt(LocalDateTime.now());
        
        Asset savedAsset = assetRepository.save(asset);
        
        // Create unassignment movement record
        if (previousAssignee != null) {
            AssetMovement movement = new AssetMovement();
            movement.setAsset(savedAsset);
            movement.setMovementType("UNASSIGNED");
            movement.setFromLocation(previousAssignee.getFullName());
            movement.setToLocation("UNASSIGNED");
            movement.setMovedAt(LocalDateTime.now());
            movement.setNotes("Asset unassigned from " + previousAssignee.getFullName());
            assetMovementRepository.save(movement);
        }
        
        return savedAsset;
    }

    /**
     * Get asset movements
     */
    public List<AssetMovement> getAssetMovements(UUID assetId) {
        return assetMovementRepository.findByAssetIdOrderByMovedAtDesc(assetId);
    }

    /**
     * Add asset movement
     */
    public AssetMovement addAssetMovement(UUID assetId, AssetMovement movement) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        movement.setAsset(asset);
        movement.setMovedAt(LocalDateTime.now());
        
        return assetMovementRepository.save(movement);
    }

    /**
     * Get maintenance schedules for asset
     */
    public List<MaintenanceSchedule> getMaintenanceSchedules(UUID assetId) {
        return maintenanceScheduleRepository.findByAssetIdOrderByScheduledDateDesc(assetId);
    }

    /**
     * Create maintenance schedule
     */
    public MaintenanceSchedule createMaintenanceSchedule(UUID assetId, MaintenanceSchedule schedule) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found with ID: " + assetId));
        
        schedule.setAsset(asset);
        schedule.setCreatedAt(LocalDateTime.now());
        
        return maintenanceScheduleRepository.save(schedule);
    }

    /**
     * Get asset statistics
     */
    public Map<String, Object> getAssetStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total assets
        long totalAssets = assetRepository.count();
        stats.put("totalAssets", totalAssets);
        
        // Assets by status
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (AssetStatus status : AssetStatus.values()) {
            long count = assetRepository.countByStatus(status);
            statusBreakdown.put(status.name(), count);
        }
        stats.put("statusBreakdown", statusBreakdown);
        
        // Assets by type
        Map<String, Long> typeBreakdown = new HashMap<>();
        for (AssetType type : AssetType.values()) {
            long count = assetRepository.countByType(type);
            typeBreakdown.put(type.name(), count);
        }
        stats.put("typeBreakdown", typeBreakdown);
        
        // Assets requiring maintenance
        long maintenanceRequired = assetRepository.countAssetsRequiringMaintenance();
        stats.put("maintenanceRequired", maintenanceRequired);
        
        // Assets with expired warranty
        long expiredWarranty = assetRepository.countAssetsWithExpiredWarranty();
        stats.put("expiredWarranty", expiredWarranty);
        
        // Assigned vs unassigned
        long assignedAssets = assetRepository.countByAssignedToIsNotNull();
        long unassignedAssets = totalAssets - assignedAssets;
        stats.put("assignedAssets", assignedAssets);
        stats.put("unassignedAssets", unassignedAssets);
        
        return stats;
    }

    /**
     * Search assets
     */
    public List<Asset> searchAssets(String query) {
        return assetRepository.findBySearchCriteria(query);
    }

    /**
     * Get assets requiring maintenance
     */
    public List<Asset> getAssetsRequiringMaintenance() {
        return assetRepository.findAssetsRequiringMaintenance();
    }

    /**
     * Get assets with expired warranty
     */
    public List<Asset> getAssetsWithExpiredWarranty() {
        return assetRepository.findAssetsWithExpiredWarranty();
    }
}
