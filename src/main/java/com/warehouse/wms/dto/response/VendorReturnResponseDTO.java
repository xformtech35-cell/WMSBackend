package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.VendorReturnRequest;
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
public class VendorReturnResponseDTO {
    private Long id;
    private String returnRequestNumber;
    private LocalDate requestDate;
    private String poNumber;
    private String grnNumber;
    private String invoiceNumber;
    private String supplierName;
    private String supplierCode;
    private Long supplierId;
    private VendorReturnRequest.ReturnType returnType;
    private String returnReason;
    private VendorReturnRequest.Priority priority;
    private VendorReturnRequest.RequestStatus status;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedDate;
    private Long rejectedBy;
    private String rejectedByName;
    private LocalDateTime rejectedDate;
    private String rejectionReason;
    private String remarks;
    private Long createdBy;
    
    private Long updatedBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VendorReturnLineResponseDTO> lines;
}