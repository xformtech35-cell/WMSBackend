package com.warehouse.wms.dto.request;

import java.time.LocalDate;

import com.warehouse.wms.entity.VendorReturnOrder.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListFilterDTO {
    // Order filters
    private String vroNumber;
    private String orderId;
    private String supplierName;
    private String supplierCode;
    
    // Pick list filters
    private String pickListNumber;
    private String assignedTo;
    
    private OrderStatus status;


    // Date filters
    private LocalDate assignedFromDate;
    private LocalDate assignedToDate;
    private LocalDate pickedFromDate;
    private LocalDate pickedToDate;
    private LocalDate createdFromDate;
    private LocalDate createdToDate;
    
    // Quantity filters
    private Integer minItems;
    private Integer maxItems;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Double minProgress;
    private Double maxProgress;
    
    // Search
    private String searchTerm;
}