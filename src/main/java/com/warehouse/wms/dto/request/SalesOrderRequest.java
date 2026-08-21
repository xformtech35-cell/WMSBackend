package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
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
public class SalesOrderRequest {

   

//    @NotNull(message = "SO Date is required")
    private LocalDateTime soDate;

    @NotBlank(message = "Customer code is required")
    private String customerCode;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    private LocalDateTime deliveryDate;

    private String priority; // HIGH, MEDIUM, LOW

    private String deliveryAddress;

    private String shippingMethod;

    private String remarks;

    private String createdBy;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesOrderItemRequest> items;
}