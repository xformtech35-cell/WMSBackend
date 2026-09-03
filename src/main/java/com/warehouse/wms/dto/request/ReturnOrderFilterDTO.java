package com.warehouse.wms.dto.request;

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnOrderFilterDTO {
    // Order filters
    private String vroNumber;
    private String supplierName;
    private String supplierCode;
    
    // Status and Priority
    private VendorReturnOrder.OrderStatus status;
    private VendorReturnRequest.Priority priority;
    
    // Date filters
    private LocalDate orderFromDate;
    private LocalDate orderToDate;
    private LocalDate expectedFromDate;
    private LocalDate expectedToDate;
    private LocalDate actualFromDate;
    private LocalDate actualToDate;
    
    // Quantity filters
    private Integer minQuantity;
    private Integer maxQuantity;
    private Double minAmount;
    private Double maxAmount;
    
    // Return type
    private VendorReturnRequest.ReturnType returnType;
    
    // Search term (searches across multiple fields)
    private String searchTerm;
    
    // Assigned to (for pick lists)
    private String assignTo;
    
    // Pick list status
    private String pickListStatus;
    
    // Pick list generated flag
    private Boolean pickListGenerated;
}