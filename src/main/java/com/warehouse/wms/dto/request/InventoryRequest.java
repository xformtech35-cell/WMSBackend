package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    
    private Long id;
    
    private Long purchaseRequestItemId;
    
    @NotNull(message = "SKU ID is required")
    private Long skuId;
    
    private Long binId;
    
    private Long goodsReceiptLineId;
    
    private String batchNo;
    
    private String serialNo;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
    
    private String state;
    
    private LocalDateTime manufactureDate;
    
    private LocalDateTime expiryDate;
}