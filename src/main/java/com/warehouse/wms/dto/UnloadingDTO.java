package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnloadingDTO {
    private Integer boxesUnloadedQuantity;
    private String unloadedBy;
    private String remarks;
}