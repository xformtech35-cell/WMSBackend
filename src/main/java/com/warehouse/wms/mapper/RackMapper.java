// ====== FILE: src/main/java/com/warehouse/wms/mapper/RackMapper.java ======
package com.warehouse.wms.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.LevelResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Level;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RackMapper {

    // ====== Main Mapping ======
    
    @Mapping(target = "aisle", expression = "java(mapAisle(rack.getAisle()))")
    @Mapping(target = "levels", expression = "java(mapLevels(rack.getLevels()))")
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

    // ====== Aisle Mapping ======
    
    protected AisleResponse mapAisle(Aisle aisle) {
        if (aisle == null) {
            return null;
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
                .zone(mapZone(aisle.getZone()))
                .racks(null)
                .build();
    }

    // ====== Zone Mapping ======
    
    protected ZoneResponse mapZone(Zone zone) {
        if (zone == null) {
            return null;
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
                .warehouse(mapWarehouse(zone.getWarehouse()))
                .aisles(null)
                .build();
    }

    // ====== Warehouse Mapping ======
    
    protected WarehouseResponse mapWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
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

    // ====== Level Mapping (Manual mapping - NO circular dependency) ======
    
    protected List<LevelResponse> mapLevels(List<Level> levels) {
        if (levels == null || levels.isEmpty()) {
            return null;
        }
        return levels.stream()
                .map(this::mapLevel)
                .collect(Collectors.toList());
    }

    // ✅ Manual mapping for Level - NO LevelMapper dependency
    protected LevelResponse mapLevel(Level level) {
        if (level == null) {
            return null;
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
                .createdBy(level.getCreatedBy())
                .createdAt(level.getCreatedAt())
                .updatedAt(level.getUpdatedAt())
                .rack(null)  // Prevent circular reference
                .bins(null)  // Prevent circular reference
                .build();
    }

    // ====== List Mapping Helper ======
    
    public List<RackResponse> toResponseList(List<Rack> racks) {
        if (racks == null) {
            return null;
        }
        return racks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}