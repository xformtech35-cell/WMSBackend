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
public class CreatePurchaseOrderLineDTO {
    
    private Long itemId;
    
    @NotBlank(message = "Item Code is required")
    private String itemCode;
    
    @NotBlank(message = "Item Name is required")
    private String itemName;
    
    private String description;
    private String hsnCode;
    
    @NotBlank(message = "UOM is required")
    private String uom;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    private Double gstRate = 0.0;
    private Double sgstRate = 0.0;
    private Double cgstRate = 0.0;
    private Double igstRate = 0.0;
    
    @NotNull(message = "Unit Price is required")
    @Min(value = 0, message = "Unit Price must be >= 0")
    private Double unitPrice = 0.0;
    
    private Double discountPercentage = 0.0;
}