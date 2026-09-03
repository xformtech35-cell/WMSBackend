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
@Table(name = "wms_vendor_return_order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "uom")
    private String uom;
    
    
    private String rejectedArea;


    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity = 0;

    @Column(name = "qc_quantity")
    private Integer qcQuantity = 0;

    @Column(name = "packed_quantity")
    private Integer packedQuantity = 0;

    @Column(name = "dispatched_quantity")
    private Integer dispatchedQuantity = 0;

    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "pick_location")
    private String pickLocation;

    @Column(name = "pick_sequence")
    private Integer pickSequence;

    @Column(name = "pack_barcode")
    private String packBarcode;

    @Column(name = "qc_status")
    @Enumerated(EnumType.STRING)
    private QCStatus qcStatus = QCStatus.PENDING;

    @Column(name = "qc_remarks", columnDefinition = "TEXT")
    private String qcRemarks;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private LineStatus status = LineStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vro_id")
    private VendorReturnOrder returnOrder;

    @Column(name = "return_request_line_id")
    private Long returnRequestLineId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum QCStatus {
        PENDING("Pending"),
        PASSED("Passed"),
        FAILED("Failed"),
        REJECTED("Rejected");

        private final String displayName;

        QCStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum LineStatus {
        PENDING("Pending"),
        PICKED("Picked"),
        QC_PASSED("QC Passed"),
        QC_FAILED("QC Failed"),
        PACKED("Packed"),
        DISPATCHED("Dispatched"),
        RECEIVED("Received");

        private final String displayName;

        LineStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}