package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_delivery_return_item", indexes = {
    @Index(name = "idx_retitem_return", columnList = "return_id"),
    @Index(name = "idx_retitem_code", columnList = "item_code"),
    @Index(name = "idx_retitem_status", columnList = "item_return_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_id", nullable = false)
    private DeliveryReturn deliveryReturn;

    // ============ ITEM IDENTITY ============
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "uom", length = 20)
    private String uom;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    // ============ QUANTITIES ============
    @Column(name = "delivered_qty")
    private Integer deliveredQty = 0;

    @Column(name = "return_qty", nullable = false)
    private Integer returnQty = 0;

    @Column(name = "received_qty")
    private Integer receivedQty = 0;

    @Column(name = "accepted_qty")
    private Integer acceptedQty = 0;

    @Column(name = "rejected_qty")
    private Integer rejectedQty = 0;

    // ============ STATUS ============
    @Column(name = "item_return_status", nullable = false, length = 30)
    private String itemReturnStatus; // PENDING, APPROVED, REJECTED, RECEIVED, QC_PASSED, QC_FAILED, RESTOCKED, SCRAPPED

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "qc_status", length = 30)
    private String qcStatus;

    @Column(name = "qc_remarks", columnDefinition = "TEXT")
    private String qcRemarks;

    @Column(name = "qc_checked_by", length = 100)
    private String qcCheckedBy;

    @Column(name = "qc_checked_at")
    private LocalDateTime qcCheckedAt;

    @Column(name = "restocked")
    private Boolean restocked = false;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}