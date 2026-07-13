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
    private List<PurchaseRequestItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime SubmittedAt;
    private LocalDateTime submittedAt;
    private String rejectionReason;
    private LocalDateTime approvedAt;

}