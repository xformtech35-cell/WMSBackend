package com.warehouse.wms.dto;

import com.warehouse.wms.entity.InboundStage;
import com.warehouse.wms.entity.InboundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundFilterDTO {
    
    // ============================================
    // STATUS & STAGE FILTERS
    // ============================================
    private InboundStatus status;
    private InboundStage stage;
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    
    // ============================================
    // TEXT FILTERS
    // ============================================
    private String poNumber;
    private String supplierName;
    private String qualityStatus; // GOOD, PARTIAL, REJECTED
    private String grnStatus; // PENDING, GENERATED
    private String searchTerm;
    
    // ============================================
    // DATE FILTERS
    // ============================================
    // Inbound Date
    private LocalDate inboundDateFrom;
    private LocalDate inboundDateTo;
    
    // Expected Arrival Date
    private LocalDate expectedArrivalDateFrom;
    private LocalDate expectedArrivalDateTo;
    
    // Gate Entry Date
    private LocalDate gateEntryDateTimeFrom;
    private LocalDate gateEntryDateTimeTo;
    
    // Unloading Start Time
    private LocalDate unloadingStartTimeFrom;
    private LocalDate unloadingStartTimeTo;
    
    // Received Date
    private LocalDate receivedDateFrom;
    private LocalDate receivedDateTo;
    
    // Inspection Date
    private LocalDate inspectionDateFrom;
    private LocalDate inspectionDateTo;
    
    // GRN Date
    private LocalDate grnDateFrom;
    private LocalDate grnDateTo;
    
    // Approval Date
    private LocalDate approvalDateFrom;
    private LocalDate approvalDateTo;
    
    // ============================================
    // QUANTITY FILTERS
    // ============================================
    private Integer minBoxesUnloaded;
    private Integer maxBoxesUnloaded;
    private Integer minBoxesInTruck;
    private Integer maxBoxesInTruck;
}