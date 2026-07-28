package com.warehouse.wms.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityInspectionApprovalDTO {
    
    @NotNull(message = "Approval status is required")
    private String approvalStatus; // APPROVED, REJECTED
    
    private String approvalRemarks;
    
    private String rejectionReason;
    
    @NotNull(message = "Approved by is required")
    private Long approvedBy;
}