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



    private String shipmentNumber;
    private String transporter;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private String createdBy;
    private String remarks;
    private int totalPackages;
    private int totalQuantity;
    
    

    @NotEmpty(message = "At least one package is required")
    @Valid
    private List<PackageRequests> packages;
}

