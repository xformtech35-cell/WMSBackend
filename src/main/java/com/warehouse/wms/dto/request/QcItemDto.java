package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QcItemDto {

    @NotNull
    private Long itemId;

    @NotNull
    private Boolean passed;

    @Min(0)
    private Integer acceptedQty;

    @Min(0)
    private Integer rejectedQty;

    private Boolean restock;

    private String qcRemarks;
    private String remarks;
}