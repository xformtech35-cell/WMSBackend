// ====== FILE: src/main/java/com/warehouse/wms/mapper/AisleMapper.java ======
package com.warehouse.wms.mapper;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.ZoneResponse;
import com.warehouse.wms.dto.response.WarehouseResponse;
import com.warehouse.wms.entity.Aisle;
import com.warehouse.wms.entity.Zone;
import com.warehouse.wms.entity.Warehouse;
import com.warehouse.wms.service.StockAvailabilityService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AisleMapper {

    @Autowired
    protected StockAvailabilityService stockAvailabilityService;

    @Autowired
    protected WarehouseMapper warehouseMapper;

    // ❌ REMOVE: @Autowired protected ZoneMapper zoneMapper;

    // ====== TO RESPONSE ======
    
    @Mapping(target = "zone", expression = "java(mapZone(aisle.getZone()))")
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "stockSummary", expression = "java(getAisleStockSummary(aisle))")
    public abstract AisleResponse toResponse(Aisle aisle);

    // ====== TO ENTITY ======
    
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract Aisle toEntity(AisleRequest request);

    // ====== UPDATE ENTITY ======
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "racks", ignore = true)
    @Mapping(target = "totalRacks", ignore = true)
    public abstract void updateEntity(@MappingTarget Aisle aisle, AisleRequest request);

    // ====== Custom Mapping ======

    // ✅ Manual mapping for Zone - NO ZoneMapper dependency
    protected ZoneResponse mapZone(Zone zone) {
        if (zone == null) {
            return null;
        }
        
        // Get zone stock summary
        StockAvailabilitySummary zoneStockSummary = null;
        try {
            if (zone.getWarehouse() != null) {
                zoneStockSummary = stockAvailabilityService.getZoneStockSummary(
                        zone.getWarehouse().getWarehouseId(),
                        zone.getZoneId()
                );
            }
        } catch (Exception e) {
            zoneStockSummary = buildEmptyStockSummary();
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
                .barcodeData(zone.getBarcodeData())
                .barcodeImage(zone.getBarcodeImage())
                .barcodeFormat(zone.getBarcodeFormat())
                .stockSummary(zoneStockSummary)
                .warehouse(mapWarehouse(zone.getWarehouse()))
                .aisles(null)  // Prevent circular reference
                .build();
    }

    // ✅ Manual mapping for Warehouse
    protected WarehouseResponse mapWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        
        // Get warehouse stock summary
        StockAvailabilitySummary warehouseStockSummary = null;
        try {
            warehouseStockSummary = stockAvailabilityService.getWarehouseStockSummary(warehouse.getWarehouseId());
        } catch (Exception e) {
            warehouseStockSummary = buildEmptyStockSummary();
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
                
                .stockSummary(warehouseStockSummary)
                .zones(null)  // Prevent circular reference
                .build();
    }

    protected StockAvailabilitySummary getAisleStockSummary(Aisle aisle) {
        if (aisle == null) {
            return buildEmptyStockSummary();
        }

        try {
            Zone zone = aisle.getZone();
            if (zone == null) {
                return buildEmptyStockSummary();
            }

            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) {
                return buildEmptyStockSummary();
            }

            StockAvailabilitySummary summary = stockAvailabilityService.getAisleStockSummary(
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId()
            );
            
            if (summary == null) {
                return buildEmptyStockSummary();
            }
            
            return summary;
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
                .items(new ArrayList<>())
                .build();
    }

    public List<AisleResponse> toResponseList(List<Aisle> aisles) {
        if (aisles == null) {
            return null;
        }
        return aisles.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}