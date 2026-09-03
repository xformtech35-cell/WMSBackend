package com.warehouse.wms.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnLineRequestDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer returnQuantity;
    private Double unitPrice;
    private Double totalAmount;
    private Integer originalQuantity;
    private Integer receivedQuantity;
    private String rejectedArea;

    private String reason;
    private String batchNumber;
    private LocalDate expiryDate;
    private String remarks;
    private Long inboundLineId;
}
