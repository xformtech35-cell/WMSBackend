// ====== FILE: src/main/java/com/warehouse/wms/mapper/RackMapper.java ======
package com.warehouse.wms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.service.StockAvailabilityService;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RackMapper {
    
    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    @Autowired
    protected WarehouseMapper warehouseMapper;

    // ❌ REMOVE: @Autowired protected AisleMapper aisleMapper;
    // ❌ REMOVE: @Autowired protected LevelMapper levelMapper;

    // ====== Main Mapping ======
    
    @Mapping(target = "aisle", expression = "java(mapAisle(rack.getAisle()))")
    @Mapping(target = "levels", expression = "java(mapLevels(rack.getLevels()))")
    @Mapping(target = "stockSummary", expression = "java(getRackStockSummary(rack))")
    @Mapping(target = "compartments", ignore = true)
    public abstract RackResponse toResponse(Rack rack);

    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "levels", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    public abstract Rack toEntity(RackRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "levels", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    public abstract void updateEntity(@MappingTarget Rack rack, RackRequest request);

    // ====== Custom Mapping ======

    // ✅ Manual mapping for Levels - NO LevelMapper dependency
    protected List<LevelResponse> mapLevels(List<Level> levels) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }
        return levels.stream()
                .map(this::mapLevel)
                .collect(Collectors.toList());
    }

    // ✅ Manual mapping for Level
    protected LevelResponse mapLevel(Level level) {
        if (level == null) {
            return null;
        }
        
        // Get level stock summary
        StockAvailabilitySummary levelStockSummary = null;
        try {
            if (level.getRack() != null && level.getRack().getAisle() != null &&
                level.getRack().getAisle().getZone() != null &&
                level.getRack().getAisle().getZone().getWarehouse() != null) {
                levelStockSummary = stockAvailabilityService.getLevelStockSummary(
                        level.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                        level.getRack().getAisle().getZone().getZoneId(),
                        level.getRack().getAisle().getAisleId(),
                        level.getRack().getRackId(),
                        level.getLevelId()
                );
            }
        } catch (Exception e) {
            levelStockSummary = buildEmptyStockSummary();
        }
        
        return LevelResponse.builder()
                .id(level.getId())
                .levelId(level.getLevelId())
                .name(level.getName())
                .description(level.getDescription())
                .levelNumber(level.getLevelNumber())
                .heightCm(level.getHeightCm())
                .maxWeightKg(level.getMaxWeightKg())
                .maxItems(level.getMaxItems())
                .isActive(level.getIsActive())
                .remarks(level.getRemarks())
                .unit(level.getUnit())
                .createdBy(level.getCreatedBy())
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .barcodeData(level.getBarcodeData())
                .barcodeImage(level.getBarcodeImage())
                .barcodeFormat(level.getBarcodeFormat())
                .stockSummary(levelStockSummary)
                .rack(null)  // Prevent circular reference
                .bins(null)  // Prevent circular reference
                .build();
    }

    // ✅ Manual mapping for Aisle
    protected AisleResponse mapAisle(Aisle aisle) {
        if (aisle == null) {
            return null;
        }
        
        // Get aisle stock summary
        StockAvailabilitySummary aisleStockSummary = null;
        try {
            if (aisle.getZone() != null && aisle.getZone().getWarehouse() != null) {
                aisleStockSummary = stockAvailabilityService.getAisleStockSummary(
                        aisle.getZone().getWarehouse().getWarehouseId(),
                        aisle.getZone().getZoneId(),
                        aisle.getAisleId()
                );
            }
        } catch (Exception e) {
            aisleStockSummary = buildEmptyStockSummary();
        }
        
        return AisleResponse.builder()
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
                .barcodeData(aisle.getBarcodeData())
                .barcodeImage(aisle.getBarcodeImage())
                .barcodeFormat(aisle.getBarcodeFormat())
                .stockSummary(aisleStockSummary)
                .zone(mapZone(aisle.getZone()))
                .racks(null)  // Prevent circular reference
                .build();
    }

    // ✅ Manual mapping for Zone
    protected ZoneResponse mapZone(Zone zone) {
        if (zone == null) {
            return null;
        }
        
        // Get zone stock summary
        StockAvailabilitySummary zoneStockSummary = null;
        try {
            if (zone.getWarehouse() != null) {
                zoneStockSummary = stockAvailabilityService.getZoneStockSummary(
                        zone.getWarehouse().getWarehouseId(),
                        zone.getZoneId()
                );
            }
        } catch (Exception e) {
            zoneStockSummary = buildEmptyStockSummary();
        }
        
        return ZoneResponse.builder()
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
                .barcodeData(zone.getBarcodeData())
                .barcodeImage(zone.getBarcodeImage())
                .barcodeFormat(zone.getBarcodeFormat())
                .stockSummary(zoneStockSummary)
                .warehouse(mapWarehouse(zone.getWarehouse()))
                .aisles(null)  // Prevent circular reference
                .build();
    }

    // ✅ Manual mapping for Warehouse
    protected WarehouseResponse mapWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        
        // Get warehouse stock summary
        StockAvailabilitySummary warehouseStockSummary = null;
        try {
            warehouseStockSummary = stockAvailabilityService.getWarehouseStockSummary(warehouse.getWarehouseId());
        } catch (Exception e) {
            warehouseStockSummary = buildEmptyStockSummary();
        }
        
        return WarehouseResponse.builder()
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
               
                .stockSummary(warehouseStockSummary)
                .zones(null)  // Prevent circular reference
                .build();
    }

    protected StockAvailabilitySummary getRackStockSummary(Rack rack) {
        if (rack == null) {
            return buildEmptyStockSummary();
        }

        try {
            Aisle aisle = rack.getAisle();
            if (aisle == null) {
                return buildEmptyStockSummary();
            }

            Zone zone = aisle.getZone();
            if (zone == null) {
                return buildEmptyStockSummary();
            }

            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) {
                return buildEmptyStockSummary();
            }

            StockAvailabilitySummary summary = stockAvailabilityService.getRackStockSummary(
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId(),
                    rack.getRackId()
            );
            
            if (summary == null) {
                return buildEmptyStockSummary();
            }
            
            return summary;
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

    protected StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                .totalQuantity(0)
                .availableQuantity(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .uniqueItemsCount(0)
//                .maxCapacity(0)
                .utilizationPercentage(0.0)
                .locationPath(null)
                .locationLevel(null)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .lastPutawayDate(null)
                .lastPickDate(null)
                .items(List.of())
                .build();
    }

    public List<RackResponse> toResponseList(List<Rack> racks) {
        if (racks == null) {
            return null;
        }
        return racks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}