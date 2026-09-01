package com.warehouse.wms.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.warehouse.wms.entity.ReturnSettlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponseDTO {
    private Long id;
    private String settlementNumber;
    
    // Settlement type
    private ReturnSettlement.SettlementType settlementType;
    private String settlementTypeDisplayName;
    
    private LocalDate settlementDate;
    
    // References
    private Long returnOrderId;
    private String returnOrderNumber;
    private Long receiptId;
    private String receiptNumber;
    
    // Amount
    private BigDecimal settlementAmount;
    private String currency;
    
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
    private String refundStatusDisplayName;
    
    // Status
    private ReturnSettlement.SettlementStatus status;
    private String statusDisplayName;
    
    private String remarks;
    
    // Audit
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // Summary
    private SettlementSummary summary;
}