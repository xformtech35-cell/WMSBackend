// ====== FILE: src/main/java/com/warehouse/wms/service/WarehouseService.java ======
package com.warehouse.wms.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.warehouse.wms.dto.request.WarehouseFilterRequest;
import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;

public interface WarehouseService {

    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse getWarehouseById(Long id);

    WarehouseResponse getWarehouseByWarehouseId(String warehouseId);

    Page<WarehouseResponse> getAllWarehouses(Pageable pageable, String search);

    List<WarehouseResponse> getActiveWarehouses();

    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);

    void deleteWarehouse(Long id);

    void toggleWarehouseStatus(Long id, Boolean isActive);
    
    
    
    
    Page<WarehouseResponse> getWarehousesWithFullHierarchy(WarehouseFilterRequest filter, Pageable pageable);
    WarehouseResponse getWarehouseWithFullHierarchy(Long warehouseId);
//    Page<WarehouseResponse> searchWarehouses(String searchTerm, Pageable pageable);
}