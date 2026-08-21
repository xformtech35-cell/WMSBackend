package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private String priority;

    private String assignedTo;

    private String createdBy;

    @NotEmpty(message = "At least one item is required")
    private List<PickListItemRequest> items;
}