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
public class SalesOrderResponse {

    private Long id;
    private String soNumber;
    private LocalDateTime soDate;
    private String customerCode;
    private String customerName;
    private String warehouseId;
    private LocalDateTime deliveryDate;
    private String priority;
    private String deliveryAddress;
    private Integer totalQuantity;
    private Double totalWeight;
    private String shippingMethod;
    private String status;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<SalesOrderItemResponse> items;
    
    private List<StockReservationResponse> reservations;

}