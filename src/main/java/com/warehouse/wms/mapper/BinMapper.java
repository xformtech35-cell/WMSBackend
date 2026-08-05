// ====== FILE: src/main/java/com/warehouse/wms/mapper/BinMapper.java ======
package com.warehouse.wms.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;

@Mapper(componentModel = "spring")
public interface BinMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rack", ignore = true)
    @Mapping(target = "volumeCm3", ignore = true)
    @Mapping(target = "occupiedVolumeCm3", constant = "0")
    @Mapping(target = "occupiedWeightG", constant = "0")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "remarks", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Bin toEntity(BinCreateRequest request);

    @Mapping(target = "fullLocation", expression = "java(getFullLocation(bin))")
    @Mapping(target = "utilizationPercentage", expression = "java(calculateUtilization(bin))")
    @Mapping(target = "rack", expression = "java(mapRack(bin.getRack()))")
    BinResponse toResponse(Bin bin);

    // ✅ Map Rack with full hierarchy
    default RackResponse mapRack(Rack rack) {
        if (rack == null) {
            return null;
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
                .remarks(rack.getRemarks())
                .createdBy(rack.getCreatedBy())
                .createdAt(rack.getCreatedAt())
                .updatedAt(rack.getUpdatedAt())
                .aisle(mapAisle(rack.getAisle()))
                .bins(null)
                .compartments(null)
                .build();
    }

    // ✅ Map Aisle with Zone → Warehouse
    default AisleResponse mapAisle(Aisle aisle) {
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
                .remarks(aisle.getRemarks())
                .createdBy(aisle.getCreatedBy())
                .createdAt(aisle.getCreatedAt())
                .updatedAt(aisle.getUpdatedAt())
                .zone(mapZone(aisle.getZone()))
                .racks(null)
                .build();
    }

    // ✅ Map Zone with Warehouse
    default ZoneResponse mapZone(Zone zone) {
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
    default WarehouseResponse mapWarehouse(Warehouse warehouse) {
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

    // ✅ Helper methods
    default String getFullLocation(Bin bin) {
        if (bin == null || bin.getRack() == null || bin.getRack().getAisle() == null || 
            bin.getRack().getAisle().getZone() == null || 
            bin.getRack().getAisle().getZone().getWarehouse() == null) {
            return null;
        }
        return String.format("%s-%s-%s-%s-%s",
                bin.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                bin.getRack().getAisle().getZone().getZoneId(),
                bin.getRack().getAisle().getAisleId(),
                bin.getRack().getRackId(),
                bin.getBarcode());
    }

    default BigDecimal calculateUtilization(Bin bin) {
        if (bin == null || bin.getVolumeCm3() == null || 
            BigDecimal.ZERO.compareTo(bin.getVolumeCm3()) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal occupied = bin.getOccupiedVolumeCm3() == null ? 
                               BigDecimal.ZERO : bin.getOccupiedVolumeCm3();
        return occupied.multiply(BigDecimal.valueOf(100))
                .divide(bin.getVolumeCm3(), 2, RoundingMode.HALF_UP);
    }
}