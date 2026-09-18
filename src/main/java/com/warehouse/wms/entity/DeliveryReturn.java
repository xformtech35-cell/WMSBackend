package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_delivery_return", indexes = {
    @Index(name = "idx_ret_delivery", columnList = "delivery_id"),
    @Index(name = "idx_ret_number", columnList = "return_number"),
    @Index(name = "idx_ret_status", columnList = "return_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION TO EXISTING DELIVERY =================
    @Column(name = "delivery_id", nullable = false)
    private Long deliveryId;

    // Denormalized snapshot (so return remains meaningful even if delivery changes)
    @Column(name = "delivery_number", nullable = false, length = 50)
    private String deliveryNumber;

    @Column(name = "shipment_number", length = 50)
    private String shipmentNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    // ================= RETURN HEADER =================
    @Column(name = "return_number", unique = true, nullable = false, length = 50)
    private String returnNumber;

    @Column(name = "return_status", nullable = false, length = 30)
    private String returnStatus; // PENDING, APPROVED, REJECTED, IN_TRANSIT, RECEIVED, QC_PASSED, QC_FAILED, COMPLETED, CANCELLED

    @Column(name = "return_reason", length = 500)
    private String returnReason;

    @Column(name = "return_requested_by", length = 100)
    private String returnRequestedBy;

    @Column(name = "return_requested_at")
    private LocalDateTime returnRequestedAt;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "return_received_by", length = 100)
    private String returnReceivedBy;

    @Column(name = "return_received_at")
    private LocalDateTime returnReceivedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "refund_amount")
    private Double refundAmount;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= ITEMS =================
    @OneToMany(mappedBy = "deliveryReturn", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DeliveryReturnItem> items = new ArrayList<>();
}