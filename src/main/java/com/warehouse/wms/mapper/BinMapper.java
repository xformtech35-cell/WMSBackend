// ====== FILE: src/main/java/com/warehouse/wms/mapper/BinMapper.java ======
package com.warehouse.wms.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Level;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.service.StockAvailabilityService;

@Mapper(componentModel = "spring")
public abstract class BinMapper {

    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    @Autowired
    protected WarehouseMapper warehouseMapper;

    // ====== TO ENTITY ======
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "volumeCm3", ignore = true)
    @Mapping(target = "occupiedVolumeCm3", constant = "0")
    @Mapping(target = "occupiedWeightG", constant = "0")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "barcodeData", ignore = true)
    @Mapping(target = "barcodeImage", ignore = true)
    @Mapping(target = "barcodeFormat", ignore = true)
    @Mapping(target = "maxCapacity", source = "maxCapacity")
    @Mapping(target = "minCapacity", source = "minCapacity")
    @Mapping(target = "capacityUnit", source = "capacityUnit")
    public abstract Bin toEntity(BinCreateRequest request);

    // ====== TO RESPONSE ======
    
    @Mapping(target = "fullLocation", expression = "java(getFullLocation(bin))")
   
    @Mapping(target = "utilizationPercentage", expression = "java(calculateUtilization(bin))")
    @Mapping(target = "rackId", source = "rack.id")
    @Mapping(target = "rackName", source = "rack.name")
    @Mapping(target = "levelId", source = "level.id")
    @Mapping(target = "levelName", source = "level.name")
    @Mapping(target = "level", expression = "java(mapLevelWithoutBins(bin.getLevel()))")
    @Mapping(target = "stockSummary", expression = "java(getBinStockSummarySafe(bin))")
    public abstract BinResponse toResponse(Bin bin);

    // ====== Safe Stock Summary ======
    
    protected StockAvailabilitySummary getBinStockSummarySafe(Bin bin) {
        if (bin == null || bin.getBarcode() == null) {
            return buildEmptyStockSummary();
        }
        try {
            StockAvailabilitySummary summary = stockAvailabilityService.getBinStockSummary(bin.getBarcode());
            if (summary == null) {
                return buildEmptyStockSummary();
            }
            return summary;
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

    // ====== Custom Mappings ======

    /**
     * Map Level WITHOUT bins to prevent infinite recursion
     */
    protected LevelResponse mapLevelWithoutBins(Level level) {
        if (level == null) {
            return null;
        }
        
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
               
                .rack(mapRackWithoutLevels(level.getRack()))
                .bins(null)
                .build();
    }

    /**
     * Map Rack WITHOUT levels to prevent infinite recursion
     */
    protected RackResponse mapRackWithoutLevels(Rack rack) {
        if (rack == null) {
            return null;
        }
        
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
                
                .aisle(mapAisleWithoutRacks(rack.getAisle()))
                .levels(null)
                .compartments(null)
                .build();
    }

    /**
     * Map Aisle WITHOUT racks to prevent infinite recursion
     */
    protected AisleResponse mapAisleWithoutRacks(Aisle aisle) {
        if (aisle == null) {
            return null;
        }
        
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
               
                .zone(mapZoneWithoutAisles(aisle.getZone()))
                .racks(null)
                .build();
    }

    /**
     * Map Zone WITHOUT aisles to prevent infinite recursion
     */
    protected ZoneResponse mapZoneWithoutAisles(Zone zone) {
        if (zone == null) {
            return null;
        }
        
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
               
                .warehouse(mapWarehouseWithoutZones(zone.getWarehouse()))
                .aisles(null)
                .build();
    }

    /**
     * Map Warehouse WITHOUT zones to prevent infinite recursion
     */
    protected WarehouseResponse mapWarehouseWithoutZones(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        
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
               
                .zones(null)
                .build();
    }

    // ====== Helper Methods ======

    protected String getFullLocation(Bin bin) {
        if (bin == null) {
            return null;
        }
        try {
            Level level = bin.getLevel();
            if (level == null) return null;
            Rack rack = level.getRack();
            if (rack == null) return null;
            Aisle aisle = rack.getAisle();
            if (aisle == null) return null;
            Zone zone = aisle.getZone();
            if (zone == null) return null;
            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) return null;

            return String.format("%s-%s-%s-%s-%s-%s",
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId(),
                    rack.getRackId(),
                    level.getLevelId(),
                    bin.getBarcode());
        } catch (Exception e) {
            return null;
        }
    }

    protected BigDecimal getAvailableVolume(Bin bin) {
        if (bin == null || bin.getVolumeCm3() == null || bin.getOccupiedVolumeCm3() == null) {
            return BigDecimal.ZERO;
        }
        return bin.getVolumeCm3().subtract(bin.getOccupiedVolumeCm3());
    }

    protected BigDecimal getAvailableWeight(Bin bin) {
        if (bin == null || bin.getMaxWeightG() == null || bin.getOccupiedWeightG() == null) {
            return BigDecimal.ZERO;
        }
        return bin.getMaxWeightG().subtract(bin.getOccupiedWeightG());
    }

    protected BigDecimal calculateUtilization(Bin bin) {
        if (bin == null || bin.getVolumeCm3() == null || bin.getOccupiedVolumeCm3() == null ||
            bin.getVolumeCm3().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return bin.getOccupiedVolumeCm3()
                .multiply(BigDecimal.valueOf(100))
                .divide(bin.getVolumeCm3(), 2, RoundingMode.HALF_UP);
    }

    protected StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                .totalQuantity(0)
                .availableQuantity(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .uniqueItemsCount(0)
                .utilizationPercentage(0.0)
                .locationPath(null)
                .locationLevel(null)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .lastPutawayDate(null)
                .lastPickDate(null)
                .items(new ArrayList<>())
                .build();
    }

    public List<BinResponse> toResponseList(List<Bin> bins) {
        if (bins == null) {
            return null;
        }
        return bins.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}