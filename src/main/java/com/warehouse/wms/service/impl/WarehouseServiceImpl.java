// ====== FILE: src/main/java/com/warehouse/wms/service/impl/WarehouseServiceImpl.java ======
package com.warehouse.wms.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.warehouse.wms.dto.request.WarehouseFilterRequest;
import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.WarehouseMapper;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.LevelRepository;
import com.warehouse.wms.repository.RackRepository;
import com.warehouse.wms.repository.WarehouseRepository;
import com.warehouse.wms.repository.ZoneRepository;
import com.warehouse.wms.service.WarehouseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;
    private final BarcodeServiceImpl barcodeServiceImpl;
    
    private final BinRepository binRepository;
    private final AisleRepository aisleRepository;
    private final RackRepository rackRepository;
    private final LevelRepository levelRepository;

    private final ZoneRepository zoneRepository;


    @Override
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        log.info("Creating warehouse: {}", request.getWarehouseId());

        if (warehouseRepository.existsByWarehouseId(request.getWarehouseId())) {
            throw new InvalidOperationException("Warehouse ID already exists: " + request.getWarehouseId());
        }

        Warehouse warehouse = warehouseMapper.toEntity(request);
        warehouse.setTotalZones(0);

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        log.info("✅ Warehouse created: {}", savedWarehouse.getWarehouseId());
        
        barcodeServiceImpl.generateWarehouseBarcode(savedWarehouse.getWarehouseId());
        

        return warehouseMapper.toResponse(savedWarehouse);
    }

    @Override
    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    public WarehouseResponse getWarehouseByWarehouseId(String warehouseId) {
        Warehouse warehouse = warehouseRepository.findByWarehouseId(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + warehouseId));
        return warehouseMapper.toResponse(warehouse);
    }

    @Override
    public Page<WarehouseResponse> getAllWarehouses(Pageable pageable, String search) {
        if (search != null && !search.isEmpty()) {
            return warehouseRepository.searchWarehouses(search, pageable)
                    .map(warehouseMapper::toResponse);
        }
        return warehouseRepository.findAll(pageable)
                .map(warehouseMapper::toResponse);
    }

    @Override
    public List<WarehouseResponse> getActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue()
                .stream()
                .map(warehouseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        log.info("Updating warehouse: {}", id);

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        // Check uniqueness if warehouseId is changed
        if (!request.getWarehouseId().equals(warehouse.getWarehouseId()) &&
            warehouseRepository.existsByWarehouseId(request.getWarehouseId())) {
            throw new InvalidOperationException("Warehouse ID already exists: " + request.getWarehouseId());
        }

        warehouseMapper.updateEntity(warehouse, request);
        
        if(warehouse.getBarcodeImage()==null)
        {
            barcodeServiceImpl.generateWarehouseBarcode(warehouse.getWarehouseId());

        }
        
        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        log.info("✅ Warehouse updated: {}", updatedWarehouse.getWarehouseId());

        return warehouseMapper.toResponse(updatedWarehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        log.info("Deleting warehouse: {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        // Soft delete
        warehouse.setIsActive(false);
        warehouseRepository.save(warehouse);
        log.info("✅ Warehouse deactivated: {}", id);
    }

    @Override
    public void toggleWarehouseStatus(Long id, Boolean isActive) {
        log.info("Toggling warehouse status: {} to {}", id, isActive);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with ID: " + id));

        warehouse.setIsActive(isActive);
        warehouseRepository.save(warehouse);
        log.info("✅ Warehouse status updated: {} -> {}", id, isActive);
    }
    
    
    @Override
    public Page<WarehouseResponse> getWarehousesWithFullHierarchy(WarehouseFilterRequest filter, Pageable pageable) {
        log.debug("Fetching warehouses with filters: {}", filter);
        
        // Build the query based on filters
        List<Warehouse> warehouses = findWarehousesWithFilters(filter, pageable);
        Long total = countWarehousesWithFilters(filter);
        
        List<WarehouseResponse> warehouseResponses = warehouses.stream()
                .map(this::convertToFullHierarchyResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(warehouseResponses, pageable, total);
    }

    @Override
    public WarehouseResponse getWarehouseWithFullHierarchy(Long warehouseId) {
        log.debug("Fetching warehouse with full hierarchy: {}", warehouseId);
        
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with id: " + warehouseId));
        
        return convertToFullHierarchyResponse(warehouse);
    }

//    @Override
//    public Page<WarehouseResponse> searchWarehouses(String searchTerm, Pageable pageable) {
//        log.debug("Searching warehouses with term: {}", searchTerm);
//        
//        List<Warehouse> warehouses = warehouseRepository.searchWarehouses(searchTerm, pageable);
//        Long total = warehouseRepository.countSearchWarehouses(searchTerm);
//        
//        List<WarehouseResponse> warehouseResponses = warehouses.stream()
//                .map(this::convertToFullHierarchyResponse)
//                .collect(Collectors.toList());
//        
//        return new PageImpl<>(warehouseResponses, pageable, total);
//    }

    private List<Warehouse> findWarehousesWithFilters(WarehouseFilterRequest filter, Pageable pageable) {
        // Build dynamic query with specifications
        // This is a simplified version - you can use JPA Specifications for more complex queries
        
        if (filter == null) {
            return warehouseRepository.findAll(pageable).getContent();
        }
        
        // Example: Search by warehouse name or ID
        if (StringUtils.hasText(filter.getName())) {
            return warehouseRepository.findByNameContainingIgnoreCase(filter.getName(), pageable);
        }
        
        if (StringUtils.hasText(filter.getWarehouseId())) {
            return warehouseRepository.findByWarehouseIdContainingIgnoreCase(filter.getWarehouseId(), pageable);
        }
        
        if (filter.getIsActive() != null) {
            return warehouseRepository.findByIsActive(filter.getIsActive(), pageable);
        }
        
        return warehouseRepository.findAll(pageable).getContent();
    }

    private Long countWarehousesWithFilters(WarehouseFilterRequest filter) {
        if (filter == null) {
            return warehouseRepository.count();
        }
        
        if (StringUtils.hasText(filter.getName())) {
            return warehouseRepository.countByNameContainingIgnoreCase(filter.getName());
        }
        
        if (StringUtils.hasText(filter.getWarehouseId())) {
            return warehouseRepository.countByWarehouseIdContainingIgnoreCase(filter.getWarehouseId());
        }
        
        if (filter.getIsActive() != null) {
            return warehouseRepository.countByIsActive(filter.getIsActive());
        }
        
        return warehouseRepository.count();
    }

    private WarehouseResponse convertToFullHierarchyResponse(Warehouse warehouse) {
        // 1. Get zones with their hierarchy
        List<Zone> zones = zoneRepository.findByWarehouseIdWithFullHierarchy(warehouse.getId());
        List<ZoneResponse> zoneResponses = zones.stream()
                .map(this::convertZoneWithHierarchy)
                .collect(Collectors.toList());
        
        // 2. Build warehouse response with zones
        WarehouseResponse response = WarehouseResponse.builder()
                .id(warehouse.getId())
                .warehouseId(warehouse.getWarehouseId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .address(warehouse.getAddress())
                .contactPerson(warehouse.getContactPerson())
                .contactPhone(warehouse.getContactPhone())
                .contactEmail(warehouse.getContactEmail())
                .isActive(warehouse.getIsActive())
                .capacity(warehouse.getCapacity())
                .totalZones(warehouse.getTotalZones())
                .remarks(warehouse.getRemarks())
                .createdBy(warehouse.getCreatedBy())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .maxCapacity(warehouse.getMaxCapacity())
                .minCapacity(warehouse.getMinCapacity())
                .capacityUnit(warehouse.getCapacityUnit())
                .zones(zoneResponses)
                .build();
        
        // Calculate stock summary
        response.setStockSummary(calculateWarehouseStockSummary(warehouse));
        
        return response;
    }

    private ZoneResponse convertZoneWithHierarchy(Zone zone) {
        // 1. Get aisles with their hierarchy
        List<Aisle> aisles = aisleRepository.findByZoneIdWithFullHierarchy(zone.getId());
        List<AisleResponse> aisleResponses = aisles.stream()
                .map(this::convertAisleWithHierarchy)
                .collect(Collectors.toList());
        
        // 2. Build zone response with minimal warehouse info (break circular reference)
        WarehouseResponse minimalWarehouse = WarehouseResponse.builder()
                .id(zone.getWarehouse().getId())
                .warehouseId(zone.getWarehouse().getWarehouseId())
                .name(zone.getWarehouse().getName())
                .location(zone.getWarehouse().getLocation())
                .build();
        
        ZoneResponse response = ZoneResponse.builder()
                .id(zone.getId())
                .zoneId(zone.getZoneId())
                .name(zone.getName())
                .description(zone.getDescription())
                .zoneType(zone.getZoneType())
                .isActive(zone.getIsActive())
                .priority(zone.getPriority())
                .totalAisles(zone.getTotalAisles())
                .remarks(zone.getRemarks())
                .createdBy(zone.getCreatedBy())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .maxCapacity(zone.getMaxCapacity())
                .minCapacity(zone.getMinCapacity())
                .capacityUnit(zone.getCapacityUnit())
                .warehouse(minimalWarehouse)
                .aisles(aisleResponses)
                .build();
        
        // Calculate stock summary for zone
        response.setStockSummary(calculateZoneStockSummary(zone));
        
        return response;
    }

    private AisleResponse convertAisleWithHierarchy(Aisle aisle) {
        // 1. Get racks with their hierarchy
        List<Rack> racks = rackRepository.findByAisleIdWithFullHierarchy(aisle.getId());
        List<RackResponse> rackResponses = racks.stream()
                .map(this::convertRackWithHierarchy)
                .collect(Collectors.toList());
        
        // 2. Build minimal zone info (break circular reference)
        ZoneResponse minimalZone = ZoneResponse.builder()
                .id(aisle.getZone().getId())
                .zoneId(aisle.getZone().getZoneId())
                .name(aisle.getZone().getName())
                .build();
        
        AisleResponse response = AisleResponse.builder()
                .id(aisle.getId())
                .aisleId(aisle.getAisleId())
                .name(aisle.getName())
                .description(aisle.getDescription())
                .isActive(aisle.getIsActive())
                .width(aisle.getWidth())
                .length(aisle.getLength())
                .totalRacks(aisle.getTotalRacks())
                .unit(aisle.getUnit())
                .remarks(aisle.getRemarks())
                .createdBy(aisle.getCreatedBy())
                .createdAt(aisle.getCreatedAt())
                .updatedAt(aisle.getUpdatedAt())
                .maxCapacity(aisle.getMaxCapacity())
                .minCapacity(aisle.getMinCapacity())
                .capacityUnit(aisle.getCapacityUnit())
                .zone(minimalZone)
                .racks(rackResponses)
                .build();
        
        // Calculate stock summary for aisle
        response.setStockSummary(calculateAisleStockSummary(aisle));
        
        return response;
    }

    private RackResponse convertRackWithHierarchy(Rack rack) {
        // 1. Get levels with their hierarchy (bins)
        List<Level> levels = levelRepository.findByRackIdWithFullHierarchy(rack.getId());
        List<LevelResponse> levelResponses = levels.stream()
                .map(this::convertLevelWithHierarchy)
                .collect(Collectors.toList());
        
        // 2. Build minimal aisle info (break circular reference)
        AisleResponse minimalAisle = AisleResponse.builder()
                .id(rack.getAisle().getId())
                .aisleId(rack.getAisle().getAisleId())
                .name(rack.getAisle().getName())
                .build();
        
        RackResponse response = RackResponse.builder()
                .id(rack.getId())
                .rackId(rack.getRackId())
                .name(rack.getName())
                .description(rack.getDescription())
                .isActive(rack.getIsActive())
                .height(rack.getHeight())
                .width(rack.getWidth())
                .unit(rack.getUnit())
                .depth(rack.getDepth())
                .totalShelves(rack.getTotalShelves())
                .remarks(rack.getRemarks())
                .createdBy(rack.getCreatedBy())
                .createdAt(rack.getCreatedAt())
                .updatedAt(rack.getUpdatedAt())
                .maxCapacity(rack.getMaxCapacity())
                .minCapacity(rack.getMinCapacity())
                .capacityUnit(rack.getCapacityUnit())
                .aisle(minimalAisle)
                .levels(levelResponses)
                .build();
        
        // Calculate stock summary for rack
        response.setStockSummary(calculateRackStockSummary(rack));
        
        return response;
    }

    private LevelResponse convertLevelWithHierarchy(Level level) {
        // 1. Get bins
        List<Bin> bins = binRepository.findByLevelId(level.getId());
        List<BinResponse> binResponses = bins.stream()
                .map(this::convertBinWithMinimalInfo)
                .collect(Collectors.toList());
        
        // 2. Build minimal rack info (break circular reference)
        RackResponse minimalRack = RackResponse.builder()
                .id(level.getRack().getId())
                .rackId(level.getRack().getRackId())
                .name(level.getRack().getName())
                .build();
        
        LevelResponse response = LevelResponse.builder()
                .id(level.getId())
                .levelId(level.getLevelId())
                .name(level.getName())
                .description(level.getDescription())
                .unit(level.getUnit())
                .levelNumber(level.getLevelNumber())
                .heightCm(level.getHeightCm())
                .maxWeightKg(level.getMaxWeightKg())
                .maxItems(level.getMaxItems())
                .isActive(level.getIsActive())
                .remarks(level.getRemarks())
                .createdBy(level.getCreatedBy())
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .maxCapacity(level.getMaxCapacity())
                .minCapacity(level.getMinCapacity())
                .capacityUnit(level.getCapacityUnit())
                .rack(minimalRack)
                .bins(binResponses)
                .build();
        
        // Calculate stock summary for level
        response.setStockSummary(calculateLevelStockSummary(level));
        
        return response;
    }

    private BinResponse convertBinWithMinimalInfo(Bin bin) {
        return BinResponse.builder()
                .id(bin.getId())
                .barcode(bin.getBarcode())
                .lengthCm(bin.getLengthCm())
                .widthCm(bin.getWidthCm())
                .heightCm(bin.getHeightCm())
                .volumeCm3(bin.getVolumeCm3())
                .maxWeightG(bin.getMaxWeightG())
                .occupiedVolumeCm3(bin.getOccupiedVolumeCm3())
                .occupiedWeightG(bin.getOccupiedWeightG())
                .utilizationPercentage(bin.getUtilizationPercentage())
                .status(bin.getStatus())
                .fullLocation(bin.getFullLocation())
                .maxCapacity(bin.getMaxCapacity())
                .minCapacity(bin.getMinCapacity())
                .capacityUnit(bin.getCapacityUnit())
                .unit(bin.getUnit())
                .isActive(bin.getIsActive())
                .remarks(bin.getRemarks())
                .createdBy(bin.getCreatedBy())
                .createdAt(bin.getCreatedAt())
                .updatedAt(bin.getUpdatedAt())
                .levelId(bin.getLevel() != null ? bin.getLevel().getId() : null)
                .levelName(bin.getLevel() != null ? bin.getLevel().getName() : null)
                .rackId(bin.getLevel() != null && bin.getLevel().getRack() != null ? 
                        bin.getLevel().getRack().getId() : null)
                .rackName(bin.getLevel() != null && bin.getLevel().getRack() != null ? 
                        bin.getLevel().getRack().getName() : null)
                .stockSummary(calculatebinStockSummary(bin))  // ✅ Fixed: Use .stockSummary() not .setStockSummary()
                .build();
    }

    // Stock summary calculation methods
    private StockAvailabilitySummary calculateWarehouseStockSummary(Warehouse warehouse) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(1000L)
                .availableItems(750L)
                .occupiedItems(250L)
                .utilizationPercentage(25.0)
                .build();
    }

    private StockAvailabilitySummary calculateZoneStockSummary(Zone zone) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(500L)
                .availableItems(400L)
                .occupiedItems(100L)
                .utilizationPercentage(20.0)
                .build();
    }

    private StockAvailabilitySummary calculateAisleStockSummary(Aisle aisle) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(200L)
                .availableItems(150L)
                .occupiedItems(50L)
                .utilizationPercentage(25.0)
                .build();
    }

    private StockAvailabilitySummary calculateRackStockSummary(Rack rack) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(100L)
                .availableItems(80L)
                .occupiedItems(20L)
                .utilizationPercentage(20.0)
                .build();
    }

    private StockAvailabilitySummary calculateLevelStockSummary(Level level) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(50L)
                .availableItems(40L)
                .occupiedItems(10L)
                .utilizationPercentage(20.0)
                .build();
    }
    
    
    private StockAvailabilitySummary calculatebinStockSummary(Bin bin) {
        // Implementation depends on your business logic
        return StockAvailabilitySummary.builder()
                .totalItems(50L)
                .availableItems(40L)
                .occupiedItems(10L)
                .utilizationPercentage(20.0)
                .build();
    }
    
    
}