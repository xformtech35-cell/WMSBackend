package com.warehouse.wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackingDTO {
    private Long lineId;
    private Integer packedQuantity;
    private Long packedBy;
    private String packBarcode;
    private String packagingType;
    private String remarks;
}