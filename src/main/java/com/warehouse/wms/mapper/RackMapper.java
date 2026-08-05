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
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RackMapper {

    // ====== Main Mapping ======
    
    @Mapping(target = "aisle", expression = "java(mapAisle(rack.getAisle()))")
    @Mapping(target = "bins", expression = "java(mapBins(rack.getBins()))")
    @Mapping(target = "compartments", ignore = true)
    public abstract RackResponse toResponse(Rack rack);

    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    public abstract Rack toEntity(RackRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "aisle", ignore = true)
    @Mapping(target = "bins", ignore = true)
    @Mapping(target = "compartments", ignore = true)
    @Mapping(target = "totalShelves", ignore = true)
    public abstract void updateEntity(@MappingTarget Rack rack, RackRequest request);

    // ====== Aisle Mapping with Full Hierarchy ======
    
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
                .zone(mapZone(aisle.getZone()))  // ✅ Zone with Warehouse
                .racks(null)  // Prevent circular reference
                .build();
    }

    // ====== Zone Mapping with Warehouse ======
    
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
                .warehouse(mapWarehouse(zone.getWarehouse()))  // ✅ Warehouse
                .aisles(null)  // Prevent circular reference
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
                .zones(null)  // Prevent circular reference
                .build();
    }

    // ====== Bin Mapping ======
    
    protected List<BinResponse> mapBins(List<Bin> bins) {
        if (bins == null || bins.isEmpty()) {
            return null;
        }
        return bins.stream()
                .map(this::mapBin)
                .collect(Collectors.toList());
    }

    protected BinResponse mapBin(Bin bin) {
        if (bin == null) {
            return null;
        }
        
        String status = bin.getStatus() != null ? bin.getStatus().name() : null;
        
        BigDecimal utilizationPercentage = BigDecimal.ZERO;
        if (bin.getVolumeCm3() != null && 
            BigDecimal.ZERO.compareTo(bin.getVolumeCm3()) != 0 &&
            bin.getOccupiedVolumeCm3() != null) {
            utilizationPercentage = bin.getOccupiedVolumeCm3()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(bin.getVolumeCm3(), 2, java.math.RoundingMode.HALF_UP);
        }
        
        // Safely get location info
        String warehouseId = null;
        String zoneId = null;
        String aisleId = null;
        if (bin.getRack() != null && bin.getRack().getAisle() != null) {
            if (bin.getRack().getAisle().getZone() != null) {
                if (bin.getRack().getAisle().getZone().getWarehouse() != null) {
                    warehouseId = bin.getRack().getAisle().getZone().getWarehouse().getWarehouseId();
                }
                zoneId = bin.getRack().getAisle().getZone().getZoneId();
            }
            aisleId = bin.getRack().getAisle().getAisleId();
        }
        
        return BinResponse.builder()
                .id(bin.getId())
                .binId(bin.getBarcode())
                .binBarcode(bin.getBarcode())
                .warehouseId(warehouseId)
                .zone(zoneId)
                .aisle(aisleId)
                .shelf(null)
                .level(null)
                .position(null)
                .capacity(bin.getVolumeCm3() != null ? bin.getVolumeCm3().intValue() : null)
                .availableCapacity(bin.getAvailableVolume() != null ? bin.getAvailableVolume().intValue() : null)
                .usedCapacity(bin.getOccupiedVolumeCm3() != null ? bin.getOccupiedVolumeCm3().intValue() : null)
                .minThreshold(null)
                .maxThreshold(null)
                .itemCode(null)
                .itemName(null)
                .uom(null)
                .isOccupied(bin.getStatus() == Bin.BinStatus.FULL)
                .isActive(bin.getIsActive())
                .isReserved(bin.getStatus() == Bin.BinStatus.BLOCKED)
                .reservedFor(null)
                .locationType(null)
                .zoneType(null)
                .movementType(null)
                .priority(null)
                .distanceFromDispatch(null)
                .fullLocation(bin.getFullLocation())
                .status(status)
                .utilizationPercentage(utilizationPercentage)
                .lastAccessedAt(null)
                .lastPutawayAt(null)
                .lastPickAt(null)
                .remarks(bin.getRemarks())
                .createdBy(bin.getCreatedBy())
                .createdAt(bin.getCreatedAt())
                .updatedAt(bin.getUpdatedAt())
                .rack(null)  // Prevent circular reference
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