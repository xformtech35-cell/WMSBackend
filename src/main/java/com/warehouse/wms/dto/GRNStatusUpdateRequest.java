package com.warehouse.wms.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GRNStatusUpdateRequest {
    
    @NotBlank(message = "GRN status is required")
    @Pattern(regexp = "^(PENDING|APPROVED|REJECTED|PARTIALLY_APPROVED|CANCELLED)$", 
             message = "Invalid GRN status")
    private String grnStatus;
    
    private String remarks;  // Optional: for rejection reason
}