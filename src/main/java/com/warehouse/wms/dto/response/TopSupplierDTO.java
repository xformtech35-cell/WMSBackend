package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSupplierDTO {
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    private Long returnCount;
    private BigDecimal totalAmount;
    private BigDecimal averageAmount;
    private Double returnRate; // Percentage
}