package com.warehouse.wms.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReturnResponseDto {

    private Long returnId;
    private String returnNumber;

    private Long deliveryId;
    private String deliveryNumber;
    private String shipmentNumber;
    private String soNumber;
    private String customerCode;
    private String customerName;

    private String returnStatus;
    private String returnReason;
    private String returnRequestedBy;
    private LocalDateTime returnRequestedAt;

    private String approvedBy;
    private LocalDateTime approvedAt;

    private String returnReceivedBy;
    private LocalDateTime returnReceivedAt;

    private LocalDateTime completedAt;
    private Double refundAmount;
    private String remarks;

    private Integer totalReturnQty;
    private Integer totalReceivedQty;
    private Integer totalAcceptedQty;
    private Integer totalRejectedQty;

    private List<ReturnItemResponseDto> items;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}