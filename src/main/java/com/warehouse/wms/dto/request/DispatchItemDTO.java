package com.warehouse.wms.dto.request;

import java.math.BigDecimal;

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
public class DispatchItemDTO {
    
    private Long id;
    private Long vroLineId;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    @NotNull(message = "Dispatched quantity is required")
    private Integer dispatchedQuantity;
    
    private String rejectedArea;

    
    private Integer packedQuantity;
    private String packagingType;
    private Integer packageCount;
    private BigDecimal packageWeight;
}

// VendorReceiptDTO.java
