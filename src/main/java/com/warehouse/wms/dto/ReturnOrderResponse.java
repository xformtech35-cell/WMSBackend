package com.warehouse.wms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReturnOrderResponse {
    private Long id;
    private Long originalOrderId;
    private String customerRef;
    private String status;
    private List<ReturnLineResponse> lines;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
