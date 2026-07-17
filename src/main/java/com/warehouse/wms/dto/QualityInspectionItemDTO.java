package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionItemDTO {
    private Long lineId;
    private String itemCode;
    private String itemName;
    private Integer receivedQuantity;
    private Integer acceptedQuantity;
    private Integer rejectedQuantity;
    private Integer defectiveQuantity;
    private String qualityStatus; // GOOD, PARTIAL, REJECTED
    private String reason;
    private String remarks;
}