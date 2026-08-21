package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomReservationRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private String priority;

    private LocalDateTime deliveryDate;

    private String createdBy;

    @NotEmpty(message = "At least one item is required")
    private List<ReservationItemRequest> items;
}

