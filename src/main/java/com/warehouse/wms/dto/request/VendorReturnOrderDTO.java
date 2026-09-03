package com.warehouse.wms.dto.request;

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrderDTO {
    
    private Long id;
    private String vroNumber;
    
    @NotNull(message = "Order date is required")
    private LocalDate orderDate;
    
    private LocalDate expectedReturnDate;
    
    private Long returnRequestId;
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    
    private Long purchseReturnId;

    
    private VendorReturnRequest.ReturnType returnType;
    private String returnReason;
    private VendorReturnRequest.Priority priority;
    
    private String shippingAddress;
    private String shippingMethod;
    private String trackingNumber;
    private String trackingName;
    
    @Valid
    private List<VendorReturnOrderLineDTO> lines;
}

// VendorReturnOrderLineDTO.java
