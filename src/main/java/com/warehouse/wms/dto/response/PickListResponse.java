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
public class PickListResponse {

    private String pickListNumber;
    private String soNumber;
    private String warehouseId;
    private String priority;
    private Integer totalItems;
    private Integer totalQuantity;
    private String status;
    private String createdBy;
    private String assignedTo;
    private LocalDateTime completedDate;
    private String remarks;
    private LocalDateTime createdAt;

    private List<PickListItemResponse> items;
}