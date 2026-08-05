// ====== FILE: src/main/java/com/warehouse/wms/service/RackService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.RackRequest;
import com.warehouse.wms.dto.response.RackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RackService {

    // ====== Create ======
    RackResponse createRack(RackRequest request);

    // ====== Read ======
    RackResponse getRackById(Long id);

    RackResponse getRackByRackId(String rackId);

    Page<RackResponse> getAllRacks(Pageable pageable, String search, Long aisleId);

    List<RackResponse> getRacksByAisle(Long aisleId);

    List<RackResponse> getActiveRacksByAisle(Long aisleId);

    List<RackResponse> getRacksByAisleId(String aisleId);

    List<RackResponse> getRacksByZone(Long zoneId);

    List<RackResponse> getRacksByWarehouse(Long warehouseId);

    // ====== Update ======
    RackResponse updateRack(Long id, RackRequest request);

    RackResponse toggleRackStatus(Long id, Boolean isActive);

    // ====== Delete ======
    void deleteRack(Long id);

    void deleteRackByRackId(String rackId);
}