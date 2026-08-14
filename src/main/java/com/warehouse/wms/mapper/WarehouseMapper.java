// ====== FILE: src/main/java/com/warehouse/wms/mapper/WarehouseMapper.java ======
package com.warehouse.wms.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.service.StockAvailabilityService;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class WarehouseMapper {

    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    // ====== TO RESPONSE ======
    
    @Mapping(target = "zones", expression = "java(mapZones(warehouse.getZones()))")
    @Mapping(target = "stockSummary", expression = "java(getWarehouseStockSummary(warehouse.getWarehouseId()))")
    public abstract WarehouseResponse toResponse(Warehouse warehouse);

    // ====== TO ENTITY ======
    
    @Mapping(target = "zones", ignore = true)
    @Mapping(target = "totalZones", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Warehouse toEntity(WarehouseRequest request);

    // ====== UPDATE ENTITY ======
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zones", ignore = true)
    @Mapping(target = "totalZones", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget Warehouse warehouse, WarehouseRequest request);

    // ====== Custom Mapping ======
    
    protected List<ZoneResponse> mapZones(List<Zone> zones) {
        if (zones == null || zones.isEmpty()) {
            return null;
        }
        return zones.stream()
                .map(this::mapZone)
                .collect(Collectors.toList());
    }

    protected ZoneResponse mapZone(Zone zone) {
        if (zone == null) {
            return null;
        }
        
        StockAvailabilitySummary stockSummary = getZoneStockSummary(
                zone.getWarehouse() != null ? zone.getWarehouse().getWarehouseId() : null,
                zone.getZoneId()
        );
        
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
                .stockSummary(stockSummary)
                .warehouse(null)
                .aisles(null)
                .build();
    }

    // ====== Stock Summary Helper Methods ======

    protected StockAvailabilitySummary getWarehouseStockSummary(String warehouseId) {
        if (warehouseId == null) {
            return buildEmptyStockSummary();
        }
        try {
            return stockAvailabilityService.getWarehouseStockSummary(warehouseId);
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

    protected StockAvailabilitySummary getZoneStockSummary(String warehouseId, String zoneId) {
        if (warehouseId == null || zoneId == null) {
            return buildEmptyStockSummary();
        }
        try {
            return stockAvailabilityService.getZoneStockSummary(warehouseId, zoneId);
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

    protected StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                .totalQuantity(0)
                .availableQuantity(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .uniqueItemsCount(0)
//                .maxCapacity(0)
                .utilizationPercentage(0.0)
                .locationPath(null)
                .locationLevel(null)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .lastPutawayDate(null)
                .lastPickDate(null)
                .items(List.of())
                .build();
    }
}