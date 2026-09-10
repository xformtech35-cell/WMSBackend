package com.warehouse.wms.dto.request;

import java.time.LocalDate;

import com.warehouse.wms.entity.VendorReceipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReceiptFilterDTO {

    // Free-text search (receiptNumber, vroNumber, dispatchNumber, supplierName,
    // acknowledgmentNumber, itemCode, itemName)
    private String searchTerm;

    // Receipt identity
    private String receiptNumber;
    private String acknowledgmentNumber;

    // Order / Dispatch linkage
    private Long returnOrderId;
    private Long dispatchId;
    private String vroNumber;
    private String dispatchNumber;
    private String supplierName;
    private String supplierCode;

    // Status
    private VendorReceipt.ReceiptStatus status;

    // Receiver
    private String receivedBy;

    // Date filters (receiptDate)
    private LocalDate receiptFromDate;
    private LocalDate receiptToDate;

    // Date filters (acknowledgmentDate)
    private LocalDate ackFromDate;
    private LocalDate ackToDate;

    // Item-level
    private String itemCode;
    private String itemName;

    // Quantity filters
    private Integer minReceivedQuantity;
    private Integer maxReceivedQuantity;
    private Integer minAcceptedQuantity;
    private Integer maxAcceptedQuantity;
    private Integer minRejectedQuantity;
    private Integer maxRejectedQuantity;
    private Integer minShortQuantity;
    private Integer maxShortQuantity;
    private Integer minDamagedQuantity;
    private Integer maxDamagedQuantity;

    // Boolean flags
    private Boolean hasAcknowledgment;   // true → acknowledgmentNumber is not null
    private Boolean hasDocument;         // true → receiptDocumentPath is not null
}