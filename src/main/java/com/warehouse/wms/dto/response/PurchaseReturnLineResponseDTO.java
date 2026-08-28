package com.warehouse.wms.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnLineResponseDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer returnQuantity;
    private Double unitPrice;
    private Double totalAmount;
    private String reason;
    private String batchNumber;
    private LocalDate expiryDate;
    private String remarks;
}