package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnItemDecisionDto {

    @NotNull
    private Long itemId;

    @NotNull
    private Boolean approved;

    private String remarks;
}