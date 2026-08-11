package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockResponse {
    private Long id;
    private String inventoryNumber;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer quantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer inTransitQuantity;
    private String warehouseId;
    private String warehouseName;
    private String zone;
    private String aisle;
    private String rack;
    private String shelf;
    private String level;

    private String binId;
    private String binBarcode;
    private String batchNumber;
    private String serialNumbers;
    private LocalDateTime mfgDate;
    private LocalDateTime expiryDate;
    private LocalDateTime receivedDate;
    private LocalDateTime lastUpdatedDate;
    private String grnNumber;
    private String putawayTaskNumber;
    private String confirmationNumber;
    private String qrCodeValue;
    private String status;
    private Boolean isAvailable;
    private Boolean isAllocated;
    private Boolean isFrozen;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}