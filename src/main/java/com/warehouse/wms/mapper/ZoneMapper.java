// ====== FILE: src/main/java/com/warehouse/wms/mapper/ZoneMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.entity.Aisle;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)  // ✅ Remove WarehouseMapper
public abstract class ZoneMapper {

    @Mapping(target = "warehouse", expression = "java(mapWarehouse(zone.getWarehouse()))")
    @Mapping(target = "aisles", expression = "java(mapAisles(zone.getAisles()))")
    public abstract ZoneResponse toResponse(Zone zone);

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "aisles", ignore = true)
    @Mapping(target = "totalAisles", ignore = true)
    public abstract Zone toEntity(ZoneRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "aisles", ignore = true)
    @Mapping(target = "totalAisles", ignore = true)
    public abstract void updateEntity(@MappingTarget Zone zone, ZoneRequest request);

    // ✅ Custom mapping to avoid circular dependency
    protected WarehouseResponse mapWarehouse(com.warehouse.wms.entity.Warehouse warehouse) {
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
                .zones(null)  // ✅ Set to null to avoid circular reference
                .build();
    }

    // ✅ Custom mapping to avoid circular dependency
    protected List<AisleResponse> mapAisles(List<Aisle> aisles) {
        if (aisles == null || aisles.isEmpty()) {
            return null;
        }
        return aisles.stream()
                .map(aisle -> AisleResponse.builder()
                        .id(aisle.getId())
                        .aisleId(aisle.getAisleId())
                        .name(aisle.getName())
                        .description(aisle.getDescription())
                        .isActive(aisle.getIsActive())
                        .width(aisle.getWidth())
                        .length(aisle.getLength())
                        .totalRacks(aisle.getTotalRacks())
                        .remarks(aisle.getRemarks())
                        .createdBy(aisle.getCreatedBy())
                        .createdAt(aisle.getCreatedAt())
                        .updatedAt(aisle.getUpdatedAt())
                        .zone(null)  // ✅ Set to null to avoid circular reference
                        .racks(null)  // ✅ Set to null to avoid circular reference
                        .build())
                .collect(Collectors.toList());
    }
}