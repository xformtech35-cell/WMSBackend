package com.warehouse.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInboundDTO {
    private Long purchaseOrderId;
    private LocalDate inboundDate;
    private LocalDate expectedArrivalDate;
    private String poNumber;
    private String invoiceNumber;
    private String deliveryChallan;
    private String supplierName;
    private String trackingNumber;
    private String trackingName;
    private String remarks;
}