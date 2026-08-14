// ====== FILE: src/main/java/com/warehouse/wms/mapper/LevelMapper.java ======
package com.warehouse.wms.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.request.LevelRequest;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.service.StockAvailabilityService;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LevelMapper {

    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    @Autowired
    protected WarehouseMapper warehouseMapper;

    // ❌ REMOVE: @Autowired protected RackMapper rackMapper;
    // ❌ REMOVE: @Autowired protected BinMapper binMapper;

    // ====== TO RESPONSE ======
    
    @Mapping(target = "rack", expression = "java(mapRack(level.getRack()))")
    @Mapping(target = "bins", ignore = true)  // Ignore to avoid circular reference
    @Mapping(target = "stockSummary", expression = "java(getLevelStockSummary(level))")
    public abstract LevelResponse toResponse(Level level);

    // ====== TO ENTITY ======
    
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Level toEntity(LevelRequest request);

    // ====== UPDATE ENTITY ======
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget Level level, LevelRequest request);

    // ====== Custom Mapping ======

    // ✅ Manual mapping for Rack - NO RackMapper dependency
    protected RackResponse mapRack(Rack rack) {
        if (rack == null) {
            return null;
        }
        
        // Get rack stock summary
        StockAvailabilitySummary rackStockSummary = null;
        try {
            if (rack.getAisle() != null && rack.getAisle().getZone() != null &&
                rack.getAisle().getZone().getWarehouse() != null) {
                rackStockSummary = stockAvailabilityService.getRackStockSummary(
                        rack.getAisle().getZone().getWarehouse().getWarehouseId(),
                        rack.getAisle().getZone().getZoneId(),
                        rack.getAisle().getAisleId(),
                        rack.getRackId()
                );
            }
        } catch (Exception e) {
            rackStockSummary = buildEmptyStockSummary();
        }
        
        return RackResponse.builder()
                .id(rack.getId())
                .rackId(rack.getRackId())
                .name(rack.getName())
                .description(rack.getDescription())
                .isActive(rack.getIsActive())
                .height(rack.getHeight())
                .width(rack.getWidth())
                .depth(rack.getDepth())
                .totalShelves(rack.getTotalShelves())
                .unit(rack.getUnit())
                .remarks(rack.getRemarks())
                .createdBy(rack.getCreatedBy())
                .createdAt(rack.getCreatedAt())
                .updatedAt(rack.getUpdatedAt())
                .barcodeData(rack.getBarcodeData())
                .barcodeImage(rack.getBarcodeImage())
                .barcodeFormat(rack.getBarcodeFormat())
                .stockSummary(rackStockSummary)
                .aisle(mapAisle(rack.getAisle()))
                .levels(null)  // Prevent circular reference
                .compartments(null)
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

    protected StockAvailabilitySummary getLevelStockSummary(Level level) {
        if (level == null) {
            return buildEmptyStockSummary();
        }

        try {
            Rack rack = level.getRack();
            if (rack == null) {
                return buildEmptyStockSummary();
            }

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

            StockAvailabilitySummary summary = stockAvailabilityService.getLevelStockSummary(
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId(),
                    rack.getRackId(),
                    level.getLevelId()
            );
            
            if (summary == null) {
                return buildEmptyStockSummary();
            }
            
            return summary;
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======

    protected StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                // Stock counts
                .totalQuantity(0)
                .stockin(0)                    // ✅ Changed from availableQuantity
                .reservedQuantity(0)
                .inTransitQuantity(0)
                
                // Capacity
                .maxCapacity(null)             // ✅ Use null instead of 0
                .minCapacity(null)             // ✅ Added minCapacity
                .utilizationPercentage(0.0)
                .availableSlots(0)             // ✅ Added availableSlots
                .occupiedSlots(0)              // ✅ Added occupiedSlots
                
                // Stock status
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)             // ✅ Added isLowStock
                .isHighStock(false)            // ✅ Added isHighStock
                .stockStatus("EMPTY")          // ✅ Added stockStatus
                
                // Location
                .locationPath(null)
                .locationLevel(null)
                
                // Items
                .uniqueItemsCount(0)
                .items(new ArrayList<>())
                
                // Timestamps
                .lastPutawayDate(null)
                .lastPickDate(null)
                
                // Summary
                .totalBinsUsed(0)              // ✅ Added totalBinsUsed
                .totalBinsAvailable(0)         // ✅ Added totalBinsAvailable
                .stockTurnoverRate(0.0)        // ✅ Added stockTurnoverRate
                
                .build();
    }

    public List<LevelResponse> toResponseList(List<Level> levels) {
        if (levels == null) {
            return null;
        }
        return levels.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}