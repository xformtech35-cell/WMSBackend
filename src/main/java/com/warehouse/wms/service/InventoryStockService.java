package com.warehouse.wms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.InventorySearchRequest;
import com.warehouse.wms.dto.request.InventoryStockRequest;
import com.warehouse.wms.dto.response.InventoryStockResponse;

public interface InventoryStockService {
    
    InventoryStockResponse createStock(InventoryStockRequest request);
    
    InventoryStockResponse updateStock(Long id, InventoryStockRequest request);
    
    InventoryStockResponse getStockById(Long id);
    
    InventoryStockResponse getStockByInventoryNumber(String inventoryNumber);
    
    InventoryStockResponse getStockByItemAndBin(String itemCode, String binId);
    
    Page<InventoryStockResponse> getAllStocks(Pageable pageable);
    
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