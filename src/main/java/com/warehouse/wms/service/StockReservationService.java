package com.warehouse.wms.service;

import java.time.LocalDateTime;
import java.util.List;

import com.warehouse.wms.dto.request.CustomReservationRequest;
import com.warehouse.wms.dto.request.EditReservationRequest;
import com.warehouse.wms.dto.response.CustomReservationResponse;
import com.warehouse.wms.dto.response.StockAvailabilityResponse;
import com.warehouse.wms.dto.response.StockReservationResponse;
import com.warehouse.wms.entity.StockReservation;

public interface StockReservationService {

    // Auto reserve (existing)
    StockReservation reserveStock(String soNumber);
    
    // Custom reserve with specific locations
    CustomReservationResponse customReserveStock(CustomReservationRequest request);
    
    // Reserve from specific bin
    StockReservation reserveFromBin(String soNumber, String itemCode, String binId, Integer quantity);
    
    // Reserve from specific inventory stock
    StockReservation reserveFromInventory(String soNumber, Long inventoryStockId, Integer quantity);
    
    // Release specific reservation
    void releaseReservation(String reservationNumber);
    
    // Release all reservations for SO
    void releaseAllReservations(String soNumber);
    
    // Get reservation details
    List<StockReservation> getReservationsBySoNumber(String soNumber);
    
    // Check availability for item
    StockAvailabilityResponse checkAvailability(String itemCode, Integer requestedQuantity);
    
    
    // Edit single reservation
    StockReservationResponse editReservation(EditReservationRequest request);
    
    // Edit reservation quantity only
    StockReservationResponse editReservationQuantity(String reservationNumber, Integer newQuantity);
    
    // Edit reservation location (bin)
    StockReservationResponse editReservationLocation(String reservationNumber, String newBinId);
    
    // Edit reservation batch
    StockReservationResponse editReservationBatch(String reservationNumber, String batchNumber);
    
    // Edit reservation remarks
    StockReservationResponse editReservationRemarks(String reservationNumber, String remarks);
    
    // Bulk edit reservations
    
    // Extend reservation expiry
    StockReservationResponse extendReservationExpiry(String reservationNumber, LocalDateTime newExpiryDate);
}