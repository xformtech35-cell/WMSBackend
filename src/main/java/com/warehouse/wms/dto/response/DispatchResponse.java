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
public class DispatchResponse {

    private String dispatchNumber;
    private String shipmentNumber;
    private String soNumber;
    private String packageNumber;
    private String customerCode;
    private String customerName;
    private String transporter;
    private String vehicleNumber;
    private String driverName;
    private String driverMobile;
    private String invoiceNumber;
    private String deliveryChallan;
    private LocalDateTime dispatchDate;
    private String status;
    private String dispatchedBy;
    private String remarks;
    private LocalDateTime createdAt;
}