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
public class DispatchRequest {

    @NotBlank(message = "SO Number is required")
    private String soNumber;

    @NotBlank(message = "Package Number is required")
    private String packageNumber;

    private String customerCode;
    private String customerName;

    @NotBlank(message = "Transporter is required")
    private String transporter;

    private String vehicleNumber;
    private String driverName;
    private String driverMobile;

    private String invoiceNumber;
    private String deliveryChallan;

    private LocalDateTime dispatchDate;

    private String dispatchedBy;

    private String createdBy;

    private String remarks;
}