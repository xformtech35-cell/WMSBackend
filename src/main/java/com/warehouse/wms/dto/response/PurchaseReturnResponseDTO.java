package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.PurchaseReturn;
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
public class PurchaseReturnResponseDTO {
    private Long id;
    private String returnNumber;
    private LocalDate returnDate;
    private String poNumber;
    private String grnNumber;
    private String invoiceNumber;
    private String supplierName;
    private String supplierCode;
    private Long supplierId;
    private String reason;
    private PurchaseReturn.ReturnType returnType;
    private PurchaseReturn.ReturnStatus status;
    private Double totalAmount;
    private Integer totalQuantity;
    private String remarks;
    private String approvedBy;
    private LocalDateTime approvedDate;
    private String rejectionReason;
    private String trackingNumber;
    private List<PurchaseReturnLineResponseDTO> lines;
    private LocalDateTime createdAt;
    private String createdBy;
}

