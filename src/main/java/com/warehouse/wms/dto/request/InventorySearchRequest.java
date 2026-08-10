package com.warehouse.wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventorySearchRequest {
    private String itemCode;
    private String itemName;
    private String binId;
    private String warehouseId;
    private String status;
    private String state;
    private String serialNo;
    private String batchNo;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Integer minQuantity;
    private Integer maxQuantity;
}