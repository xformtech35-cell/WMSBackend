package com.warehouse.wms.dto;

import com.warehouse.wms.entity.RequestStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkStatusUpdateRequestDTO {
    
    @NotEmpty(message = "At least one ID is required")
    private List<Long> ids;
    
    @NotNull(message = "Status is required")
    private RequestStatus status;
    
    private String rejectionReason;
    private String remarks;
}