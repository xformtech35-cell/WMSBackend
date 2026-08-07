// ====== FILE: src/main/java/com/warehouse/wms/service/RockService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.RockRequest;
import com.warehouse.wms.dto.response.RockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RockService {

    // ====== Create ======
    RockResponse createRock(RockRequest request);

    // ====== Read ======
    RockResponse getRockById(Long id);

    RockResponse getRockByRockId(String rockId);

    Page<RockResponse> getAllRocks(Pageable pageable, String search, Long warehouseId);

    List<RockResponse> getRocksByWarehouse(Long warehouseId);

    List<RockResponse> getActiveRocksByWarehouse(Long warehouseId);

    List<RockResponse> getRocksByType(String rockType);

    List<RockResponse> getLowStockRocks();

    List<RockResponse> getOverStockRocks();

    // ====== Update ======
    RockResponse updateRock(Long id, RockRequest request);

    RockResponse toggleRockStatus(Long id, Boolean isActive);

    RockResponse addRockQuantity(Long id, Integer quantity);

    RockResponse deductRockQuantity(Long id, Integer quantity);

    // ====== Delete ======
    void deleteRock(Long id);

    void deleteRockByRockId(String rockId);
}