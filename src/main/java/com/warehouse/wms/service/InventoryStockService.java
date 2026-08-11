package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.constant.InventoryStatus;
import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.dto.request.InventoryStockRequest;
import com.warehouse.wms.dto.response.InventoryStockResponse;

public interface InventoryStockService {
    
    InventoryStockResponse createStock(InventoryStockRequest request);
    
    InventoryStockResponse updateStock(Long id, InventoryStockRequest request);
    
    InventoryStockResponse getStockById(Long id);
    
    InventoryStockResponse getStockByInventoryNumber(String inventoryNumber);
    
    InventoryStockResponse getStockByItemAndBin(String itemCode, String binId);
    
//    Page<InventoryStockResponse> getAllStocks(Pageable pageable);
    
    Page<InventoryStockResponse> getAllStocks(
            String search,
            String itemCode,
            String itemName,
            InventoryStatus status,
            String warehouseId,
            String zone,
            String aisle,
            String rack,
            String level,
            String binId,
            String batchNumber,
            String grnNumber,
            Boolean isAvailable,
            Boolean isAllocated,
            Boolean isFrozen,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer minQuantity,
            Integer maxQuantity,
            Pageable pageable);
    
    List<InventoryStockResponse> getStocksByItemCode(String itemCode);
    
    List<InventoryStockResponse> getStocksByBinId(String binId);
    
    List<InventoryStockResponse> getStocksByWarehouseId(String warehouseId);
    
    Page<InventoryStockResponse> searchStocks(InventorySearchRequest searchRequest, Pageable pageable);
    
    void deleteStock(Long id);
    
    void addQuantity(Long id, Integer quantity);
    
    void removeQuantity(Long id, Integer quantity);
    
    void reserveStock(Long id, Integer quantity);
    
    void unreserveStock(Long id, Integer quantity);
    
    void updateStockStatus(Long id, String status);
    
    void freezeStock(Long id);
    
    void unfreezeStock(Long id);
    
    List<InventoryStockResponse> getLowStockItems();
}