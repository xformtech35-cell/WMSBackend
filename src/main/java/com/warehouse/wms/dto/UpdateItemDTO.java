package com.warehouse.wms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemDTO {
    
    private Long id;
    
    @NotBlank(message = "Item Code is required")
    private String itemCode;
    
    @NotBlank(message = "Item Name is required")
    private String itemName;
    
    private String description;
    
    @NotBlank(message = "UOM is required")
    private String uom;
    
    // GST Fields
    private Double gstRate;
    private String gstHsnCode;
    private String gstSacCode;
    private Boolean isGstApplicable = true;
    private Double cgstRate;
    private Double sgstRate;
    private Double igstRate;
    
    // Additional Fields
    private Double unitPrice;
    private Integer currentStock = 0;
    private Integer minStockLevel = 0;
    private Integer reorderLevel = 0;
    private Boolean isActive = true;
    private String category;
    private String brand;
    private Long supplierId;
    private String notes;
}