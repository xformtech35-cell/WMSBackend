package com.warehouse.wms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReturnItemResponseDto {

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String sku;
    private String uom;
    private String batchNumber;

    private Integer deliveredQty;
    private Integer returnQty;
    private Integer receivedQty;
    private Integer acceptedQty;
    private Integer rejectedQty;

    private String itemReturnStatus;
    private String returnReason;
    private String qcStatus;
    private String qcRemarks;
    private String qcCheckedBy;
    private LocalDateTime qcCheckedAt;

    private Boolean restocked;
    private Double unitPrice;
    private Double refundAmount;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}