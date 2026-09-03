package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.VendorReturnOrderLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrderLineResponseDTO {
    private Long id;
    private Long returnRequestLineId;
    
    // Item details
    private String itemCode;
    private String itemName;
    private String uom;
    
    // Quantities
    private Integer orderQuantity;
    private Integer pickedQuantity;
    private Integer qcQuantity;
    private Integer packedQuantity;
    private Integer dispatchedQuantity;
    private Integer receivedQuantity;
    
    // Batch/Serial
    private String batchNumber;
    private String serialNumbers;
    private LocalDate expiryDate;
    
    // Pricing
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    
    // Warehouse execution
    private String pickLocation;
    private Integer pickSequence;
    private String packBarcode;
    
    private String rejectedArea;

    
    // QC
    private VendorReturnOrderLine.QCStatus qcStatus;
    private String qcRemarks;
    
    // Status
    private VendorReturnOrderLine.LineStatus status;
    
    // Progress
    private Integer pickingProgress; // Percentage
    private Integer qcProgress;
    private Integer packingProgress;
    private Integer dispatchProgress;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}