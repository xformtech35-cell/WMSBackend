// ====== FILE: src/main/java/com/warehouse/wms/mapper/BinMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.BinCreateRequest;
import com.warehouse.wms.dto.BinResponse;
import com.warehouse.wms.entity.Bin;
import com.warehouse.wms.entity.Rack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    @Mapping(target = "rackId", source = "rack.id")  // ✅ Map rack ID
    @Mapping(target = "rackName", source = "rack.name")  // ✅ Map rack name
    @Mapping(target = "rack", ignore = true)  // ✅ Ignore full rack object
    BinResponse toResponse(Bin bin);

    // Helper methods
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