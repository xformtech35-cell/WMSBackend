package com.warehouse.wms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    
    @NotNull(message = "Requested date is required")
    private LocalDate requestedDate;
    
    @NotNull(message = "Required date is required")
    private LocalDate requiredDate;
    
    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "LOW|NORMAL|MEDIUM|HIGH|URGENT", message = "Invalid priority")
    private String priority;
    
    private String notes;
    
    private Long supplierId;
    
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<@Valid PurchaseRequestItemDTO> items;
}