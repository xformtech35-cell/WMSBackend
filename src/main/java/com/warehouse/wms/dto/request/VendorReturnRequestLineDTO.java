package com.warehouse.wms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnRequestLineDTO {
    
    private Long id;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    private String uom;
    
    
    private String rejectedArea;

    
    @NotNull(message = "Requested quantity is required")
    private Integer requestedQuantity;
    
    private Integer approvedQuantity;
    private Integer actualReturnedQuantity;
    private Integer originalQuantity;
    private Integer receivedQuantity;
    
    private String batchNumber;
    private String serialNumbers;
    private LocalDate expiryDate;
    
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    private String reason;
    private String remarks;
    private Long inboundLineId;
}

// VendorReturnOrderDTO.java
