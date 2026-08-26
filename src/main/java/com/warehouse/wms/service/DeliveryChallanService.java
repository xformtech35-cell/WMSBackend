package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.DeliveryChallanRequest;
import com.warehouse.wms.dto.response.DeliveryChallanResponse;
import com.warehouse.wms.dto.response.DeliveryChallanSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryChallanService {

    // ====== CRUD ======
    DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request);
    
    DeliveryChallanResponse updateDeliveryChallan(String challanNumber, DeliveryChallanRequest request);

    DeliveryChallanResponse getDeliveryChallanByNumber(String challanNumber);
    Page<DeliveryChallanResponse> getAllDeliveryChallans(Pageable pageable);

    // ====== Filters ======
    Page<DeliveryChallanResponse> getAllDeliveryChallansWithFilters(
            String challanNumber,
            String shipmentNumber,
            String transporter,
            String vehicleNumber,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    Page<DeliveryChallanResponse> searchDeliveryChallans(String search, Pageable pageable);
    List<DeliveryChallanResponse> getDeliveryChallansByStatus(String status);

    // ====== Status Management ======
    DeliveryChallanResponse updateDeliveryChallanStatus(String challanNumber, String status);
    DeliveryChallanResponse printDeliveryChallan(String challanNumber);
    DeliveryChallanResponse markAsDispatched(String challanNumber);
    DeliveryChallanResponse markAsDelivered(String challanNumber);
    DeliveryChallanResponse cancelDeliveryChallan(String challanNumber);

    // ====== PDF/HTML ======
//    byte[] generateDeliveryChallanPdf(String challanNumber);
//    String generateDeliveryChallanHtml(String challanNumber);

    // ====== Summary ======
    DeliveryChallanSummaryResponse getDeliveryChallanSummary();

    // ====== Delete ======
    void deleteDeliveryChallan(String challanNumber);
}