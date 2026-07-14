package com.warehouse.wms.dto;

import com.warehouse.wms.entity.Priority;
import com.warehouse.wms.entity.RequestStatus;
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
    private LocalDate prDate;
    private String requestedBy;
    private String department;
    private String warehouse;
    private Priority priority;
    private LocalDate requiredDate;
    private String remarks;
    private RequestStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Supplier fields
    private Long supplierId;
    private String supplierName;
    
    // Items
    private List<PurchaseRequestItemDTO> items;
}