package com.warehouse.wms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.InventoryResponse;
import com.warehouse.wms.dto.request.InventoryRequest;
import com.warehouse.wms.dto.request.InventorySearchRequest;

public interface InventoryService {
    
    InventoryResponse createInventory(InventoryRequest request);
    
    InventoryResponse updateInventory(Long id, InventoryRequest request);
    
    InventoryResponse getInventoryById(Long id);
    
    InventoryResponse getInventoryBySerialNo(String serialNo);
    
    Page<InventoryResponse> getAllInventories(Pageable pageable);
    
    List<InventoryResponse> getInventoriesByItemCode(String itemCode);
    
    List<InventoryResponse> getInventoriesBySkuId(Long skuId);
    
    List<InventoryResponse> getInventoriesByBinId(Long binId);
    
    Page<InventoryResponse> searchInventories(InventorySearchRequest searchRequest, Pageable pageable);
    
    void deleteInventory(Long id);
    
    void updateInventoryState(Long id, String state);
    
    void reserveInventory(Long id, Integer quantity);
    
    void unreserveInventory(Long id, Integer quantity);
}