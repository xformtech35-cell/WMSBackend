package com.warehouse.wms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnLineResponseDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer requestedQuantity;
    private Integer approvedQuantity;
    private Integer actualReturnedQuantity;
    private Integer originalQuantity;
    private Integer receivedQuantity;
    
    private String rejectedArea;

    private String batchNumber;
    private String serialNumbers;
    private LocalDate expiryDate;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String reason;
    private String remarks;
    private Long inboundLineId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}