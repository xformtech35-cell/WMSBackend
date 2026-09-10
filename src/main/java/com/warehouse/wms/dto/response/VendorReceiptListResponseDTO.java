package com.warehouse.wms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.warehouse.wms.entity.VendorReceipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReceiptListResponseDTO {

    private Long id;
    private String receiptNumber;
    private LocalDate receiptDate;

    // Linkage
    private Long returnOrderId;
    private String returnOrderNumber;   // vroNumber
    private Long dispatchId;
    private String dispatchNumber;
    private Long supplierId;
    private String supplierName;

    // Receiver
    private String receivedBy;

    // Totals
    private Integer totalReceivedQuantity;
    private Integer totalAcceptedQuantity;
    private Integer totalRejectedQuantity;
    private Integer totalShortQuantity;
    private Integer totalDamagedQuantity;

    // Status
    private VendorReceipt.ReceiptStatus status;
    private String statusDisplayName;

    // Acknowledgment / document
    private String acknowledgmentNumber;
    private LocalDate acknowledgmentDate;
    private String receiptDocumentPath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Optional — only for detail views
    private List<VendorReceiptLineResponseDTO> lines;
}