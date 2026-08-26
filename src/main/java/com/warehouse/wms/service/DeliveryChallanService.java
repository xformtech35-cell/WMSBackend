package com.warehouse.wms.service;

import com.warehouse.wms.dto.request.DeliveryChallanRequest;
import com.warehouse.wms.dto.response.DeliveryChallanResponse;
import com.warehouse.wms.dto.response.DeliveryChallanSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryChallanService {

    // ====== CRUD Operations ======
    
    /**
     * Create a new Delivery Challan
     */
    DeliveryChallanResponse createDeliveryChallan(DeliveryChallanRequest request);
    
    /**
     * Get Delivery Challan by Challan Number
     */
    DeliveryChallanResponse getDeliveryChallanByNumber(String challanNumber);
    
    /**
     * Get all Delivery Challans with pagination
     */
    Page<DeliveryChallanResponse> getAllDeliveryChallans(Pageable pageable);
    
    /**
     * Get Delivery Challan by Package Number
     */
    
    /**
     * Get Delivery Challan by SO Number
     */
    List<DeliveryChallanResponse> getDeliveryChallansBySoNumber(String soNumber);

    // ====== Filter & Search ======
    
    /**
     * Get all Delivery Challans with filters
     */
    Page<DeliveryChallanResponse> getAllDeliveryChallansWithFilters(
            String challanNumber,
            String soNumber,
            String packageNumber,
            String shipmentNumber,
            String customerCode,
            String customerName,
            String status,
            String transporter,
            String vehicleNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime startDispatchDate,
            LocalDateTime endDispatchDate,
            Pageable pageable);
    
    /**
     * Search Delivery Challans by keyword
     */
    Page<DeliveryChallanResponse> searchDeliveryChallans(String search, Pageable pageable);
    
    /**
     * Get Delivery Challans by Status
     */
    List<DeliveryChallanResponse> getDeliveryChallansByStatus(String status);
    


    // ====== Status Management ======
    
    /**
     * Update Delivery Challan Status
     */
    DeliveryChallanResponse updateDeliveryChallanStatus(String challanNumber, String status);
    
    /**
     * Print Delivery Challan (Update status to PRINTED)
     */
    DeliveryChallanResponse printDeliveryChallan(String challanNumber);
    
    /**
     * Mark Delivery Challan as Dispatched
     */
    
    /**
     * Mark Delivery Challan as Delivered
     */
    DeliveryChallanResponse markAsDelivered(String challanNumber);
    
    /**
     * Cancel Delivery Challan
     */
    DeliveryChallanResponse cancelDeliveryChallan(String challanNumber);

    // ====== Document Generation ======
    
   

    // ====== Summary & Statistics ======
    
    /**
     * Get Delivery Challan Summary
     */

    // ====== Delete ======
    
    /**
     * Delete Delivery Challan
     */
    void deleteDeliveryChallan(String challanNumber);
}