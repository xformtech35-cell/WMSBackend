package com.warehouse.wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class DeliveryChallanRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "package Number is required")
    private String packageNumber;

    private String shipmentNumber;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private String customerGst;
    private String customerPhone;
    private String invoiceNumber;
    private LocalDateTime orderDate;
    private LocalDateTime dispatchDate;
    private LocalDateTime expectedDeliveryDate;
    private String transporter;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String createdBy;
    private String remarks;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<DeliveryChallanItemRequest> items;
}

