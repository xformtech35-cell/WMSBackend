// ====== FILE: src/main/java/com/warehouse/wms/service/BinLocationService.java ======
package com.warehouse.wms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.BinLocationRequest;
import com.warehouse.wms.dto.response.BinLocationResponse;
import com.warehouse.wms.dto.response.BinLocationStatistics;

public interface BinLocationService {

    // ====== Create ======
    
    BinLocationResponse createBinLocation(BinLocationRequest request);
    
    List<BinLocationResponse> createBatchBinLocations(List<BinLocationRequest> requests);
    
    // ====== Read ======
    
    BinLocationResponse getBinLocationById(Long id);
    
    BinLocationResponse getBinLocationByBinId(String binId);
    
    BinLocationResponse getBinLocationByBarcode(String binBarcode);
    
    Page<BinLocationResponse> getAllBinLocations(Pageable pageable, String warehouseId);
    
    List<BinLocationResponse> getBinLocationsByWarehouse(String warehouseId);
    
    List<BinLocationResponse> getBinLocationsByWarehouseAndZone(String warehouseId, String zone);
    
    List<BinLocationResponse> getAvailableBinLocations(String warehouseId, String zone);
    
    List<BinLocationResponse> getOccupiedBinLocations(String warehouseId);
    
    // ====== Update ======
    
    BinLocationResponse updateBinLocation(Long id, BinLocationRequest request);
    
    BinLocationResponse allocateBinCapacity(Long id, Integer quantity, String itemCode, String itemName, String uom);
    
    BinLocationResponse releaseBinCapacity(Long id, Integer quantity);
    
    BinLocationResponse toggleActiveStatus(Long id, Boolean isActive);
    
    // ====== Delete ======
    
    void deleteBinLocation(Long id);
    
    void deleteBinLocationByBinId(String binId);
    
    // ====== Statistics ======
    
    BinLocationStatistics getBinLocationStatistics(String warehouseId);
}