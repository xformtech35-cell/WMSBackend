package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wms_return_settlement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_number", nullable = false, unique = true)
    private String settlementNumber;

    @Column(name = "settlement_type")
    @Enumerated(EnumType.STRING)
    private SettlementType settlementType;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "settlement_amount")
    private BigDecimal settlementAmount;

    @Column(name = "credit_note_number")
    private String creditNoteNumber;

    @Column(name = "credit_note_date")
    private LocalDate creditNoteDate;

    @Column(name = "credit_note_amount")
    private BigDecimal creditNoteAmount;

    @Column(name = "replacement_order_id")
    private Long replacementOrderId;

    @Column(name = "replacement_order_number")
    private String replacementOrderNumber;

    @Column(name = "replacement_quantity")
    private Integer replacementQuantity;

    @Column(name = "refund_reference")
    private String refundReference;

    @Column(name = "refund_date")
    private LocalDate refundDate;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vro_id")
    private VendorReturnOrder returnOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private VendorReceipt receipt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    public enum SettlementType {
        CREDIT_NOTE("Credit Note"),
        REPLACEMENT("Replacement"),
        REFUND("Refund");

        private final String displayName;

        SettlementType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum SettlementStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        SettlementStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum RefundStatus {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed");

        private final String displayName;

        RefundStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}