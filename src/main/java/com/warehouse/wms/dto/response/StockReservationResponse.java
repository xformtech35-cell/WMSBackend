// StockReservationResponse.java
package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationResponse {
    private String reservationNumber;
    private String soNumber;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer requiredQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private String warehouseId;
    private String zoneId;
    private String aisleId;
    private String rackId;
    private String levelId;
    private String binId;
    private String batchNumber;
    private String status;
    private LocalDateTime reservationDate;
    private String remarks;
    private LocalDateTime createdAt;
}

// ShippingLabelResponse.java
