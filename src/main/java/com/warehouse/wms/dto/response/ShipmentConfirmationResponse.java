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
public class ShipmentConfirmationResponse {

    private String shipmentNumber;
    private String dispatchNumber;
    private String soNumber;
    private String packageNumber;
    private String trackingNumber;
    private String transporter;
    private String shippingMethod;
    private String vehicleNumber;
    private LocalDateTime dispatchDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String status;
    private String confirmedBy;
    private String remarks;
    private LocalDateTime createdAt;
}