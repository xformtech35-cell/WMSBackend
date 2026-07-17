package com.warehouse.wms.dto;

import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;
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
public class InboundDTO {
    private Long id;
    private String inboundNumber;
    private LocalDate inboundDate;
    private LocalDate expectedArrivalDate;
    
    // PO Details
    private String poNumber;
    private String invoiceNumber;
    private String deliveryChallan;
    private String supplierName;
    private String trackingNumber;
    private String trackingName;
    
    // Gate Entry
    private String gateEntryNumber;
    private String driverName;
    private String driverContact;
    private String driverId;
    private String trackNumber;
    private String gateNumber;
    private Long approvedBy;
    private LocalDateTime gateEntryDateTime;
    
    // Unloading
    private Integer boxesUnloadedQuantity;
    private String unloadedBy;
    private LocalDateTime unloadingStartTime;
    private LocalDateTime unloadingEndTime;
    
    // Goods Receiving
    private Long receivedBy;
    private LocalDateTime receivedDate;
    
    // Quality Inspection
    private Long inspectedBy;
    private LocalDateTime inspectionDate;
    private String qualityStatus;
    private String qualityRemarks;
    
    // GRN
    private String grnNumber;
    private LocalDateTime grnDate;
    private String grnStatus;
    
    // Status
    private InboundStatus status;
    private InboundStage stage;
    private String remarks;
    private Long createdBy;
    
    private List<InboundLineDTO> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}