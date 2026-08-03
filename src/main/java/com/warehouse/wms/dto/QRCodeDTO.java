package com.warehouse.wms.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeDTO {
    
    // Basic Info
    private String qrId;
    private String qrCode;
    private String qrImage;
    private String qrData;
    private String qrType;
    private String labelLevel;
    
    // Reference
    private String grnNumber;
    private Long inboundId;
    private Long inboundLineId;
    private String itemCode;
    private String itemName;
    private String batchNumber;
    private List<String> serialNumbers;  // Change from String to List<String>
    
    // Quantities
    private Integer quantity;
    private String uom;
    
    // Dates
    private LocalDateTime mfgDate;
    private LocalDateTime expiryDate;
    
    // Location
    private String warehouseId;
    private String binId;
    private String palletNumber;
    
    // Status
    private String status;
    private String printedBy;
    private LocalDateTime printedAt;
    private Integer printCopies;
    
    // Metadata
    private String generatedBy;
    private String templateName;
    private String labelFormat;
    private String remarks;
    private LocalDateTime createdAt;
    
    // Nested DTOs
    private List<QRCodeItemDTO> items;
}
