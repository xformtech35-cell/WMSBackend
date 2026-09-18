package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiveItemDto {

    @NotNull
    private Long itemId;

    @NotNull
    @Min(0)
    private Integer receivedQty;

    private String remarks;
}