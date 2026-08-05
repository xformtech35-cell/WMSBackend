// ====== FILE: src/main/java/com/warehouse/wms/service/RackCompartmentService.java ======
package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.RackCompartmentRequest;
import com.warehouse.wms.dto.response.RackCompartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RackCompartmentService {

    // ====== Create ======
    RackCompartmentResponse createCompartment(RackCompartmentRequest request);

    // ====== Read ======
    RackCompartmentResponse getCompartmentById(Long id);

    RackCompartmentResponse getCompartmentByCompartmentId(String compartmentId);

    Page<RackCompartmentResponse> getAllCompartments(Pageable pageable, String search, Long rackId);

    List<RackCompartmentResponse> getCompartmentsByRack(Long rackId);

    List<RackCompartmentResponse> getActiveCompartmentsByRack(Long rackId);

    List<RackCompartmentResponse> getCompartmentsByTrolley(Long trolleyId);

    List<RackCompartmentResponse> getCompartmentsBySalesOrder(Long salesOrderId);

    List<RackCompartmentResponse> getAvailableCompartments(Long rackId, Integer requiredCapacity);

    // ====== Update ======
    RackCompartmentResponse updateCompartment(Long id, RackCompartmentRequest request);

    RackCompartmentResponse toggleCompartmentStatus(Long id, Boolean isActive);

    RackCompartmentResponse allocateCapacity(Long id, Integer quantity);

    RackCompartmentResponse releaseCapacity(Long id, Integer quantity);

    // ====== Delete ======
    void deleteCompartment(Long id);

    void deleteCompartmentByCompartmentId(String compartmentId);
}