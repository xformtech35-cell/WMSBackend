package com.warehouse.wms.dto;

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
}