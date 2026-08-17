// ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.response.ItemStockSummary;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.dto.response.StockAvailabilityResponse;
import com.warehouse.wms.entity.*;
import com.warehouse.wms.entity.StockAvailability.LocationLevel;
import com.warehouse.wms.repository.*;
import com.warehouse.wms.service.StockAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAvailabilityServiceImpl implements StockAvailabilityService {

    private final StockAvailabilityRepository stockAvailabilityRepository;
    private final WarehouseRepository warehouseRepository;
    private final ZoneRepository zoneRepository;
    private final AisleRepository aisleRepository;
    private final RackRepository rackRepository;
    private final LevelRepository levelRepository;
    private final BinRepository binRepository;

    // ====== Get Stock Availability at Each Level ======

    @Override
    public StockAvailabilitySummary getWarehouseStockSummary(String warehouseId) {
        log.debug("Getting warehouse stock summary for: {}", warehouseId);
        
        if (warehouseId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = stockAvailabilityRepository
                .findByWarehouseIdAndLocationLevel(warehouseId, LocationLevel.WAREHOUSE);
        
        // If no stock records found, create a summary with warehouse capacity
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummaryWithCapacity(LocationLevel.WAREHOUSE, warehouseId, null, null, null, null);
        }
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getZoneStockSummary(String warehouseId, String zoneId) {
        log.debug("Getting zone stock summary for: {}-{}", warehouseId, zoneId);
        
        if (warehouseId == null || zoneId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = stockAvailabilityRepository
                .findByWarehouseIdAndZoneIdAndLocationLevel(warehouseId, zoneId, LocationLevel.ZONE);
        
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummaryWithCapacity(LocationLevel.ZONE, warehouseId, zoneId, null, null, null);
        }
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getAisleStockSummary(String warehouseId, String zoneId, String aisleId) {
        log.debug("Getting aisle stock summary for: {}-{}-{}", warehouseId, zoneId, aisleId);
        
        if (warehouseId == null || zoneId == null || aisleId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = stockAvailabilityRepository
                .findByWarehouseIdAndZoneIdAndAisleIdAndLocationLevel(warehouseId, zoneId, aisleId, LocationLevel.AISLE);
        
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummaryWithCapacity(LocationLevel.AISLE, warehouseId, zoneId, aisleId, null, null);
        }
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getRackStockSummary(String warehouseId, String zoneId, String aisleId, String rackId) {
        log.debug("Getting rack stock summary for: {}-{}-{}-{}", warehouseId, zoneId, aisleId, rackId);
        
        if (warehouseId == null || zoneId == null || aisleId == null || rackId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = stockAvailabilityRepository
                .findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLocationLevel(warehouseId, zoneId, aisleId, rackId, LocationLevel.RACK);
        
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummaryWithCapacity(LocationLevel.RACK, warehouseId, zoneId, aisleId, rackId, null);
        }
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getLevelStockSummary(String warehouseId, String zoneId, String aisleId, String rackId, String levelId) {
        log.debug("Getting level stock summary for: {}-{}-{}-{}-{}", warehouseId, zoneId, aisleId, rackId, levelId);
        
        if (warehouseId == null || zoneId == null || aisleId == null || rackId == null || levelId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = stockAvailabilityRepository
                .findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelIdAndLocationLevel(warehouseId, zoneId, aisleId, rackId, levelId, LocationLevel.LEVEL);
        
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummaryWithCapacity(LocationLevel.LEVEL, warehouseId, zoneId, aisleId, rackId, levelId);
        }
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getBinStockSummary(String binId) {
        log.debug("Getting bin stock summary for: {}", binId);
        
        if (binId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = new ArrayList<>();
        String foundBinId = binId;
        String foundBinBarcode = null;
        String foundWarehouseId = null;
        String foundZoneId = null;
        String foundAisleId = null;
        String foundRackId = null;
        String foundLevelId = null;
        Integer maxCapFromBin = null;
        Integer minCapFromBin = null;
        
        try {
            // First try to get bin entity to get capacity
            Optional<Bin> binEntity = binRepository.findByBarcode(binId);
            if (binEntity.isPresent()) {
                Bin bin = binEntity.get();
                maxCapFromBin = bin.getMaxCapacity();
                minCapFromBin = bin.getMinCapacity();
                foundWarehouseId = bin.getLevel() != null && bin.getLevel().getRack() != null && 
                        bin.getLevel().getRack().getAisle() != null && 
                        bin.getLevel().getRack().getAisle().getZone() != null && 
                        bin.getLevel().getRack().getAisle().getZone().getWarehouse() != null ?
                        bin.getLevel().getRack().getAisle().getZone().getWarehouse().getWarehouseId() : null;
                log.debug("Found bin entity with maxCapacity: {} for bin: {}", maxCapFromBin, binId);
            }
            
            // Try to find stock records
            List<StockAvailability> byBinId = stockAvailabilityRepository
                    .findByBinIdAndItemCode(binId, null);
            
            if (byBinId != null && !byBinId.isEmpty()) {
                stocks = byBinId;
                log.debug("Found {} stock records by bin_id: {}", stocks.size(), binId);
            }
            
            if (stocks.isEmpty()) {
                List<StockAvailability> byBinBarcode = stockAvailabilityRepository
                        .findByBinBarcodeAndItemCode(binId, null);
                if (byBinBarcode != null && !byBinBarcode.isEmpty()) {
                    stocks = byBinBarcode;
                    foundBinBarcode = binId;
                    log.debug("Found {} stock records by bin_barcode: {}", stocks.size(), binId);
                }
            }
            
            if (stocks.isEmpty()) {
                stocks = stockAvailabilityRepository
                        .findAll()
                        .stream()
                        .filter(s -> LocationLevel.BIN == s.getLocationLevel())
                        .filter(s -> binId.equals(s.getBinId()) || binId.equals(s.getBinBarcode()))
                        .collect(Collectors.toList());
                
                if (!stocks.isEmpty()) {
                    log.debug("Found {} stock records by manual filtering: {}", stocks.size(), binId);
                }
            }
            
            // If we have stock records but no bin entity, try to get capacity from stock record
            if (!stocks.isEmpty() && maxCapFromBin == null) {
                for (StockAvailability stock : stocks) {
                    if (stock.getMaxCapacity() != null && stock.getMaxCapacity() > 0) {
                        maxCapFromBin = stock.getMaxCapacity();
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching bin stock summary for binId: {}", binId, e);
        }
        
        // Build summary with capacity from bin entity
        return buildBinSummary(stocks, binId, maxCapFromBin, minCapFromBin);
    }

    // ====== Check Stock Availability ======

    @Override
    public boolean isStockAvailable(String warehouseId, String zoneId, String aisleId,
                                    String rackId, String levelId, String binId,
                                    String itemCode, Integer requiredQuantity) {
        log.debug("Checking stock availability: item={}, required={}", itemCode, requiredQuantity);
        
        if (requiredQuantity == null || requiredQuantity <= 0) {
            return true;
        }
        
        if (binId != null) {
            Optional<Integer> binAvailable = stockAvailabilityRepository
                    .getAvailableQuantityInBin(binId, itemCode);
            if (binAvailable.isPresent() && binAvailable.get() >= requiredQuantity) {
                return true;
            }
        }

        if (levelId != null) {
            Optional<StockAvailability> levelStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, null, 
                            itemCode, LocationLevel.LEVEL);
            if (levelStock.isPresent() && levelStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        if (rackId != null) {
            Optional<StockAvailability> rackStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, null, null, 
                            itemCode, LocationLevel.RACK);
            if (rackStock.isPresent() && rackStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        if (aisleId != null) {
            Optional<StockAvailability> aisleStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, null, null, null, 
                            itemCode, LocationLevel.AISLE);
            if (aisleStock.isPresent() && aisleStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        if (zoneId != null) {
            Optional<StockAvailability> zoneStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, null, null, null, null, 
                            itemCode, LocationLevel.ZONE);
            if (zoneStock.isPresent() && zoneStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        if (warehouseId != null) {
            Optional<StockAvailability> warehouseStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, null, null, null, null, null, 
                            itemCode, LocationLevel.WAREHOUSE);
            if (warehouseStock.isPresent() && warehouseStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Integer getAvailableQuantity(String warehouseId, String zoneId, String aisleId,
                                        String rackId, String levelId, String binId, String itemCode) {
        log.debug("Getting available quantity: item={}", itemCode);
        
        if (binId != null) {
            Optional<Integer> binAvailable = stockAvailabilityRepository
                    .getAvailableQuantityInBin(binId, itemCode);
            if (binAvailable.isPresent()) {
                return binAvailable.get();
            }
        }

        if (levelId != null) {
            Optional<StockAvailability> levelStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, null, 
                            itemCode, LocationLevel.LEVEL);
            if (levelStock.isPresent()) {
                return levelStock.get().getAvailableQuantity();
            }
        }

        if (rackId != null) {
            Optional<StockAvailability> rackStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, null, null, 
                            itemCode, LocationLevel.RACK);
            if (rackStock.isPresent()) {
                return rackStock.get().getAvailableQuantity();
            }
        }

        if (aisleId != null) {
            Optional<StockAvailability> aisleStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, null, null, null, 
                            itemCode, LocationLevel.AISLE);
            if (aisleStock.isPresent()) {
                return aisleStock.get().getAvailableQuantity();
            }
        }

        if (zoneId != null) {
            Optional<StockAvailability> zoneStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, null, null, null, null, 
                            itemCode, LocationLevel.ZONE);
            if (zoneStock.isPresent()) {
                return zoneStock.get().getAvailableQuantity();
            }
        }

        if (warehouseId != null) {
            Optional<StockAvailability> warehouseStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, null, null, null, null, null, 
                            itemCode, LocationLevel.WAREHOUSE);
            if (warehouseStock.isPresent()) {
                return warehouseStock.get().getAvailableQuantity();
            }
        }

        return 0;
    }

    /**
     * Get available slots (capacity) for a location
     */
    public StockAvailabilityResponse getAvailableSlots(String warehouseId, String zoneId, String aisleId,
                                                        String rackId, String levelId, String binId) {
        log.debug("Getting available slots for location: warehouse={}, zone={}, aisle={}, rack={}, level={}, bin={}", 
                warehouseId, zoneId, aisleId, rackId, levelId, binId);
        
        StockAvailabilityResponse response = new StockAvailabilityResponse();
        
        // Get max capacity from entity based on hierarchy
        Integer maxCapacity = getLocationMaxCapacity(warehouseId, zoneId, aisleId, rackId, levelId, binId);
        response.setMaxCapacity(maxCapacity);
        
        // Get total quantity at this location (all items)
        Integer totalQuantity = getTotalQuantityAtLocation(warehouseId, zoneId, aisleId, rackId, levelId, binId);
        response.setTotalQuantity(totalQuantity);
        
        // Calculate available slots
        if (maxCapacity != null && maxCapacity > 0) {
            int availableSlots = Math.max(0, maxCapacity - (totalQuantity != null ? totalQuantity : 0));
            response.setAvailableSlots(availableSlots);
            
            double utilization = ((double) (totalQuantity != null ? totalQuantity : 0) / maxCapacity) * 100;
            response.setUtilizationPercentage(Math.min(utilization, 100.0));
        } else {
            response.setAvailableSlots(0);
            response.setUtilizationPercentage(0.0);
        }
        
        return response;
    }

    /**
     * Get max capacity from entity based on location hierarchy
     */
    private Integer getLocationMaxCapacity(String warehouseId, String zoneId, String aisleId,
                                           String rackId, String levelId, String binId) {
        try {
            // Check from most specific to least specific
            if (binId != null) {
                Optional<Bin> bin = binRepository.findByBarcode(binId);
                if (bin.isPresent() && bin.get().getMaxCapacity() != null) {
                    return bin.get().getMaxCapacity();
                }
            }
            
            if (levelId != null && warehouseId != null && zoneId != null && aisleId != null && rackId != null) {
                Optional<Level> level = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                        warehouseId, zoneId, aisleId, rackId, levelId);
                if (level.isPresent() && level.get().getMaxCapacity() != null) {
                    return level.get().getMaxCapacity();
                }
            }
            
            if (rackId != null && warehouseId != null && zoneId != null && aisleId != null) {
                Optional<Rack> rack = rackRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
                        warehouseId, zoneId, aisleId, rackId);
                if (rack.isPresent() && rack.get().getMaxCapacity() != null) {
                    return rack.get().getMaxCapacity();
                }
            }
            
            if (aisleId != null && warehouseId != null && zoneId != null) {
                Optional<Aisle> aisle = aisleRepository.findByWarehouseIdAndZoneIdAndAisleId(
                        warehouseId, zoneId, aisleId);
                if (aisle.isPresent() && aisle.get().getMaxCapacity() != null) {
                    return aisle.get().getMaxCapacity();
                }
            }
            
            if (zoneId != null && warehouseId != null) {
                Optional<Zone> zone = zoneRepository.findByWarehouseIdAndZoneId(warehouseId, zoneId);
                if (zone.isPresent() && zone.get().getMaxCapacity() != null) {
                    return zone.get().getMaxCapacity();
                }
            }
            
            if (warehouseId != null) {
                Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(warehouseId);
                if (warehouse.isPresent()) {
                    Integer capacity = warehouse.get().getMaxCapacity();
                    if (capacity != null) {
                        return capacity;
                    }
                    return warehouse.get().getCapacity();
                }
            }
            
        } catch (Exception e) {
            log.warn("Error getting location max capacity", e);
        }
        
        return null;
    }

    /**
     * Get total quantity at a location (all items)
     */
    private Integer getTotalQuantityAtLocation(String warehouseId, String zoneId, String aisleId,
                                               String rackId, String levelId, String binId) {
        try {
            if (binId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository.findByBinIdAndItemCode(binId, null);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
            if (levelId != null && warehouseId != null && zoneId != null && aisleId != null && rackId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository
                        .findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelIdAndLocationLevel(
                                warehouseId, zoneId, aisleId, rackId, levelId, LocationLevel.LEVEL);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
            if (rackId != null && warehouseId != null && zoneId != null && aisleId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository
                        .findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLocationLevel(
                                warehouseId, zoneId, aisleId, rackId, LocationLevel.RACK);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
            if (aisleId != null && warehouseId != null && zoneId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository
                        .findByWarehouseIdAndZoneIdAndAisleIdAndLocationLevel(
                                warehouseId, zoneId, aisleId, LocationLevel.AISLE);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
            if (zoneId != null && warehouseId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository
                        .findByWarehouseIdAndZoneIdAndLocationLevel(
                                warehouseId, zoneId, LocationLevel.ZONE);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
            if (warehouseId != null) {
                List<StockAvailability> stocks = stockAvailabilityRepository
                        .findByWarehouseIdAndLocationLevel(warehouseId, LocationLevel.WAREHOUSE);
                return stocks.stream()
                        .mapToInt(s -> s.getTotalQuantity() != null ? s.getTotalQuantity() : 0)
                        .sum();
            }
            
        } catch (Exception e) {
            log.warn("Error getting total quantity at location", e);
        }
        
        return 0;
    }

    // ====== Private Helper Methods ======

    /**
     * Build empty stock summary with capacity from entity
     */
    private StockAvailabilitySummary buildEmptyStockSummaryWithCapacity(LocationLevel level, 
                                                                         String warehouseId, 
                                                                         String zoneId, 
                                                                         String aisleId, 
                                                                         String rackId, 
                                                                         String levelId) {
        Integer maxCapacity = 0;
        String locationPath = null;
        
        try {
            switch (level) {
                case WAREHOUSE:
                    if (warehouseId != null) {
                        Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(warehouseId);
                        if (warehouse.isPresent()) {
                            maxCapacity = warehouse.get().getMaxCapacity() != null ? 
                                    warehouse.get().getMaxCapacity() : 
                                    (warehouse.get().getCapacity() != null ? warehouse.get().getCapacity() : 0);
                            locationPath = warehouse.get().getWarehouseId();
                            log.debug("Building empty warehouse summary with capacity: {}", maxCapacity);
                        }
                    }
                    break;
                    
                case ZONE:
                    if (warehouseId != null && zoneId != null) {
                        Optional<Zone> zone = zoneRepository.findByWarehouseIdAndZoneId(warehouseId, zoneId);
                        if (zone.isPresent()) {
                            maxCapacity = zone.get().getMaxCapacity() != null ? zone.get().getMaxCapacity() : 0;
                            locationPath = warehouseId + "-" + zoneId;
                            log.debug("Building empty zone summary with capacity: {}", maxCapacity);
                        }
                    }
                    break;
                    
                case AISLE:
                    if (warehouseId != null && zoneId != null && aisleId != null) {
                        Optional<Aisle> aisle = aisleRepository.findByWarehouseIdAndZoneIdAndAisleId(warehouseId, zoneId, aisleId);
                        if (aisle.isPresent()) {
                            maxCapacity = aisle.get().getMaxCapacity() != null ? aisle.get().getMaxCapacity() : 0;
                            locationPath = warehouseId + "-" + zoneId + "-" + aisleId;
                            log.debug("Building empty aisle summary with capacity: {}", maxCapacity);
                        }
                    }
                    break;
                    
                case RACK:
                    if (warehouseId != null && zoneId != null && aisleId != null && rackId != null) {
                        Optional<Rack> rack = rackRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
                                warehouseId, zoneId, aisleId, rackId);
                        if (rack.isPresent()) {
                            maxCapacity = rack.get().getMaxCapacity() != null ? rack.get().getMaxCapacity() : 0;
                            locationPath = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId;
                            log.debug("Building empty rack summary with capacity: {}", maxCapacity);
                        }
                    }
                    break;
                    
                case LEVEL:
                    if (warehouseId != null && zoneId != null && aisleId != null && rackId != null && levelId != null) {
                        Optional<Level> levelEntity = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                                warehouseId, zoneId, aisleId, rackId, levelId);
                        if (levelEntity.isPresent()) {
                            maxCapacity = levelEntity.get().getMaxCapacity() != null ? levelEntity.get().getMaxCapacity() : 0;
                            locationPath = warehouseId + "-" + zoneId + "-" + aisleId + "-" + rackId + "-" + levelId;
                            log.debug("Building empty level summary with capacity: {}", maxCapacity);
                        }
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error building empty stock summary with capacity for level: {}", level, e);
        }
        
        int availableSlots = maxCapacity > 0 ? maxCapacity : 0;
        
        return StockAvailabilitySummary.builder()
                .totalQuantity(0)
                .stockin(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .maxCapacity(maxCapacity > 0 ? maxCapacity : null)
                .minCapacity(0)
                .utilizationPercentage(0.0)
                .availableSlots(availableSlots)
                .occupiedSlots(0)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)
                .isHighStock(false)
                .stockStatus("EMPTY")
                .locationPath(locationPath)
                .locationLevel(level != null ? level.name() : null)
                .uniqueItemsCount(0)
                .items(new ArrayList<>())
                .lastPutawayDate(null)
                .lastPickDate(null)
                .totalBinsUsed(0)
                .totalBinsAvailable(0)
                .stockTurnoverRate(0.0)
                .build();
    }

    /**
     * Build bin summary with capacity from bin entity
     */
    private StockAvailabilitySummary buildBinSummary(List<StockAvailability> stocks, 
                                                       String binId, 
                                                       Integer maxCapFromBin, 
                                                       Integer minCapFromBin) {
        // First build summary from stocks if available
        StockAvailabilitySummary summary;
        if (stocks != null && !stocks.isEmpty()) {
            summary = buildSummary(stocks);
        } else {
            summary = buildEmptyStockSummary();
        }
        
        // Override maxCapacity if we have it from bin entity
        int maxCapacity = 0;
        if (maxCapFromBin != null && maxCapFromBin > 0) {
            maxCapacity = maxCapFromBin;
        } else if (summary.getMaxCapacity() != null && summary.getMaxCapacity() > 0) {
            maxCapacity = summary.getMaxCapacity();
        } else {
            // Try to get from bin repository directly
            try {
                Optional<Bin> bin = binRepository.findByBarcode(binId);
                if (bin.isPresent() && bin.get().getMaxCapacity() != null) {
                    maxCapacity = bin.get().getMaxCapacity();
                    log.debug("Got bin maxCapacity directly from repository: {}", maxCapacity);
                }
            } catch (Exception e) {
                log.warn("Error getting bin capacity directly for binId: {}", binId, e);
            }
        }
        
        int totalQty = summary.getTotalQuantity() != null ? summary.getTotalQuantity() : 0;
        int availableSlots = Math.max(0, maxCapacity - totalQty);
        
        Double utilization = null;
        if (maxCapacity > 0) {
            utilization = ((double) totalQty / maxCapacity) * 100;
            if (utilization > 100) {
                utilization = 100.0;
            }
        }
        
        boolean hasStock = totalQty > 0;
        boolean isFull = maxCapacity > 0 && totalQty >= maxCapacity;
        
        String stockStatus = "EMPTY";
        if (hasStock) {
            if (isFull) {
                stockStatus = "FULL";
            } else if (utilization != null && utilization > 80.0) {
                stockStatus = "HIGH";
            } else if (utilization != null && utilization < 20.0) {
                stockStatus = "LOW";
            } else {
                stockStatus = "NORMAL";
            }
        }
        
        return StockAvailabilitySummary.builder()
                .totalQuantity(totalQty)
                .stockin(summary.getStockin() != null ? summary.getStockin() : 0)
                .reservedQuantity(summary.getReservedQuantity() != null ? summary.getReservedQuantity() : 0)
                .inTransitQuantity(summary.getInTransitQuantity() != null ? summary.getInTransitQuantity() : 0)
                .maxCapacity(maxCapacity > 0 ? maxCapacity : null)
                .minCapacity(minCapFromBin != null ? minCapFromBin : (summary.getMinCapacity() != null ? summary.getMinCapacity() : 0))
                .utilizationPercentage(utilization)
                .availableSlots(availableSlots)
                .occupiedSlots(Math.min(maxCapacity, totalQty))
                .hasStock(hasStock)
                .isFull(isFull)
                .isAvailable(totalQty > 0)
                .isLowStock(hasStock && utilization != null && utilization < 20.0)
                .isHighStock(hasStock && utilization != null && utilization > 80.0)
                .stockStatus(stockStatus)
                .locationPath(summary.getLocationPath() != null ? summary.getLocationPath() : binId)
                .locationLevel(LocationLevel.BIN.name())
                .uniqueItemsCount(summary.getUniqueItemsCount() != null ? summary.getUniqueItemsCount() : 0)
                .items(summary.getItems() != null ? summary.getItems() : new ArrayList<>())
                .lastPutawayDate(summary.getLastPutawayDate())
                .lastPickDate(summary.getLastPickDate())
                .totalBinsUsed(summary.getTotalBinsUsed() != null ? summary.getTotalBinsUsed() : 0)
                .totalBinsAvailable(summary.getTotalBinsAvailable() != null ? summary.getTotalBinsAvailable() : 0)
                .stockTurnoverRate(summary.getStockTurnoverRate() != null ? summary.getStockTurnoverRate() : 0.0)
                .build();
    }

    private StockAvailabilitySummary buildSummary(List<StockAvailability> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return buildEmptyStockSummary();
        }

        int totalQty = 0;
        int stockin = 0;
        int reservedQty = 0;
        int inTransitQty = 0;
        int maxCapacity = 0;
        int minCapacity = 0;
        int binCount = 0;
        int totalBinsUsed = 0;
        
        List<ItemStockSummary> itemSummaries = new ArrayList<>();
        List<String> uniqueItems = new ArrayList<>();

        // First pass: Calculate totals from stock records
        for (StockAvailability stock : stocks) {
            totalQty += stock.getTotalQuantity() != null ? stock.getTotalQuantity() : 0;
            stockin += stock.getAvailableQuantity() != null ? stock.getAvailableQuantity() : 0;
            reservedQty += stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0;
            inTransitQty += stock.getInTransitQuantity() != null ? stock.getInTransitQuantity() : 0;
            
            if (stock.getItemCode() != null && !uniqueItems.contains(stock.getItemCode())) {
                uniqueItems.add(stock.getItemCode());
            }
            
            if (stock.getTotalQuantity() != null && stock.getTotalQuantity() > 0) {
                if (stock.getBinId() != null) {
                    totalBinsUsed++;
                }
            }
            
            boolean exists = itemSummaries.stream()
                    .anyMatch(item -> item.getItemCode().equals(stock.getItemCode()));
            
            if (!exists) {
                itemSummaries.add(ItemStockSummary.builder()
                        .itemCode(stock.getItemCode())
                        .itemName(stock.getItemName())
                        .uom(stock.getUom())
                        .totalQuantity(stock.getTotalQuantity())
                        .availableQuantity(stock.getAvailableQuantity())
                        .reservedQuantity(stock.getReservedQuantity())
                        .batchNumber(stock.getBatchNumber())
                        .build());
            }
        }

        // Get capacity from entity based on location level
        StockAvailability firstStock = stocks.get(0);
        LocationLevel level = firstStock.getLocationLevel();
        
        // Get capacity from entity
        Integer entityMaxCapacity = getCapacityFromEntity(firstStock);
        Integer entityMinCapacity = getMinCapacityFromEntity(firstStock);
        
        // If entity has capacity, use it
        if (entityMaxCapacity != null && entityMaxCapacity > 0) {
            maxCapacity = entityMaxCapacity;
            log.debug("Using entity maxCapacity: {} from {}", maxCapacity, level);
        } else {
            // Fallback: Try to get from stock records
            for (StockAvailability stock : stocks) {
                if (stock.getMaxCapacity() != null && stock.getMaxCapacity() > 0) {
                    if (stock.getMaxCapacity() > maxCapacity) {
                        maxCapacity = stock.getMaxCapacity();
                    }
                    binCount++;
                }
            }
            // If still 0, try to get from the entity directly
            if (maxCapacity == 0) {
                Integer directCapacity = getCapacityFromEntityDirect(firstStock);
                if (directCapacity != null && directCapacity > 0) {
                    maxCapacity = directCapacity;
                    log.debug("Found capacity directly from entity: {}", maxCapacity);
                }
            }
            log.debug("Using maxCapacity: {} (after fallback)", maxCapacity);
        }
        
        // If entity has min capacity, use it
        if (entityMinCapacity != null && entityMinCapacity > 0) {
            minCapacity = entityMinCapacity;
            log.debug("Using entity minCapacity: {} from {}", minCapacity, level);
        } else {
            // Fallback for min capacity
            for (StockAvailability stock : stocks) {
                if (stock.getMinCapacity() != null && stock.getMinCapacity() > 0) {
                    if (stock.getMinCapacity() > minCapacity) {
                        minCapacity = stock.getMinCapacity();
                    }
                }
            }
            log.debug("Using stock minCapacity: {} (no entity capacity found)", minCapacity);
        }

        // If still no capacity, try to get from stock's maxCapacity field directly
        if (maxCapacity == 0) {
            for (StockAvailability stock : stocks) {
                if (stock.getMaxCapacity() != null && stock.getMaxCapacity() > 0) {
                    maxCapacity = stock.getMaxCapacity();
                    break;
                }
            }
            if (maxCapacity == 0) {
                log.warn("No capacity found for level: {}, using default", level);
            }
        }

        // Calculate available slots
        int availableSlots = Math.max(0, maxCapacity - totalQty);
        int occupiedSlots = Math.min(maxCapacity, totalQty);
        
        Double utilization = null;
        if (maxCapacity > 0) {
            utilization = ((double) totalQty / maxCapacity) * 100;
            if (utilization > 100) {
                utilization = 100.0;
            }
        }

        boolean hasStock = totalQty > 0;
        boolean isAvailable = stockin > 0;
        boolean isFull = maxCapacity > 0 && totalQty >= maxCapacity;
        boolean isLowStock = hasStock && utilization != null && utilization < 20.0;
        boolean isHighStock = hasStock && utilization != null && utilization > 80.0;
        
        String stockStatus = "EMPTY";
        if (hasStock) {
            if (isFull) {
                stockStatus = "FULL";
            } else if (isHighStock) {
                stockStatus = "HIGH";
            } else if (isLowStock) {
                stockStatus = "LOW";
            } else {
                stockStatus = "NORMAL";
            }
        }

        String locationPath = firstStock.getFullLocationPath();
        String locationLevel = firstStock.getLocationLevel() != null ? 
                firstStock.getLocationLevel().name() : null;

        String lastPutawayDate = stocks.stream()
                .map(StockAvailability::getLastPutawayDate)
                .filter(d -> d != null)
                .max(LocalDateTime::compareTo)
                .map(d -> d.toString())
                .orElse(null);

        String lastPickDate = stocks.stream()
                .map(StockAvailability::getLastPickDate)
                .filter(d -> d != null)
                .max(LocalDateTime::compareTo)
                .map(d -> d.toString())
                .orElse(null);

        Double stockTurnoverRate = null;
        if (totalQty > 0 && stockin > 0) {
            stockTurnoverRate = (double) totalQty / stockin;
        }

        return StockAvailabilitySummary.builder()
                .totalQuantity(totalQty)
                .stockin(stockin)
                .reservedQuantity(reservedQty)
                .inTransitQuantity(inTransitQty)
                .maxCapacity(maxCapacity > 0 ? maxCapacity : null)
                .minCapacity(minCapacity)
                .utilizationPercentage(utilization)
                .availableSlots(availableSlots)
                .occupiedSlots(occupiedSlots)
                .hasStock(hasStock)
                .isAvailable(isAvailable)
                .isFull(isFull)
                .isLowStock(isLowStock)
                .isHighStock(isHighStock)
                .stockStatus(stockStatus)
                .locationPath(locationPath)
                .locationLevel(locationLevel)
                .uniqueItemsCount(uniqueItems.size())
                .items(itemSummaries)
                .lastPutawayDate(lastPutawayDate)
                .lastPickDate(lastPickDate)
                .totalBinsUsed(totalBinsUsed)
                .totalBinsAvailable(binCount > 0 ? binCount : null)
                .stockTurnoverRate(stockTurnoverRate)
                .build();
    }

    /**
     * Get capacity from entity directly (without using StockAvailability)
     */
    private Integer getCapacityFromEntityDirect(StockAvailability stock) {
        if (stock == null) {
            return 0;
        }
        
        LocationLevel level = stock.getLocationLevel();
        
        try {
            switch (level) {
                case WAREHOUSE:
                    Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(stock.getWarehouseId());
                    if (warehouse.isPresent()) {
                        Integer capacity = warehouse.get().getMaxCapacity();
                        return capacity != null ? capacity : (warehouse.get().getCapacity() != null ? warehouse.get().getCapacity() : 0);
                    }
                    break;
                    
                case ZONE:
                    Optional<Zone> zone = zoneRepository.findByWarehouseIdAndZoneId(
                            stock.getWarehouseId(), stock.getZoneId());
                    if (zone.isPresent()) {
                        return zone.get().getMaxCapacity() != null ? zone.get().getMaxCapacity() : 0;
                    }
                    break;
                    
                case AISLE:
                    Optional<Aisle> aisle = aisleRepository.findByWarehouseIdAndZoneIdAndAisleId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId());
                    if (aisle.isPresent()) {
                        return aisle.get().getMaxCapacity() != null ? aisle.get().getMaxCapacity() : 0;
                    }
                    break;
                    
                case RACK:
                    Optional<Rack> rack = rackRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), stock.getRackId());
                    if (rack.isPresent()) {
                        return rack.get().getMaxCapacity() != null ? rack.get().getMaxCapacity() : 0;
                    }
                    break;
                    
                case LEVEL:
                    Optional<Level> levelEntity = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), 
                            stock.getRackId(), stock.getLevelId());
                    if (levelEntity.isPresent()) {
                        return levelEntity.get().getMaxCapacity() != null ? levelEntity.get().getMaxCapacity() : 0;
                    }
                    break;
                    
                case BIN:
                    String binId = stock.getBinId();
                    if (binId != null) {
                        Optional<Bin> bin = binRepository.findByBarcode(binId);
                        if (bin.isPresent()) {
                            return bin.get().getMaxCapacity() != null ? bin.get().getMaxCapacity() : 0;
                        }
                    }
                    String binBarcode = stock.getBinBarcode();
                    if (binBarcode != null) {
                        Optional<Bin> bin = binRepository.findByBarcode(binBarcode);
                        if (bin.isPresent()) {
                            return bin.get().getMaxCapacity() != null ? bin.get().getMaxCapacity() : 0;
                        }
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error getting capacity directly from entity for level: {}", level, e);
        }
        
        return 0;
    }

    /**
     * Get capacity from entity based on location level
     */
    private Integer getCapacityFromEntity(StockAvailability stock) {
        if (stock == null) {
            return 0;
        }
        
        LocationLevel level = stock.getLocationLevel();
        
        try {
            switch (level) {
                case WAREHOUSE:
                    Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(stock.getWarehouseId());
                    if (warehouse.isPresent()) {
                        Integer capacity = warehouse.get().getMaxCapacity();
                        log.debug("Found warehouse maxCapacity: {} for warehouse: {}", capacity, stock.getWarehouseId());
                        return capacity != null ? capacity : (warehouse.get().getCapacity() != null ? warehouse.get().getCapacity() : 0);
                    }
                    break;
                    
                case ZONE:
                    Optional<Zone> zone = zoneRepository.findByWarehouseIdAndZoneId(
                            stock.getWarehouseId(), stock.getZoneId());
                    if (zone.isPresent()) {
                        Integer maxCap = zone.get().getMaxCapacity();
                        log.debug("Found zone maxCapacity: {} for zone: {}", maxCap, stock.getZoneId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                case AISLE:
                    Optional<Aisle> aisle = aisleRepository.findByWarehouseIdAndZoneIdAndAisleId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId());
                    if (aisle.isPresent()) {
                        Integer maxCap = aisle.get().getMaxCapacity();
                        log.debug("Found aisle maxCapacity: {} for aisle: {}", maxCap, stock.getAisleId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                case RACK:
                    Optional<Rack> rack = rackRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), stock.getRackId());
                    if (rack.isPresent()) {
                        Integer maxCap = rack.get().getMaxCapacity();
                        log.debug("Found rack maxCapacity: {} for rack: {}", maxCap, stock.getRackId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                case LEVEL:
                    Optional<Level> level1 = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), 
                            stock.getRackId(), stock.getLevelId());
                    if (level1.isPresent()) {
                        Integer maxCap = level1.get().getMaxCapacity();
                        log.debug("Found level maxCapacity: {} for level: {}", maxCap, stock.getLevelId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                case BIN:
                    String binId = stock.getBinId();
                    String binBarcode = stock.getBinBarcode();
                    
                    if (binId != null) {
                        Optional<Bin> bin = binRepository.findByBarcode(binId);
                        if (bin.isPresent() && bin.get().getMaxCapacity() != null) {
                            log.debug("Found bin maxCapacity by binId: {} for bin: {}", bin.get().getMaxCapacity(), binId);
                            return bin.get().getMaxCapacity();
                        }
                    }
                    
                    if (binBarcode != null && !binBarcode.equals(binId)) {
                        Optional<Bin> bin = binRepository.findByBarcode(binBarcode);
                        if (bin.isPresent() && bin.get().getMaxCapacity() != null) {
                            log.debug("Found bin maxCapacity by binBarcode: {} for bin: {}", bin.get().getMaxCapacity(), binBarcode);
                            return bin.get().getMaxCapacity();
                        }
                    }
                    
                    if (stock.getId() != null) {
                        Optional<Bin> bin = binRepository.findById(stock.getId().longValue());
                        if (bin.isPresent() && bin.get().getMaxCapacity() != null) {
                            log.debug("Found bin maxCapacity by ID: {} for bin: {}", bin.get().getMaxCapacity(), stock.getId());
                            return bin.get().getMaxCapacity();
                        }
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error getting capacity from entity for level: {}", level, e);
        }
        
        return 0;
    }

    /**
     * Get min capacity from entity based on location level
     */
    private Integer getMinCapacityFromEntity(StockAvailability stock) {
        if (stock == null) {
            return 0;
        }
        
        LocationLevel level = stock.getLocationLevel();
        
        try {
            switch (level) {
                case WAREHOUSE:
                    Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(stock.getWarehouseId());
                    if (warehouse.isPresent()) {
                        Integer minCap = warehouse.get().getMinCapacity();
                        log.debug("Found warehouse minCapacity: {} for warehouse: {}", minCap, stock.getWarehouseId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case ZONE:
                    Optional<Zone> zone = zoneRepository.findByWarehouseIdAndZoneId(
                            stock.getWarehouseId(), stock.getZoneId());
                    if (zone.isPresent()) {
                        Integer minCap = zone.get().getMinCapacity();
                        log.debug("Found zone minCapacity: {} for zone: {}", minCap, stock.getZoneId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case AISLE:
                    Optional<Aisle> aisle = aisleRepository.findByWarehouseIdAndZoneIdAndAisleId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId());
                    if (aisle.isPresent()) {
                        Integer minCap = aisle.get().getMinCapacity();
                        log.debug("Found aisle minCapacity: {} for aisle: {}", minCap, stock.getAisleId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case RACK:
                    Optional<Rack> rack = rackRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), stock.getRackId());
                    if (rack.isPresent()) {
                        Integer minCap = rack.get().getMinCapacity();
                        log.debug("Found rack minCapacity: {} for rack: {}", minCap, stock.getRackId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case LEVEL:
                    Optional<Level> level2 = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), 
                            stock.getRackId(), stock.getLevelId());
                    if (level2.isPresent()) {
                        Integer minCap = level2.get().getMinCapacity();
                        log.debug("Found level minCapacity: {} for level: {}", minCap, stock.getLevelId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case BIN:
                    String binId = stock.getBinId();
                    String binBarcode = stock.getBinBarcode();
                    
                    if (binId != null) {
                        Optional<Bin> bin = binRepository.findByBarcode(binId);
                        if (bin.isPresent() && bin.get().getMinCapacity() != null) {
                            log.debug("Found bin minCapacity by binId: {} for bin: {}", bin.get().getMinCapacity(), binId);
                            return bin.get().getMinCapacity();
                        }
                    }
                    
                    if (binBarcode != null && !binBarcode.equals(binId)) {
                        Optional<Bin> bin = binRepository.findByBarcode(binBarcode);
                        if (bin.isPresent() && bin.get().getMinCapacity() != null) {
                            log.debug("Found bin minCapacity by binBarcode: {} for bin: {}", bin.get().getMinCapacity(), binBarcode);
                            return bin.get().getMinCapacity();
                        }
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error getting min capacity from entity for level: {}", level, e);
        }
        
        return 0;
    }

    private StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                .totalQuantity(0)
                .stockin(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                .maxCapacity(null)
                .minCapacity(0)
                .utilizationPercentage(0.0)
                .availableSlots(0)
                .occupiedSlots(0)
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)
                .isHighStock(false)
                .stockStatus("EMPTY")
                .locationPath(null)
                .locationLevel(null)
                .uniqueItemsCount(0)
                .items(new ArrayList<>())
                .lastPutawayDate(null)
                .lastPickDate(null)
                .totalBinsUsed(0)
                .totalBinsAvailable(0)
                .stockTurnoverRate(0.0)
                .build();
    }
}