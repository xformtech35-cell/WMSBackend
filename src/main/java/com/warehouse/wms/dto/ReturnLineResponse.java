package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReturnLineResponse {
    private Long id;
    private Long skuId;
    private String skuCode;
    private Integer orderedQty;
    private Integer returnedQty;
    private String conditionGrade;
    private String inspectionNotes;
    private String restockedBinBarcode;
    private String batchNumber;
}
