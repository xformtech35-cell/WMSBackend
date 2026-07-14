package com.warehouse.wms.dto;

import com.warehouse.wms.entity.Priority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreatePurchaseRequestDTO {
    
    @NotNull(message = "PR Date is required")
    private LocalDate prDate;
    
    @NotBlank(message = "Requested By is required")
    private String requestedBy;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Warehouse is required")
    private String warehouse;
    
    @NotNull(message = "Priority is required")
    private Priority priority;
    
    @NotNull(message = "Required Date is required")
    private LocalDate requiredDate;
    
    private String remarks;
    
    private Long supplierId;
    
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<@Valid PurchaseRequestItemDTO> items;
}