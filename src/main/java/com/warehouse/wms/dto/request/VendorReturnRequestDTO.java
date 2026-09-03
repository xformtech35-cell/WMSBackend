// VendorReturnRequestDTO.java
package com.warehouse.wms.dto.request;

import com.warehouse.wms.entity.VendorReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnRequestDTO {
    
    private Long id;
    
    private String returnRequestNumber;
    
    @NotNull(message = "Request date is required")
    private LocalDate requestDate;
    
    private String poNumber;
    private String grnNumber;
    private String invoiceNumber;
    
    private String supplierName;
    private String supplierCode;
    private Long supplierId;
    
    
    
    private String rockArea;

    
    @NotNull(message = "Return type is required")
    private VendorReturnRequest.ReturnType returnType;
    
    private String returnReason;
    private VendorReturnRequest.Priority priority;
    private VendorReturnRequest.RequestStatus status;
    
    private Long approvedBy;
    private Long rejectedBy;
    private String rejectionReason;
    
    private String remarks;
    private Long createdBy;
    
    @Valid
    private List<VendorReturnRequestLineDTO> lines;
}

// VendorReturnRequestLineDTO.java
