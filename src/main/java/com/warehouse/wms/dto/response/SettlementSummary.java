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
public class SettlementSummary {
    // Amounts
    private BigDecimal totalSettlementAmount;
    private BigDecimal totalCreditNoteAmount;
    private BigDecimal totalRefundAmount;
    
    // Counts
    private Integer totalSettlements;
    private Integer pendingSettlements;
    private Integer completedSettlements;
    private Integer cancelledSettlements;
    
    // Types
    private Integer creditNoteCount;
    private Integer replacementCount;
    private Integer refundCount;
}