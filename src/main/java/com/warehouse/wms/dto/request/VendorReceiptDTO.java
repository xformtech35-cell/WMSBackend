package com.warehouse.wms.dto.request;

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
public class VendorReceiptDTO {
    
    private Long id;
    private String receiptNumber;
    
    @NotNull(message = "Receipt date is required")
    private LocalDate receiptDate;
    
    private Long returnOrderId;
    private Long dispatchId;
    private Long supplierId;
    
    private String receivedBy;
    private String supplierName;
    
    private String acknowledgmentNumber;
    private LocalDate acknowledgmentDate;
    private String receiptDocumentPath;
    
    @Valid
    private List<VendorReceiptLineDTO> lines;
}

// VendorReceiptLineDTO.java
