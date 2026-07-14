package com.warehouse.wms.dto;

import com.warehouse.wms.entity.Priority;
import com.warehouse.wms.entity.RequestStatus;
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
public class PurchaseRequestFilterDTO {
    
    // Status filters
    private RequestStatus status;
    private List<RequestStatus> statuses; // Multiple statuses
    
    // Priority filters
	private Priority priority;
    private List<Priority> priorities; // Multiple priorities
    
    // Date filters
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate prDateFrom;
    private LocalDate prDateTo;
    private LocalDate requiredDateFrom;
    private LocalDate requiredDateTo;
    
    // String filters (contains search)
    private String prNumber;
    private String requestedBy;
    private String department;
    private String warehouse;
    private String remarks;
    
    // Item filters
    private String itemCode;
    private String itemName;
    

    
    // Other filters
    private Boolean isActive;
    private Boolean hasSupplier;
    private Boolean hasItems;
    
    // Date range for creation
    private LocalDate createdFrom;
    private LocalDate createdTo;
    
    // Search term (searches across multiple fields)
    private String searchTerm;
}