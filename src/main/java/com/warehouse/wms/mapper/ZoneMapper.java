// ====== FILE: src/main/java/com/warehouse/wms/mapper/ZoneMapper.java ======
package com.warehouse.wms.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.service.StockAvailabilityService;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ZoneMapper {

    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    @Autowired
    protected WarehouseMapper warehouseMapper;

    // ❌ REMOVE: @Autowired protected AisleMapper aisleMapper;

    // ====== TO RESPONSE ======
    
    @Mapping(target = "warehouse", expression = "java(mapWarehouse(zone.getWarehouse()))")
    @Mapping(target = "aisles", expression = "java(mapAisles(zone.getAisles()))")
    @Mapping(target = "stockSummary", expression = "java(getZoneStockSummary(zone))")
    public abstract ZoneResponse toResponse(Zone zone);

    // ====== TO ENTITY ======
    
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "aisles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Zone toEntity(ZoneRequest request);

    // ====== UPDATE ENTITY ======
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "aisles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract void updateEntity(@MappingTarget Zone zone, ZoneRequest request);

    // ====== Custom Mapping ======

    // ✅ Manual mapping for Aisles - NO AisleMapper dependency
    protected List<AisleResponse> mapAisles(List<Aisle> aisles) {
        if (aisles == null || aisles.isEmpty()) {
            return null;
        }
        return aisles.stream()
                .map(this::mapAisle)
                .collect(Collectors.toList());
    }

    // ✅ Manual mapping for Aisle - NO AisleMapper dependency
    protected AisleResponse mapAisle(Aisle aisle) {
        if (aisle == null) {
            return null;
        }
        
        // Get aisle stock summary
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
                .barcodeData(aisle.getBarcodeData())
                .barcodeImage(aisle.getBarcodeImage())
                .barcodeFormat(aisle.getBarcodeFormat())
                .stockSummary(aisleStockSummary)
                .zone(null)  // Prevent circular reference
                .racks(null)  // Prevent circular reference
                .build();
    }

    protected WarehouseResponse mapWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        WarehouseResponse response = warehouseMapper.toResponse(warehouse);
        if (response != null) {
            response.setZones(null);
        }
        return response;
    }

    protected StockAvailabilitySummary getZoneStockSummary(Zone zone) {
        if (zone == null) {
            return buildEmptyStockSummary();
        }
        try {
            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) {
                return buildEmptyStockSummary();
            }
            return stockAvailabilityService.getZoneStockSummary(
                    warehouse.getWarehouseId(),
                    zone.getZoneId()
            );
        } catch (Exception e) {
            return buildEmptyStockSummary();
        }
    }

    public List<ZoneResponse> toResponseList(List<Zone> zones) {
        if (zones == null) {
            return null;
        }
        return zones.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

 // ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======

    protected StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                // Stock counts
                .totalQuantity(0)
                .stockin(0)                    // ✅ Changed from availableQuantity
                .reservedQuantity(0)
                .inTransitQuantity(0)
                
                // Capacity
                .maxCapacity(null)             // ✅ Use null instead of 0
                .minCapacity(null)             // ✅ Added minCapacity
                .utilizationPercentage(0.0)
                .availableSlots(0)             // ✅ Added availableSlots
                .occupiedSlots(0)              // ✅ Added occupiedSlots
                
                // Stock status
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)             // ✅ Added isLowStock
                .isHighStock(false)            // ✅ Added isHighStock
                .stockStatus("EMPTY")          // ✅ Added stockStatus
                
                // Location
                .locationPath(null)
                .locationLevel(null)
                
                // Items
                .uniqueItemsCount(0)
                .items(new ArrayList<>())
                
                // Timestamps
                .lastPutawayDate(null)
                .lastPickDate(null)
                
                // Summary
                .totalBinsUsed(0)              // ✅ Added totalBinsUsed
                .totalBinsAvailable(0)         // ✅ Added totalBinsAvailable
                .stockTurnoverRate(0.0)        // ✅ Added stockTurnoverRate
                
                .build();
    }
}