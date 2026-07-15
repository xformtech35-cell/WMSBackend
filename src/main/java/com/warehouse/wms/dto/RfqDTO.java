package com.warehouse.wms.dto;

import com.warehouse.wms.entity.RfqStatus;
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
public class RfqDTO {
    
    private Long id;
    private String rfqNumber;
    private LocalDate rfqDate;
    private LocalDate closingDate;
    private RfqStatus status;
    private String referenceNumber;
    private String remarks;
    private String termsAndConditions;
    private String deliveryTerms;
    private String paymentTerms;
    
    private Long purchaseRequestId;
    private String prNumber;
    
    private List<RfqItemDTO> items;
    private List<VendorQuotationDTO> vendorQuotations;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}