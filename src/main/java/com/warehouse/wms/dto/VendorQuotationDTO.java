package com.warehouse.wms.dto;

import com.warehouse.wms.entity.QuotationStatus;
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
public class VendorQuotationDTO {
    
    private Long id;
    private String quotationNumber;
    private LocalDate quotationDate;
    private LocalDate deliveryDate;
    private LocalDate validTill;
    private Double subTotal;
    private Double gstTotal;
    private Double grandTotal;
    private Double discountAmount;
    private Double shippingCharges;
    private String remarks;
    private QuotationStatus status;
    private Integer rank;
    
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    
    
    
    
    private String supplierEmail;

    private String supplierMobile;

    private String supplierAddress;
    
    private String gstNumber;
    
    private List<VendorQuotationItemDTO> items;
}