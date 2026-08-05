// ====== FILE: src/main/java/com/warehouse/wms/service/ZoneService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.ZoneRequest;
import com.warehouse.wms.dto.response.ZoneResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ZoneService {

    // ====== Create ======
    ZoneResponse createZone(ZoneRequest request);

    // ====== Read ======
    ZoneResponse getZoneById(Long id);

    ZoneResponse getZoneByZoneId(String zoneId);

    Page<ZoneResponse> getAllZones(Pageable pageable, String search, Long warehouseId);

    List<ZoneResponse> getZonesByWarehouse(Long warehouseId);

    List<ZoneResponse> getActiveZonesByWarehouse(Long warehouseId);

    List<ZoneResponse> getZonesByType(String zoneType);

    // ====== Update ======
    ZoneResponse updateZone(Long id, ZoneRequest request);

    ZoneResponse toggleZoneStatus(Long id, Boolean isActive);

    // ====== Delete ======
    void deleteZone(Long id);

    void deleteZoneByZoneId(String zoneId);
}