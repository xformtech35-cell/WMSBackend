package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnQcDto {

    @NotNull
    private Long returnId;

    @NotBlank
    private String checkedBy;

    @Valid
    private List<QcItemDto> items;
}