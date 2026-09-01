package com.warehouse.wms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDTO {
    private String yearMonth; // Format: YYYY-MM
    private Long requestCount;
    private Long orderCount;
    private Long completedCount;
    private BigDecimal totalAmount;
    private Long totalItems;
    private Double completionRate;
}