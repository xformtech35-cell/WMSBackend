package com.warehouse.wms.dto;

import com.warehouse.wms.entity.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    
    private Long id;
    private String poNumber;
    private LocalDate poDate;
    private LocalDate expectedArrivalDate;
    private PurchaseOrderStatus status;
    private Double subtotal;
    private Double totalGst;
    private Double grandTotal;
    private Double discountAmount;
    private Double shippingCharges;
    private String remarks;
    private String termsAndConditions;
    
    // Supplier details
    private Long supplierId;
    private String supplierName;
    private String supplierEmail;
    private String supplierPhone;
    private String shippingAddress;
    private String billingAddress;
    
    private Long purchaseRequestId;
    private Long createdBy;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime deliveredAt;
    private String rejectionReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<PurchaseOrderLineDTO> lines;
}