package com.warehouse.wms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentConfirmationRequest {

    @NotBlank(message = "Dispatch Number is required")
    private String dispatchNumber;

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Transporter is required")
    private String transporter;

    @NotBlank(message = "Tracking Number is required")
    private String trackingNumber;

    private String shippingMethod;
    private String vehicleNumber;

    private LocalDateTime dispatchDate;
    private LocalDateTime expectedDeliveryDate;

    private String confirmedBy;

    private String createdBy;

    private String remarks;
}