package com.warehouse.wms.dto.request;

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
public class VendorReceiptLineDTO {
    
    private Long id;
    private Long vroLineId;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    private String rejectedArea;

    
    private Integer dispatchedQuantity;
    
    @NotNull(message = "Received quantity is required")
    private Integer receivedQuantity;
    
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer shortQuantity;
    private Integer damagedQuantity;
    
    private String rejectionReason;
    private String damagedRemarks;
}

// SettlementDTO.java
