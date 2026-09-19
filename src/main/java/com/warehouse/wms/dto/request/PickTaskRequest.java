package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTaskRequest {

    @NotBlank(message = "Pick List Number is required")
    private String pickListNumber;

    private String soNumber;

    private String warehouseId;

    private String priority;

    private String assignedTo;

    private String status;

    private String remarks;

    private String createdBy;

    private String updatedBy;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PickTaskItemRequest> items;
}