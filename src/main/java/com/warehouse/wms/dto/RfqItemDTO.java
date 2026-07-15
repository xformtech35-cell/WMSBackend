package com.warehouse.wms.dto;

import jakarta.validation.constraints.Min;
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
public class RfqItemDTO {
    
    private Long id;
    
    @NotBlank(message = "Item Code is required")
    private String itemCode;
    
    @NotBlank(message = "Item Name is required")
    private String itemName;
    
    private String description;
    
    @NotBlank(message = "UOM is required")
    private String uom;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be >= 0")
    private Double quantity;
    
    private String hsnCode;
    private Double gstRate;
    private Double cgstRate;
    private Double sgstRate;
    private Double igstRate;
    private Double estimatedUnitPrice;
    private Double estimatedTotal;
    private String specifications;
    private Long purchaseRequestItemId;
}