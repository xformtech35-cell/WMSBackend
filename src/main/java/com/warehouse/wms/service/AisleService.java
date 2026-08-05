// ====== FILE: src/main/java/com/warehouse/wms/service/AisleService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.AisleRequest;
import com.warehouse.wms.dto.response.AisleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AisleService {

    // ====== Create ======
    AisleResponse createAisle(AisleRequest request);

    // ====== Read ======
    AisleResponse getAisleById(Long id);

    AisleResponse getAisleByAisleId(String aisleId);

    Page<AisleResponse> getAllAisles(Pageable pageable, String search, Long zoneId);

    List<AisleResponse> getAislesByZone(Long zoneId);

    List<AisleResponse> getActiveAislesByZone(Long zoneId);

    List<AisleResponse> getAislesByZoneId(String zoneId);

    List<AisleResponse> getAislesByWarehouse(Long warehouseId);

    // ====== Update ======
    AisleResponse updateAisle(Long id, AisleRequest request);

    AisleResponse toggleAisleStatus(Long id, Boolean isActive);

    // ====== Delete ======
    void deleteAisle(Long id);

    void deleteAisleByAisleId(String aisleId);
}