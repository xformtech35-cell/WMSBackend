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
public class ReceiptSummary {
    // Acceptance rates
    private BigDecimal acceptanceRate;
    private BigDecimal rejectionRate;
    private BigDecimal damageRate;
    private BigDecimal shortageRate;
    
    // Line counts
    private Integer totalLines;
    private Integer fullyAcceptedLines;
    private Integer partiallyAcceptedLines;
    private Integer fullyRejectedLines;
    
    // Status counts
    private Integer pendingItems;
    private Integer acceptedItems;
    private Integer rejectedItems;
    private Integer damagedItems;
    private Integer shortItems;
}