// ====== FILE: src/main/java/com/warehouse/wms/service/TrolleyService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.TrolleyRequest;
import com.warehouse.wms.dto.response.TrolleyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TrolleyService {

    // ====== Create ======
    TrolleyResponse createTrolley(TrolleyRequest request);

    // ====== Read ======
    TrolleyResponse getTrolleyById(Long id);

    TrolleyResponse getTrolleyByIdentifier(String trolleyIdentifier);

    Page<TrolleyResponse> getAllTrolleys(Pageable pageable, String search, String status);

    List<TrolleyResponse> getTrolleysByStatus(String status);

    List<TrolleyResponse> getTrolleysByType(String trolleyType);

    List<TrolleyResponse> getAvailableTrolleys(Integer requiredWeight);

    List<TrolleyResponse> getTrolleysDueForMaintenance();

    // ====== Update ======
    TrolleyResponse updateTrolley(Long id, TrolleyRequest request);

    TrolleyResponse updateTrolleyStatus(Long id, String status);

    TrolleyResponse addTrolleyLoad(Long id, Integer weight);

    TrolleyResponse removeTrolleyLoad(Long id, Integer weight);

    TrolleyResponse toggleTrolleyStatus(Long id, Boolean isActive);

    // ====== Delete ======
    void deleteTrolley(Long id);

    void deleteTrolleyByIdentifier(String trolleyIdentifier);
}