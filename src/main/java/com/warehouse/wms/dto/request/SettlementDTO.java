package com.warehouse.wms.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.warehouse.wms.entity.ReturnSettlement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementDTO {
    
    private Long id;
    private String settlementNumber;
    
    @NotNull(message = "Settlement type is required")
    private ReturnSettlement.SettlementType settlementType;
    
    @NotNull(message = "Settlement date is required")
    private LocalDate settlementDate;
    
    private Long returnOrderId;
    private Long receiptId;
    
    private BigDecimal settlementAmount;
    
    // Credit Note fields
    private String creditNoteNumber;
    private LocalDate creditNoteDate;
    private BigDecimal creditNoteAmount;
    
    // Replacement fields
    private Long replacementOrderId;
    private String replacementOrderNumber;
    private Integer replacementQuantity;
    
    // Refund fields
    private String refundReference;
    private LocalDate refundDate;
    private BigDecimal refundAmount;
    private ReturnSettlement.RefundStatus refundStatus;
    
    private String remarks;
}