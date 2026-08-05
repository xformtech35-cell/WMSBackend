// ====== FILE: src/main/java/com/warehouse/wms/mapper/AisleMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AisleMapper {

    @Mapping(target = "zone", expression = "java(mapZone(aisle.getZone()))")
    @Mapping(target = "racks", ignore = true)  // ✅ IGNORE racks to break circular dependency
    public abstract AisleResponse toResponse(Aisle aisle);

    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract Aisle toEntity(AisleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract void updateEntity(@MappingTarget Aisle aisle, AisleRequest request);

    // ✅ Map Zone with Warehouse
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

    // ✅ Map Warehouse
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

    // ✅ Method for list mapping
    public List<AisleResponse> toResponseList(List<Aisle> aisles) {
        if (aisles == null) {
            return null;
        }
        return aisles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}