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
public class PickTaskResponse {

    // ===== Identifiers =====
    private Long id;                      // PickTask.id
    private String pickTaskNumber;
    private String pickListNumber;
    private String soNumber;

    // ===== Header Info =====
    private String warehouseId;
    private String assignedTo;            // picker assigned
    private String priority;              // LOW, MEDIUM, HIGH, URGENT
    private Integer totalItems;
    private Integer totalQuantity;
    private String status;                // PENDING, PICKING, PARTIAL, COMPLETED, CANCELLED

    // ===== Meta =====
    private String remarks;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime completedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== Items (children) =====
    private List<PickTaskItemResponse> items;
}