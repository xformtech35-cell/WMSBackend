// ====== FILE: src/main/java/com/warehouse/wms/service/WarehouseService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.WarehouseRequest;
import com.warehouse.wms.dto.response.WarehouseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse getWarehouseById(Long id);

    WarehouseResponse getWarehouseByWarehouseId(String warehouseId);

    Page<WarehouseResponse> getAllWarehouses(Pageable pageable, String search);

    List<WarehouseResponse> getActiveWarehouses();

    WarehouseResponse updateWarehouse(Long id, WarehouseRequest request);

    void deleteWarehouse(Long id);

    void toggleWarehouseStatus(Long id, Boolean isActive);
}