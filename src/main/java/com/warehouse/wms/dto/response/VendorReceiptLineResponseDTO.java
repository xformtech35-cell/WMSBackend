package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.VendorReceiptLine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReceiptLineResponseDTO {
    private Long id;
    private Long vroLineId;
    private String itemCode;
    private String itemName;
    
    // Quantities
    private Integer dispatchedQuantity;
    private Integer receivedQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer shortQuantity;
    private Integer damagedQuantity;
    
    // Quality details
    private String rejectionReason;
    private String damagedRemarks;
    
    // Status
    private VendorReceiptLine.LineReceiptStatus status;
    private String statusDisplayName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}