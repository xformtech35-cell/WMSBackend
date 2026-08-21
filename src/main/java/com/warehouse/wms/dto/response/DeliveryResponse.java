package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private String deliveryNumber;
    private String shipmentNumber;
    private String soNumber;
    private String packageNumber;
    private String customerCode;
    private String customerName;
    private String trackingNumber;
    private LocalDateTime deliveryDate;
    private String receivedBy;
    private Integer deliveredQuantity;
    private String deliveryStatus;
    private String signature;
    private String deliveryProofUrl;
    private String remarks;
    private LocalDateTime createdAt;
}