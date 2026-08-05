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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.BinResponse;
import com.warehouse.wms.dto.response.RackResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Rack;
import com.warehouse.wms.entity.Warehouse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class RackMapper {

	   @Autowired
	    @Lazy
	    protected AisleMapper aisleMapper;

	   @Mapping(target = "aisle", expression = "java(mapAisle(rack.getAisle()))")
	    @Mapping(target = "bins", expression = "java(mapBins(rack.getBins()))")
	    @Mapping(target = "compartments", ignore = true)
	    @Mapping(target = "warehouse", expression = "java(mapWarehouse(rack.getAisle().getZone().getWarehouse()))")  // ✅ Add warehouse
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

    // ✅ Map aisle without circular dependency
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
                .remarks(aisle.getRemarks())
                .createdBy(aisle.getCreatedBy())
                .createdAt(aisle.getCreatedAt())
                .updatedAt(aisle.getUpdatedAt())
                .zone(null)  // ✅ Set to null to avoid circular reference
                .racks(null)  // ✅ Set to null to avoid circular reference
                .build();
    }
    
    
    
    public WarehouseResponse mapWarehouse(Warehouse warehouse) {
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

    // ✅ Custom mapping for bins - NO circular dependency
    protected List<BinResponse> mapBins(List<Bin> bins) {
        if (bins == null || bins.isEmpty()) {
            return null;
        }
        return bins.stream()
                .map(this::mapBin)
                .collect(Collectors.toList());
    }

    // ✅ Map single bin - NO rack reference
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
        
        return BinResponse.builder()
                .id(bin.getId())
                .binId(bin.getBarcode())
                .binBarcode(bin.getBarcode())
                .warehouseId(bin.getRack() != null && bin.getRack().getAisle() != null && 
                            bin.getRack().getAisle().getZone() != null && 
                            bin.getRack().getAisle().getZone().getWarehouse() != null ? 
                            bin.getRack().getAisle().getZone().getWarehouse().getWarehouseId() : null)
                .zone(bin.getRack() != null && bin.getRack().getAisle() != null && 
                      bin.getRack().getAisle().getZone() != null ? 
                      bin.getRack().getAisle().getZone().getZoneId() : null)
                .aisle(bin.getRack() != null && bin.getRack().getAisle() != null ? 
                       bin.getRack().getAisle().getAisleId() : null)
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
                .rack(null)  // ✅ Set to null to break cycle
                .build();
    }

    // ✅ Method for list mapping
    public List<RackResponse> toResponseList(List<Rack> racks) {
        if (racks == null) {
            return null;
        }
        return racks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}