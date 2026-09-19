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
public class PickTaskItemResponse {

    private Long id;                     // PickTaskItem.id
    private Long pickTaskId;             // Parent PickTask.id

    // ===== Item =====
    private String itemCode;
    private String itemName;
    private String uom;

    // ===== Quantities =====
    private Integer requiredQuantity;
    private Integer quantityToPick;
    private Integer pickedQuantity;
    private Integer shortQuantity;

    // ===== Location / Barcode =====
    private String locationBarcode;
    private String itemBarcode;
    private String binId;
    private String batchNumber;
    private String sourceLocation;

    // ===== Relationships =====
//    private Long inventoryId;            // InventoryStock.id (safe)
//    private Long salesOrderLineId;

    // ===== Status =====
    private String status;               // PENDING, PICKING, PICKED, SHORT, CANCELLED
    private String priority;             // LOW, MEDIUM, HIGH, URGENT
    private Boolean isScanned;
    private LocalDateTime scanTime;

    // ===== Meta =====
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}