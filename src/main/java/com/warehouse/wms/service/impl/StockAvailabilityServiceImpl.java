// ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======
package com.warehouse.wms.service.impl;

import com.warehouse.wms.dto.response.ItemStockSummary;
import com.warehouse.wms.dto.response.StockAvailabilitySummary;
import com.warehouse.wms.entity.StockAvailability;
import com.warehouse.wms.entity.StockAvailability.LocationLevel;
import com.warehouse.wms.repository.StockAvailabilityRepository;
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

// ====== FILE: src/main/java/com/warehouse/wms/service/impl/StockAvailabilityServiceImpl.java ======

@Override
public StockAvailabilitySummary getBinStockSummary(String binId) {
    log.debug("Getting bin stock summary for: {}", binId);
    
    if (binId == null) {
        return buildEmptyStockSummary();
    }
    
    List<StockAvailability> stocks = new ArrayList<>();
    
    try {
        // ✅ Method 1: Find by bin_id - returns List<StockAvailability>
        List<StockAvailability> byBinId = stockAvailabilityRepository
                .findByBinIdAndItemCode(binId, null);
        
        if (byBinId != null && !byBinId.isEmpty()) {
            stocks = byBinId;
            log.debug("Found {} stock records by bin_id: {}", stocks.size(), binId);
        }
        
        // Method 2: If no results, try by bin_barcode
        if (stocks.isEmpty()) {
            List<StockAvailability> byBinBarcode = stockAvailabilityRepository
                    .findByBinBarcodeAndItemCode(binId, null);
            if (byBinBarcode != null && !byBinBarcode.isEmpty()) {
                stocks = byBinBarcode;
                log.debug("Found {} stock records by bin_barcode: {}", stocks.size(), binId);
            }
        }
        
        // Method 3: Manual filtering as fallback
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
    
    // ✅ Always return a summary
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
        
        // Check at BIN level first
        if (binId != null) {
            Optional<Integer> binAvailable = stockAvailabilityRepository
                    .getAvailableQuantityInBin(binId, itemCode);
            if (binAvailable.isPresent() && binAvailable.get() >= requiredQuantity) {
                return true;
            }
        }

        // Check at LEVEL level
        if (levelId != null) {
            Optional<StockAvailability> levelStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, null, 
                            itemCode, LocationLevel.LEVEL);
            if (levelStock.isPresent() && levelStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        // Check at RACK level
        if (rackId != null) {
            Optional<StockAvailability> rackStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, null, null, 
                            itemCode, LocationLevel.RACK);
            if (rackStock.isPresent() && rackStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        // Check at AISLE level
        if (aisleId != null) {
            Optional<StockAvailability> aisleStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, null, null, null, 
                            itemCode, LocationLevel.AISLE);
            if (aisleStock.isPresent() && aisleStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        // Check at ZONE level
        if (zoneId != null) {
            Optional<StockAvailability> zoneStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, null, null, null, null, 
                            itemCode, LocationLevel.ZONE);
            if (zoneStock.isPresent() && zoneStock.get().getAvailableQuantity() >= requiredQuantity) {
                return true;
            }
        }

        // Check at WAREHOUSE level
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
        
        // Check at BIN level first
        if (binId != null) {
            Optional<Integer> binAvailable = stockAvailabilityRepository
                    .getAvailableQuantityInBin(binId, itemCode);
            if (binAvailable.isPresent()) {
                return binAvailable.get();
            }
        }

        // Check at LEVEL level
        if (levelId != null) {
            Optional<StockAvailability> levelStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, levelId, null, 
                            itemCode, LocationLevel.LEVEL);
            if (levelStock.isPresent()) {
                return levelStock.get().getAvailableQuantity();
            }
        }

        // Check at RACK level
        if (rackId != null) {
            Optional<StockAvailability> rackStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, rackId, null, null, 
                            itemCode, LocationLevel.RACK);
            if (rackStock.isPresent()) {
                return rackStock.get().getAvailableQuantity();
            }
        }

        // Check at AISLE level
        if (aisleId != null) {
            Optional<StockAvailability> aisleStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, aisleId, null, null, null, 
                            itemCode, LocationLevel.AISLE);
            if (aisleStock.isPresent()) {
                return aisleStock.get().getAvailableQuantity();
            }
        }

        // Check at ZONE level
        if (zoneId != null) {
            Optional<StockAvailability> zoneStock = stockAvailabilityRepository
                    .findByLocationAndItem(warehouseId, zoneId, null, null, null, null, 
                            itemCode, LocationLevel.ZONE);
            if (zoneStock.isPresent()) {
                return zoneStock.get().getAvailableQuantity();
            }
        }

        // Check at WAREHOUSE level
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
    int capacityFound = 0;
    
    // ✅ Use the MAX maxCapacity value from all stock records
    Integer binMaxCapacity = null;
    Integer binMinCapacity = null;
    
    List<ItemStockSummary> itemSummaries = new ArrayList<>();
    List<String> uniqueItems = new ArrayList<>();

    for (StockAvailability stock : stocks) {
        totalQty += stock.getTotalQuantity() != null ? stock.getTotalQuantity() : 0;
        stockin += stock.getAvailableQuantity() != null ? stock.getAvailableQuantity() : 0;
        reservedQty += stock.getReservedQuantity() != null ? stock.getReservedQuantity() : 0;
        inTransitQty += stock.getInTransitQuantity() != null ? stock.getInTransitQuantity() : 0;
        
        // Track unique items
        if (stock.getItemCode() != null && !uniqueItems.contains(stock.getItemCode())) {
            uniqueItems.add(stock.getItemCode());
        }
        
        // ✅ Take the MAX maxCapacity (should be same for all items in same bin)
        if (stock.getMaxCapacity() != null && stock.getMaxCapacity() > 0) {
            if (binMaxCapacity == null || stock.getMaxCapacity() > binMaxCapacity) {
                binMaxCapacity = stock.getMaxCapacity();
            }
            capacityFound++;
            binCount++;
        }
        
        // ✅ Take the MAX minCapacity
        if (stock.getMinCapacity() != null && stock.getMinCapacity() > 0) {
            if (binMinCapacity == null || stock.getMinCapacity() > binMinCapacity) {
                binMinCapacity = stock.getMinCapacity();
            }
        }
        
        // Count bins used
        if (stock.getTotalQuantity() != null && stock.getTotalQuantity() > 0) {
            if (stock.getBinId() != null) {
                totalBinsUsed++;
            }
        }
        
        // Add item summary (avoid duplicates)
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

    // ✅ Use the MAX maxCapacity
    if (binMaxCapacity != null) {
        maxCapacity = binMaxCapacity;
    } else {
        // If no capacity found, use default based on location level
        String locationLevel = stocks.get(0).getLocationLevel() != null ? 
                stocks.get(0).getLocationLevel().name() : "UNKNOWN";
        maxCapacity = getDefaultCapacityForLevel(locationLevel);
        minCapacity = getDefaultMinCapacityForLevel(locationLevel);
    }
    
    // ✅ Use the MAX minCapacity
    if (binMinCapacity != null) {
        minCapacity = binMinCapacity;
    }

    // Calculate available slots (maxCapacity - totalQuantity)
    int availableSlots = Math.max(0, maxCapacity - totalQty);
    int occupiedSlots = Math.min(maxCapacity, totalQty);
    
    // Calculate utilization
    Double utilization = null;
    if (maxCapacity > 0) {
        utilization = ((double) totalQty / maxCapacity) * 100;
        if (utilization > 100) {
            utilization = 100.0;
        }
    }

    // Determine stock status
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

    // Get location path from first stock
    String locationPath = stocks.get(0).getFullLocationPath();
    String locationLevel = stocks.get(0).getLocationLevel() != null ? 
            stocks.get(0).getLocationLevel().name() : null;

    // Get timestamps
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

    // Calculate stock turnover rate
    Double stockTurnoverRate = null;
    if (totalQty > 0 && stockin > 0) {
        stockTurnoverRate = (double) totalQty / stockin;
    }

    return StockAvailabilitySummary.builder()
            // Stock counts
            .totalQuantity(totalQty)
            .stockin(stockin)
            .reservedQuantity(reservedQty)
            .inTransitQuantity(inTransitQty)
            
            // Capacity - use MAX value
            .maxCapacity(maxCapacity > 0 ? maxCapacity : null)
            .minCapacity(minCapacity > 0 ? minCapacity : null)
            .utilizationPercentage(utilization)
            .availableSlots(availableSlots)
            .occupiedSlots(occupiedSlots)
            
            // Stock status
            .hasStock(hasStock)
            .isAvailable(isAvailable)
            .isFull(isFull)
            .isLowStock(isLowStock)
            .isHighStock(isHighStock)
            .stockStatus(stockStatus)
            
            // Location
            .locationPath(locationPath)
            .locationLevel(locationLevel)
            
            // Items
            .uniqueItemsCount(uniqueItems.size())
            .items(itemSummaries)
            
            // Timestamps
            .lastPutawayDate(lastPutawayDate)
            .lastPickDate(lastPickDate)
            
            // Summary
            .totalBinsUsed(totalBinsUsed)
            .totalBinsAvailable(binCount > 0 ? binCount : null)
            .stockTurnoverRate(stockTurnoverRate)
            
            .build();
}

    private Integer getDefaultCapacityForLevel(String locationLevel) {
        if (locationLevel == null) return 1000;
        switch (locationLevel.toUpperCase()) {
            case "WAREHOUSE": return 100000;
            case "ZONE": return 50000;
            case "AISLE": return 10000;
            case "RACK": return 5000;
            case "LEVEL": return 1000;
            case "BIN": return 100;
            default: return 1000;
        }
    }

    private Integer getDefaultMinCapacityForLevel(String locationLevel) {
        if (locationLevel == null) return 0;
        switch (locationLevel.toUpperCase()) {
            case "WAREHOUSE": return 0;
            case "ZONE": return 0;
            case "AISLE": return 0;
            case "RACK": return 0;
            case "LEVEL": return 0;
            case "BIN": return 10;
            default: return 0;
        }
    }

    private StockAvailabilitySummary buildEmptyStockSummary() {
        return StockAvailabilitySummary.builder()
                // Stock counts
                .totalQuantity(0)
                .stockin(0)
                .reservedQuantity(0)
                .inTransitQuantity(0)
                
                // Capacity
                .maxCapacity(null)
                .minCapacity(null)
                .utilizationPercentage(0.0)
                .availableSlots(0)
                .occupiedSlots(0)
                
                // Stock status
                .hasStock(false)
                .isFull(false)
                .isAvailable(false)
                .isLowStock(false)
                .isHighStock(false)
                .stockStatus("EMPTY")
                
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
                .totalBinsUsed(0)
                .totalBinsAvailable(0)
                .stockTurnoverRate(0.0)
                
                .build();
    }
}