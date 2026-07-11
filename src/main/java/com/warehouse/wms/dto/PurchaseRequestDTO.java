package com.warehouse.wms.dto;

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
public class PurchaseRequestDTO {
    private Long id;
    private String prNumber;
    private LocalDate requestedDate;
    private String batchNo;
    private LocalDate requiredDate;
    private String priority;
    private String status;
    private String notes;
    private Double totalAmount;
    private Long supplierId;
    private String supplierName;
    private List<PurchaseRequestItemDTO> items;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime submittedAt;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}