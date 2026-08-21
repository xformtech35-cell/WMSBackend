package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {

    @NotBlank(message = "Shipment Number is required")
    private String shipmentNumber;

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    private String packageNumber;

    @NotBlank(message = "Received by is required")
    private String receivedBy;

    @NotNull(message = "Delivered quantity is required")
    @Positive(message = "Delivered quantity must be greater than 0")
    private Integer deliveredQuantity;

    private String signature;

    private String deliveryProofUrl;

    private String createdBy;

    private String remarks;
}