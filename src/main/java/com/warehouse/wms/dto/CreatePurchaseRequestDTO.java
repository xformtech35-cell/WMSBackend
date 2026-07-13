package com.warehouse.wms.dto;

import java.time.LocalDate;
import java.util.List;

import com.warehouse.wms.entity.Priority;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseRequestDTO {
    
    @NotNull(message = "PR Date is required")
    private LocalDate prDate; // Date of request
    
    @NotBlank(message = "Requested By is required")
    private String requestedBy; // Employee name
    
    @NotBlank(message = "Department is required")
    private String department; // Requesting department
    
    @NotBlank(message = "Warehouse is required")
    private String warehouse; // Destination warehouse
    
    @NotNull(message = "Priority is required")
    private Priority priority; // LOW, MEDIUM, HIGH
    
    @NotNull(message = "Required Date is required")
    private LocalDate requiredDate; // When items are needed
    
    private String remarks; // Additional notes
    
    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<@Valid PurchaseRequestItemDTO> items;
}