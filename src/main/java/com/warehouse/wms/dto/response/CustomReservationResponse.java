package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomReservationResponse {

    private String soNumber;
    private String warehouseId;
    private String status;
    private LocalDateTime reservationDate;
    private String createdBy;
    
    private Integer totalItems;
    private Integer totalRequiredQuantity;
    private Integer totalReservedQuantity;
    private Integer totalAvailableQuantity;
    
    private List<ReservationItemResponse> items;
    private List<StockReservationResponse> reservations;
    private List<ReservationSummaryResponse> summary;
}





