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
    private Integer totalItems;
    private Integer totalQuantity;
    private Double totalWeight;
    private Double totalVolume;
    private String status;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeliveryChallanItemResponse> items;
}

