package com.warehouse.wms.dto.request;

import com.warehouse.wms.entity.PurchaseReturn;
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
public class PurchaseReturnRequestDTO {
    private Long id;
    private String returnNumber;
    private LocalDate returnDate;
    private String poNumber;
    private String grnNumber;
    private String invoiceNumber;
    private String supplierName;
    private String supplierCode;
    private Long supplierId;
    private Long purchaseOrderId;
    private Long inboundId;
    private String reason;
    private PurchaseReturn.ReturnType returnType;
    private PurchaseReturn.ReturnStatus status;
    private String remarks;
    private List<PurchaseReturnLineRequestDTO> lines;
}

