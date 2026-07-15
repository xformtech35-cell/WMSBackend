package com.warehouse.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateRfqDTO {
    
    @NotNull(message = "RFQ Date is required")
    private LocalDate rfqDate;
    
    private LocalDate closingDate;
    
    private Long purchaseRequestId;
    
    private String referenceNumber;
    private String remarks;
    private String termsAndConditions;
    private String deliveryTerms;
    private String paymentTerms;
    
    private List<Long> supplierIds; // Selected suppliers
    
    @Valid
    private List<@Valid RfqItemDTO> items;
}