// ====== FILE: src/main/java/com/warehouse/wms/mapper/WarehouseMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Zone;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)  // ✅ Remove ZoneMapper
public abstract class WarehouseMapper {

    @Mapping(target = "zones", expression = "java(mapZones(warehouse.getZones()))")
    public abstract WarehouseResponse toResponse(Warehouse warehouse);

    @Mapping(target = "zones", ignore = true)
    @Mapping(target = "totalZones", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Warehouse toEntity(WarehouseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zones", ignore = true)
    @Mapping(target = "totalZones", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget Warehouse warehouse, WarehouseRequest request);

    // ✅ Custom mapping to avoid circular dependency
    protected List<ZoneResponse> mapZones(List<Zone> zones) {
        if (zones == null || zones.isEmpty()) {
            return null;
        }
        return zones.stream()
                .map(this::mapZone)
                .collect(Collectors.toList());
    }

    // ✅ Map single zone without using ZoneMapper
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
                .aisles(null)  // ✅ Set to null to avoid circular reference
                .warehouse(null)  // ✅ Set to null to avoid circular reference
                .build();
    }
}