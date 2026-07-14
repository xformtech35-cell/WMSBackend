package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String description;
    private String uom;
    private Double gstRate;
    private String gstHsnCode;
    private String gstSacCode;
    private Boolean isGstApplicable;
    private Double cgstRate;
    private Double sgstRate;
    private Double igstRate;
    private Double unitPrice;
    private Integer currentStock;
    private Integer minStockLevel;
    private Integer reorderLevel;
    private Boolean isActive;
    private String category;
    private String brand;
    private Long supplierId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}