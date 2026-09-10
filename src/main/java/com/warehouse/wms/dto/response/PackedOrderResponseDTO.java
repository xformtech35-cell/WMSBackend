package com.warehouse.wms.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.warehouse.wms.entity.VendorReturnOrder;
import com.warehouse.wms.entity.VendorReturnRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackedOrderResponseDTO {

    private Long id;
    private String vroNumber;
    private String supplierName;
    private String supplierCode;

    private VendorReturnOrder.OrderStatus status;
    private String statusDisplayName;
    private VendorReturnRequest.Priority priority;

    private Boolean pickListGenerated;
    private LocalDateTime pickListGeneratedAt;

    private String packedBy;                // or Long
    private LocalDateTime packedAt;

    private Integer totalItems;
    private Integer totalQuantity;
    private Integer packedQuantity;
    private Integer remainingQuantity;
    private Double packingProgress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PackedOrderItemDTO> items;
}