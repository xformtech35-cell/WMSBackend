package com.warehouse.wms.dto;

import com.warehouse.wms.entity.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderFilterDTO {
    
    // Status filters
    private PurchaseOrderStatus status;
    private List<PurchaseOrderStatus> statuses;
    
    // Date filters
    private LocalDate poDateFrom;
    private LocalDate poDateTo;
    private LocalDate expectedArrivalFrom;
    private LocalDate expectedArrivalTo;
    
    // Search fields
    private String poNumber;
    private String supplierName;
    private String itemCode;
    private String itemName;
    private String searchTerm;
    
    // Other filters
    private Long supplierId;
    private Long purchaseRequestId;
    private Double minAmount;
    private Double maxAmount;
    private Boolean hasItems;
}