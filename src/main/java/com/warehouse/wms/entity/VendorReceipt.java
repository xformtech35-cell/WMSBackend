package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_vendor_receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "total_received_quantity")
    private Integer totalReceivedQuantity = 0;

    @Column(name = "total_accepted_quantity")
    private Integer totalAcceptedQuantity = 0;

    @Column(name = "total_rejected_quantity")
    private Integer totalRejectedQuantity = 0;

    @Column(name = "total_short_quantity")
    private Integer totalShortQuantity = 0;

    @Column(name = "total_damaged_quantity")
    private Integer totalDamagedQuantity = 0;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReceiptStatus status = ReceiptStatus.PENDING;

    @Column(name = "receipt_document_path")
    private String receiptDocumentPath;

    @Column(name = "acknowledgment_number")
    private String acknowledgmentNumber;

    @Column(name = "acknowledgment_date")
    private LocalDate acknowledgmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vro_id")
    private VendorReturnOrder returnOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id")
    private ReturnDispatch dispatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorReceiptLine> lines = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReceiptStatus {
        PENDING("Pending"),
        PARTIAL("Partial"),
        COMPLETED("Completed"),
        REJECTED("Rejected");

        private final String displayName;

        ReceiptStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void addLine(VendorReceiptLine line) {
        lines.add(line);
        line.setReceipt(this);
        updateTotals();
    }

    public void updateTotals() {
        this.totalReceivedQuantity = lines.stream()
                .mapToInt(VendorReceiptLine::getReceivedQuantity)
                .sum();
        this.totalAcceptedQuantity = lines.stream()
                .mapToInt(VendorReceiptLine::getAcceptedQuantity)
                .sum();
        this.totalRejectedQuantity = lines.stream()
                .mapToInt(VendorReceiptLine::getRejectedQuantity)
                .sum();
        this.totalShortQuantity = lines.stream()
                .mapToInt(VendorReceiptLine::getShortQuantity)
                .sum();
        this.totalDamagedQuantity = lines.stream()
                .mapToInt(VendorReceiptLine::getDamagedQuantity)
                .sum();
    }
}