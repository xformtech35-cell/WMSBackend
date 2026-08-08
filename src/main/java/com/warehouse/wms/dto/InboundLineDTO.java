package com.warehouse.wms.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundLineDTO {
    private Long id;
    private String itemCode;
    private String itemName;
    private String uom;
    private Integer orderedQuantity;
    private Integer receivedQuantity;
    private Integer pendingQuantity;
    private Integer totalQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer defectiveQuantity;
    private String qualityStatus;
    private String reason;
    private String remarks;
    private List<InspectionImageDTO> images;
    private String batchNumber;
    
    @Builder.Default  // ✅ Default value false
    private Boolean barcodeGenerate = false;
    
    @Builder.Default  // ✅ Default value false
    private Boolean taskAssinged = false;
    
    private String fullpath;
    
    
    
    private String warehouseId;
    private String zone;
    private String aisle;
    private String rack;
    private String level;
    private String binId;
    

}