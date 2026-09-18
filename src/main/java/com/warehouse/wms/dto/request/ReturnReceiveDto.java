package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnReceiveDto {

    @NotNull
    private Long returnId;

    @NotBlank
    private String receivedBy;

    private String remarks;

    @Valid
    private List<ReceiveItemDto> items;
}