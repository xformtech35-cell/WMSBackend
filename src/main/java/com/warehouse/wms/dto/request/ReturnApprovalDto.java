package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReturnApprovalDto {

    @NotNull
    private Long returnId;

    @NotNull
    private Boolean approved;

    private String approvedBy;
    private String remarks;

    /** Optional per-item approval. */
    @Valid
    private List<ReturnItemDecisionDto> items;
}