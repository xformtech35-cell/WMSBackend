package com.warehouse.wms.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStockResponse {
    private String category;
    private Long items;
    private Integer quantity;
    private Double value;
}
