package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickListResponseDTO {
    private Long id;
    private String pickListNumber;
    private String vroNumber;
    private Long orderId;
    private String supplierName;
    private String supplierCode;
    private String assignedTo;
    private String status;
    private Integer totalItems;
    private Integer totalQuantity;
    private Integer pickedQuantity;
    private Integer remainingQuantity;
    private Double pickingProgress;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedAt;
    private LocalDateTime completedAt;
    private String priority;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private String updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    private List<PickListItemDTO> items;
}


