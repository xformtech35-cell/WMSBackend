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
import com.warehouse.wms.dto.response.ItemStockSummary;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.InventoryStock;
import com.warehouse.wms.entity.Item;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.exception.InvalidOperationException;
import com.warehouse.wms.exception.ResourceNotFoundException;
import com.warehouse.wms.mapper.WarehouseMapper;
import com.warehouse.wms.repository.AisleRepository;
import com.warehouse.wms.repository.BinRepository;
import com.warehouse.wms.repository.InventoryStockRepository;
import com.warehouse.wms.repository.ItemRepository;
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
    private final InventoryStockRepository inventoryStockRepository;
    private final ZoneRepository zoneRepository;
    private final ItemRepository itemRepository;

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

        if (!request.getWarehouseId().equals(warehouse.getWarehouseId()) &&
            warehouseRepository.existsByWarehouseId(request.getWarehouseId())) {
            throw new InvalidOperationException("Warehouse ID already exists: " + request.getWarehouseId());
        }

        warehouseMapper.updateEntity(warehouse, request);
        
        if(warehouse.getBarcodeImage() == null) {
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

    private List<Warehouse> findWarehousesWithFilters(WarehouseFilterRequest filter, Pageable pageable) {
        if (filter == null) {
            return warehouseRepository.findAll(pageable).getContent();
        }
        
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

    // ====== HIERARCHY CONVERSION METHODS ======

    private WarehouseResponse convertToFullHierarchyResponse(Warehouse warehouse) {
        List<Zone> zones = zoneRepository.findByWarehouseIdWithFullHierarchy(warehouse.getId());
        List<ZoneResponse> zoneResponses = zones.stream()
                .map(this::convertZoneWithHierarchy)
                .collect(Collectors.toList());
        
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
        
        response.setStockSummary(calculateWarehouseStockSummary(warehouse));
        return response;
    }

    private ZoneResponse convertZoneWithHierarchy(Zone zone) {
        List<Aisle> aisles = aisleRepository.findByZoneIdWithFullHierarchy(zone.getId());
        List<AisleResponse> aisleResponses = aisles.stream()
                .map(this::convertAisleWithHierarchy)
                .collect(Collectors.toList());
        
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
        
        response.setStockSummary(calculateZoneStockSummary(zone));
        return response;
    }

    private AisleResponse convertAisleWithHierarchy(Aisle aisle) {
        List<Rack> racks = rackRepository.findByAisleIdWithFullHierarchy(aisle.getId());
        List<RackResponse> rackResponses = racks.stream()
                .map(this::convertRackWithHierarchy)
                .collect(Collectors.toList());
        
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
        
        response.setStockSummary(calculateAisleStockSummary(aisle));
        return response;
    }

    private RackResponse convertRackWithHierarchy(Rack rack) {
        List<Level> levels = levelRepository.findByRackIdWithFullHierarchy(rack.getId());
        List<LevelResponse> levelResponses = levels.stream()
                .map(this::convertLevelWithHierarchy)
                .collect(Collectors.toList());
        
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
        
        response.setStockSummary(calculateRackStockSummary(rack));
        return response;
    }

    private LevelResponse convertLevelWithHierarchy(Level level) {
        List<Bin> bins = binRepository.findByLevelId(level.getId());
        List<BinResponse> binResponses = bins.stream()
                .map(this::convertBinWithMinimalInfo)
                .collect(Collectors.toList());
        
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
                .stockSummary(calculateBinStockSummary(bin))
                .build();
    }

    // ====== REAL STOCK SUMMARY CALCULATION METHODS ======

    private StockAvailabilitySummary calculateWarehouseStockSummary(Warehouse warehouse) {
        try {
            String warehouseId = warehouse.getWarehouseId();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByWarehouseId(warehouseId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByWarehouseId(warehouseId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByWarehouseId(warehouseId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByWarehouseId(warehouseId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByWarehouseId(warehouseId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .limit(10)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, warehouse.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                warehouse.getMaxCapacity(), warehouse.getMinCapacity(),
                utilization, "WAREHOUSE", warehouse.getWarehouseId(),
                warehouse.getName(), uniqueItems, itemSummaries
            );
        } catch (Exception e) {
            log.error("Error calculating warehouse stock summary for: {}", warehouse.getWarehouseId(), e);
            return getEmptyStockSummary();
        }
    }

    private StockAvailabilitySummary calculateZoneStockSummary(Zone zone) {
        try {
            String zoneId = zone.getZoneId();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByZone(zoneId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByZone(zoneId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByZone(zoneId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByZone(zoneId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByZone(zoneId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .limit(10)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, zone.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                zone.getMaxCapacity(), zone.getMinCapacity(),
                utilization, "ZONE", zone.getZoneId(),
                zone.getName(), uniqueItems, itemSummaries
            );
        } catch (Exception e) {
            log.error("Error calculating zone stock summary for: {}", zone.getZoneId(), e);
            return getEmptyStockSummary();
        }
    }

    private StockAvailabilitySummary calculateAisleStockSummary(Aisle aisle) {
        try {
            String aisleId = aisle.getAisleId();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByAisle(aisleId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByAisle(aisleId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByAisle(aisleId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByAisle(aisleId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByAisle(aisleId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .limit(10)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, aisle.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                aisle.getMaxCapacity(), aisle.getMinCapacity(),
                utilization, "AISLE", aisle.getAisleId(),
                aisle.getName(), uniqueItems, itemSummaries
            );
        } catch (Exception e) {
            log.error("Error calculating aisle stock summary for: {}", aisle.getAisleId(), e);
            return getEmptyStockSummary();
        }
    }

    private StockAvailabilitySummary calculateRackStockSummary(Rack rack) {
        try {
            String rackId = rack.getRackId();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByRack(rackId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByRack(rackId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByRack(rackId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByRack(rackId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByRack(rackId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .limit(10)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, rack.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                rack.getMaxCapacity(), rack.getMinCapacity(),
                utilization, "RACK", rack.getRackId(),
                rack.getName(), uniqueItems, itemSummaries
            );
        } catch (Exception e) {
            log.error("Error calculating rack stock summary for: {}", rack.getRackId(), e);
            return getEmptyStockSummary();
        }
    }

    private StockAvailabilitySummary calculateLevelStockSummary(Level level) {
        try {
            String levelId = level.getLevelId();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByLevel(levelId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByLevel(levelId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByLevel(levelId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByLevel(levelId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByLevel(levelId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .limit(10)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, level.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                level.getMaxCapacity(), level.getMinCapacity(),
                utilization, "LEVEL", level.getLevelId(),
                level.getName(), uniqueItems, itemSummaries
            );
        } catch (Exception e) {
            log.error("Error calculating level stock summary for: {}", level.getLevelId(), e);
            return getEmptyStockSummary();
        }
    }

    private StockAvailabilitySummary calculateBinStockSummary(Bin bin) {
        try {
            String binId = bin.getBarcode();
            
            Long totalItems = inventoryStockRepository.getTotalQuantityByBinId(binId);
            Long availableItems = inventoryStockRepository.getAvailableQuantityByBinId(binId);
            Long reservedItems = inventoryStockRepository.getReservedQuantityByBinId(binId);
            
            totalItems = totalItems != null ? totalItems : 0L;
            availableItems = availableItems != null ? availableItems : 0L;
            reservedItems = reservedItems != null ? reservedItems : 0L;
            
            Integer uniqueItems = inventoryStockRepository.countUniqueItemsByBinId(binId);
            uniqueItems = uniqueItems != null ? uniqueItems : 0;
            
            List<InventoryStock> stockItems = inventoryStockRepository.findItemsWithStockByBinId(binId);
            List<ItemStockSummary> itemSummaries = stockItems.stream()
                    .map(this::convertToItemStockSummary)
                    .collect(Collectors.toList());
            
            Double utilization = calculateUtilization(totalItems, bin.getMaxCapacity());
            
            return buildFullStockSummary(
                totalItems, availableItems, reservedItems,
                bin.getMaxCapacity(), bin.getMinCapacity(),
                utilization, "BIN", bin.getBarcode(),
                bin.getBarcode(), uniqueItems, itemSummaries
            );
                    
        } catch (Exception e) {
            log.error("Error calculating bin stock summary for bin: {}", bin.getBarcode(), e);
            return getEmptyStockSummary();
        }
    }

    // ====== HELPER METHODS ======

    private StockAvailabilitySummary buildFullStockSummary(
            Long totalItems, Long availableItems, Long reservedItems,
            Integer maxCapacity, Integer minCapacity,
            Double utilization, String locationLevel, String locationId,
            String locationName, Integer uniqueItems, List<ItemStockSummary> items) {
        
        return StockAvailabilitySummary.builder()
                // Stock Counts
                .totalItems(totalItems)
                .availableItems(availableItems)
                .occupiedItems(totalItems - availableItems)
                .totalQuantity(totalItems.intValue())
                .stockin(availableItems.intValue())
                .reservedQuantity(reservedItems.intValue())
                .inTransitQuantity(0)
                
                // Capacity Information
                .maxCapacity(maxCapacity)
                .minCapacity(minCapacity)
                .utilizationPercentage(Math.min(utilization, 100.0))
                .availableSlots(maxCapacity != null ? maxCapacity - totalItems.intValue() : null)
                .occupiedSlots(totalItems.intValue())
                
                // Stock Status
                .hasStock(totalItems > 0)
                .isFull(maxCapacity != null && totalItems >= maxCapacity)
                .isAvailable(availableItems > 0)
                .isLowStock(utilization < 20.0 && totalItems > 0)
                .isHighStock(utilization > 80.0)
                .stockStatus(determineStockStatus(totalItems, utilization, maxCapacity))
                
                // Location Information
                .locationPath(locationId)
                .locationLevel(locationLevel)
                
                // Item Information
                .uniqueItemsCount(uniqueItems)
                .items(items != null && !items.isEmpty() ? items : null)
                
                // Summary
                .totalBinsUsed(totalItems > 0 ? 1 : 0)
                .totalBinsAvailable(1)
                .stockTurnoverRate(0.0)
                
                .build();
    }

    private StockAvailabilitySummary getEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                .totalItems(0L)
                .availableItems(0L)
                .occupiedItems(0L)
                .totalQuantity(0)
                .stockin(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .utilizationPercentage(0.0)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)
                .isHighStock(false)
                .stockStatus("EMPTY")
                .uniqueItemsCount(0)
                .totalBinsUsed(0)
                .totalBinsAvailable(0)
                .stockTurnoverRate(0.0)
                .build();
    }

    private String determineStockStatus(Long totalItems, Double utilization, Integer maxCapacity) {
        if (totalItems == null || totalItems <= 0) {
            return "EMPTY";
        }
        if (maxCapacity != null && maxCapacity > 0 && totalItems >= maxCapacity) {
            return "FULL";
        }
        if (utilization != null) {
            if (utilization > 80.0) return "HIGH";
            if (utilization < 20.0) return "LOW";
        }
        return "NORMAL";
    }

    private Double calculateUtilization(Long totalItems, Integer maxCapacity) {
        if (maxCapacity == null || maxCapacity <= 0) {
            return 0.0;
        }
        if (totalItems == null || totalItems <= 0) {
            return 0.0;
        }
        return (totalItems.doubleValue() / maxCapacity) * 100;
    }

    private ItemStockSummary convertToItemStockSummary(InventoryStock stock) {
        if (stock == null) {
            return null;
        }
        
        // Fetch item details for unit price
        Double unitPrice = null;
        Double totalValue = null;
        try {
            Item item = itemRepository.findByItemCode(stock.getItemCode()).orElse(null);
            if (item != null) {
                unitPrice = item.getUnitPrice();
                if (unitPrice != null && stock.getQuantity() != null) {
                    totalValue = unitPrice * stock.getQuantity();
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch item details for: {}", stock.getItemCode());
        }
        
        return ItemStockSummary.builder()
                .itemCode(stock.getItemCode())
                .itemName(stock.getItemName())
                .quantity(stock.getQuantity())
                .availableQuantity(stock.getAvailableQuantity())
                .reservedQuantity(stock.getReservedQuantity())
                .uom(stock.getUom())
                .batchNumber(stock.getBatchNumber())
                .expiryDate(stock.getExpiryDate())
                .mfgDate(stock.getMfgDate())
                .unitPrice(unitPrice)
                .totalValue(totalValue)
                .build();
    }
}