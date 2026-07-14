package com.warehouse.wms.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderDTO {
    
    @NotNull(message = "PO Date is required")
    private LocalDate poDate;
    
    private LocalDate expectedArrivalDate;
    
    private Long supplierId;
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String shippingAddress;
    private String billingAddress;
    
    private Double discountAmount = 0.0;
    private Double shippingCharges = 0.0;
    private String remarks;
    private String termsAndConditions;
    
    private Long purchaseRequestId;
    
    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<@Valid CreatePurchaseOrderLineDTO> lines;
}