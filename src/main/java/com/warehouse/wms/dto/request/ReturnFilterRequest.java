package com.warehouse.wms.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReturnFilterRequest {

    /** Free-text search — matches returnNumber, deliveryNumber, soNumber, shipmentNumber, customerCode, customerName */
    private String search;

    /** Header status: PENDING, APPROVED, REJECTED, IN_TRANSIT, RECEIVED, QC_PASSED, QC_FAILED, COMPLETED, CANCELLED */
    private String returnStatus;

    /** Item-level status: PENDING, APPROVED, REJECTED, RECEIVED, QC_PASSED, QC_FAILED, RESTOCKED, SCRAPPED */
    private String itemReturnStatus;

    private Long deliveryId;
    private String deliveryNumber;
    private String soNumber;
    private String shipmentNumber;
    private String customerCode;
    private String customerName;
    private String itemCode;

    private String requestedBy;
    private String approvedBy;
    private String receivedBy;

    /** Date range on returnRequestedAt */
    private LocalDate fromDate;
    private LocalDate toDate;
}