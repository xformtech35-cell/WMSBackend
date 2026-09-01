package com.warehouse.wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QCDTO {
    private Long lineId;
    private Integer qcQuantity;
    private Boolean passed;
    private Long verifiedBy;
    private String remarks;
}

// PackingDTO.java
