package com.warehouse.wms.dto;

import com.warehouse.wms.entity.RfqStatus;
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
public class RfqFilterDTO {
    
    // Status filters
    private RfqStatus status;
    private List<RfqStatus> statuses;
    
    // Date filters
    private LocalDate rfqDateFrom;
    private LocalDate rfqDateTo;
    private LocalDate closingDateFrom;
    private LocalDate closingDateTo;
    
    // String filters
    private String rfqNumber;
    private String prNumber;
    
    // Item filters
    private String itemCode;
    private String itemName;
    
    // Supplier filters
    private Long supplierId;
    
    // Other filters
    private Boolean hasQuotations;
    
    // Search term (searches across multiple fields)
    private String searchTerm;
}