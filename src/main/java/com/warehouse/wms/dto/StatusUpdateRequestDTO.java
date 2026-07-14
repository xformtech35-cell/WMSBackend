package com.warehouse.wms.dto;

import com.warehouse.wms.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequestDTO {
    
    @NotNull(message = "Status is required")
    private RequestStatus status;
    
    private String rejectionReason;  // For REJECTED status
    private String remarks;          // Additional remarks
    private String action;           // Action performed (SUBMIT, APPROVE, REJECT, etc.)
}