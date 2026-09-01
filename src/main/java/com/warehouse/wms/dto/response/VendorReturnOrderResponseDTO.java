package com.warehouse.wms.dto.response;

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrderResponseDTO {
    private Long id;
    private String vroNumber;
    private LocalDate orderDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    
    // References
    private Long returnRequestId;
    private String returnRequestNumber;
    private Long supplierId;
    private String supplierName;
    private String supplierCode;
    
    // Return details
    private VendorReturnRequest.ReturnType returnType;
    private String returnReason;
    private VendorReturnRequest.Priority priority;
    private VendorReturnOrder.OrderStatus status;
    private String statusDisplayName;
    
    // Shipping
    private String shippingAddress;
    private String shippingMethod;
    private String trackingNumber;
    private String trackingName;
    
    // Warehouse execution flags
    private Boolean pickListGenerated;
    private LocalDateTime pickListGeneratedAt;
    private Long pickedBy;
    private String pickedByName;
    private LocalDateTime pickedAt;
    private Long qcVerifiedBy;
    private String qcVerifiedByName;
    private LocalDateTime qcVerifiedAt;
    private Long packedBy;
    private String packedByName;
    private LocalDateTime packedAt;
    private Long dispatchedBy;
    private String dispatchedByName;
    private LocalDateTime dispatchedAt;
    private String dispatchNumber;
    
    // Totals
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    
    // Audit
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // Lines
    private List<VendorReturnOrderLineResponseDTO> lines;
    
    // Progress tracking
    private Integer pickingProgress; // Percentage
    private Integer qcProgress;
    private Integer packingProgress;
    private Integer dispatchProgress;
}