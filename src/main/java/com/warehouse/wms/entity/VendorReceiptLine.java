package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_vendor_receipt_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "dispatched_quantity")
    private Integer dispatchedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "accepted_quantity")
    private Integer acceptedQuantity;

    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity;

    @Column(name = "short_quantity")
    private Integer shortQuantity;

    @Column(name = "damaged_quantity")
    private Integer damagedQuantity;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "damaged_remarks", columnDefinition = "TEXT")
    private String damagedRemarks;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LineReceiptStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    private VendorReceipt receipt;

    @Column(name = "vro_line_id")
    private Long vroLineId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LineReceiptStatus {
        FULLY_ACCEPTED("Fully Accepted"),
        PARTIALLY_ACCEPTED("Partially Accepted"),
        FULLY_REJECTED("Fully Rejected");

        private final String displayName;

        LineReceiptStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}