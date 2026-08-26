package com.warehouse.wms.dto.response;

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
public class DeliveryChallanResponse {
    private Long id;
    private String challanNumber;
    private String soNumber;
    private String shipmentNumber;
    private String transporter;
    private String vehicleNumber;
    private String driverName;
    private String driverPhone;
    private Integer totalPackages;
    private Integer totalQuantity;
    private Double totalWeight;
    private Double totalVolume;
    private String status;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PackageResponses> packages;
}

