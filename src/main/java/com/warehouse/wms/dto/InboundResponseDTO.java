// ====== FILE: src/main/java/com/warehouse/wms/dto/InboundResponseDTO.java ======

package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundResponseDTO {
    private Long id;
    private String inboundNumber;
    private LocalDate inboundDate;
    private LocalDate expectedArrivalDate;
    private String poNumber;
    private String invoiceNumber;
    private String deliveryChallan;
    private String supplierName;
    private String trackingNumber;
    private String trackingName;
    private String gateEntryNumber;
    private String driverName;
    private String driverContact;
    private String driverId;
    private String trackNumber;
    private String gateNumber;
    private Long approvedBy;
    private LocalDateTime gateEntryDateTime;
    private Integer boxesUnloadedQuantity;
    private String unloadedBy;
    private LocalDateTime unloadingStartTime;
    private LocalDateTime unloadingEndTime;
    private Long receivedBy;
    private LocalDateTime receivedDate;
    private Long inspectedBy;
    private LocalDateTime inspectionDate;
    private String qualityStatus;
    private String qualityRemarks;
    private String grnNumber;
    private LocalDateTime grnDate;
    private String grnStatus;
    private LocalDateTime grnApprovedDate;
    private String status;
    private String stage;
    private String remarks;
    private Long createdBy;
    private List<InboundLineDTO> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approvalStatus;
    private LocalDateTime approvalDate;
    private String approvalRemarks;
    private String rejectionReason;
}