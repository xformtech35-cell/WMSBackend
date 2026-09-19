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
public class PickConfirmationResponse {

    // ---- Identifiers ----
    private Long id;
    private String confirmationNumber;
    private String pickTaskNumber;
    private String pickListNumber;
    private String soNumber;
    private String warehouseId;

    // ---- Summary ----
    private Integer totalItems;
    private Integer totalPickedQuantity;
    private Integer totalShortQuantity;

    // ---- Audit / Status ----
    private String confirmedBy;
    private LocalDateTime confirmedDate;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---- Child items ----
    private List<PickConfirmationItemResponse> items;
}