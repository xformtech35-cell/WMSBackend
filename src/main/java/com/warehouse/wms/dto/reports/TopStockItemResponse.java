package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopStockItemResponse {
    private String itemCode;
    private String itemName;
    private Integer quantity;
    private Double value;
    private String location;
}