// ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.response.ItemStockSummary;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
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
        
        return buildSummary(stocks);
    }

    @Override
    public StockAvailabilitySummary getBinStockSummary(String binId) {
        log.debug("Getting bin stock summary for: {}", binId);
        
        if (binId == null) {
            return buildEmptyStockSummary();
        }
        
        List<StockAvailability> stocks = new ArrayList<>();
        
        try {
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
            
        } catch (Exception e) {
            log.error("Error fetching bin stock summary for binId: {}", binId, e);
        }
        
        return buildSummary(stocks);
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

    // ====== Private Helper Methods ======

   // ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======

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

    // ✅ First pass: Calculate totals from stock records
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

    // ✅ Get capacity from entity (PRIORITY 1 - use entity capacity)
    StockAvailability firstStock = stocks.get(0);
    LocationLevel level = firstStock.getLocationLevel();
    
    // Get capacity from entity
    Integer entityMaxCapacity = getCapacityFromEntity(firstStock);
    Integer entityMinCapacity = getMinCapacityFromEntity(firstStock);
    
    // ✅ If entity has capacity, use it (this is the fix!)
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
        log.debug("Using stock maxCapacity: {} (no entity capacity found)", maxCapacity);
    }
    
    // ✅ If entity has min capacity, use it
    if (entityMinCapacity != null && entityMinCapacity > 0) {
        minCapacity = entityMinCapacity;
        log.debug("Using entity minCapacity: {} from {}", minCapacity, level);
    } else {
        // Fallback: Try to get from stock records
        for (StockAvailability stock : stocks) {
            if (stock.getMinCapacity() != null && stock.getMinCapacity() > 0) {
                if (stock.getMinCapacity() > minCapacity) {
                    minCapacity = stock.getMinCapacity();
                }
            }
        }
        log.debug("Using stock minCapacity: {} (no entity capacity found)", minCapacity);
    }

    // If still no capacity, use 0
    if (maxCapacity == 0) {
        maxCapacity = 0;
        log.warn("No capacity found for level: {}", level);
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
                    return capacity != null ? capacity : 0;
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
                Optional<Bin> bin = binRepository.findByBarcode(stock.getBinId());
                if (bin.isPresent()) {
                    Integer maxCap = bin.get().getMaxCapacity();
                    log.debug("Found bin maxCapacity: {} for bin: {}", maxCap, stock.getBinId());
                    return maxCap != null ? maxCap : 0;
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
                Optional<Bin> bin = binRepository.findByBarcode(stock.getBinId());
                if (bin.isPresent()) {
                    Integer minCap = bin.get().getMinCapacity();
                    log.debug("Found bin minCapacity: {} for bin: {}", minCap, stock.getBinId());
                    return minCap != null ? minCap : 0;
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

    /**
     * Get capacity from entity based on location level
     */
    private Integer getCapacityFromEntity(List<StockAvailability> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return 0;
        }
        
        StockAvailability stock = stocks.get(0);
        LocationLevel level = stock.getLocationLevel();
        
        try {
            switch (level) {
                case WAREHOUSE:
                    Optional<Warehouse> warehouse = warehouseRepository.findByWarehouseId(stock.getWarehouseId());
                    if (warehouse.isPresent()) {
                        // ✅ Use capacity field, not maxCapacity
                        Integer capacity = warehouse.get().getCapacity();
                        log.debug("Found warehouse capacity: {} for warehouse: {}", capacity, stock.getWarehouseId());
                        return capacity != null ? capacity : 0;
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
                    Optional<Level> level2 = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), 
                            stock.getRackId(), stock.getLevelId());
                    if (level2.isPresent()) {
                        Integer maxCap = level2.get().getMaxCapacity();
                        log.debug("Found level maxCapacity: {} for level: {}", maxCap, stock.getLevelId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                case BIN:
                    Optional<Bin> bin = binRepository.findByBarcode(stock.getBinId());
                    if (bin.isPresent()) {
                        Integer maxCap = bin.get().getMaxCapacity();
                        log.debug("Found bin maxCapacity: {} for bin: {}", maxCap, stock.getBinId());
                        return maxCap != null ? maxCap : 0;
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error getting capacity from entity for level: {}", level, e);
        }
        
        // ✅ Return 0 if not found (no hardcoded defaults)
        return 0;
    }

    /**
     * Get min capacity from entity based on location level
     */
    private Integer getMinCapacityFromEntity(List<StockAvailability> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return 0;
        }
        
        StockAvailability stock = stocks.get(0);
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
                    Optional<Level> level1 = levelRepository.findByWarehouseIdAndZoneIdAndAisleIdAndRackIdAndLevelId(
                            stock.getWarehouseId(), stock.getZoneId(), stock.getAisleId(), 
                            stock.getRackId(), stock.getLevelId());
                    if (level1.isPresent()) {
                        Integer minCap = level1.get().getMinCapacity();
                        log.debug("Found level minCapacity: {} for level: {}", minCap, stock.getLevelId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                case BIN:
                    Optional<Bin> bin = binRepository.findByBarcode(stock.getBinId());
                    if (bin.isPresent()) {
                        Integer minCap = bin.get().getMinCapacity();
                        log.debug("Found bin minCapacity: {} for bin: {}", minCap, stock.getBinId());
                        return minCap != null ? minCap : 0;
                    }
                    break;
                    
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error getting min capacity from entity for level: {}", level, e);
        }
        
        // ✅ Return 0 if not found (no hardcoded defaults)
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