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
public class InventoryStockRequest {
    
    private Long id;
    
    private String inventoryNumber;
    
    @NotBlank(message = "Item code is required")
    private String itemCode;
    
    @NotBlank(message = "Item name is required")
    private String itemName;
    
    private String uom;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
    
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer inTransitQuantity;
    
    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;
    
    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String binId;
    private String binBarcode;
    private String batchNumber;
    private String serialNumbers;
    private LocalDateTime mfgDate;
    private LocalDateTime expiryDate;
    private String status;
    private Boolean isAvailable;
    private Boolean isAllocated;
    private Boolean isFrozen;
    private String remarks;
}