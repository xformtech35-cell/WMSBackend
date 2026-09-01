package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.VendorReceipt;
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
public class VendorReceiptResponseDTO {
    private Long id;
    private String receiptNumber;
    private LocalDate receiptDate;
    
    // References
    private Long returnOrderId;
    private String returnOrderNumber;
    private Long dispatchId;
    private String dispatchNumber;
    private Long supplierId;
    private String supplierName;
    
    // Receipt details
    private String receivedBy;
    
    // Quantities
    private Integer totalReceivedQuantity;
    private Integer totalAcceptedQuantity;
    private Integer totalRejectedQuantity;
    private Integer totalShortQuantity;
    private Integer totalDamagedQuantity;
    
    // Status
    private VendorReceipt.ReceiptStatus status;
    private String statusDisplayName;
    
    // Documents
    private String receiptDocumentPath;
    private String acknowledgmentNumber;
    private LocalDate acknowledgmentDate;
    
    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Lines
    private List<VendorReceiptLineResponseDTO> lines;
    
    // Summary
    private ReceiptSummary summary;
}