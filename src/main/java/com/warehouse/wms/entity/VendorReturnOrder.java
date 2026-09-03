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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_vendor_return_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vro_number", nullable = false, unique = true)
    private String vroNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    @Column(name = "actual_return_date")
    private LocalDate actualReturnDate;

    @Column(name = "return_type")
    @Enumerated(EnumType.STRING)
    private VendorReturnRequest.ReturnType returnType;

    @Column(name = "return_reason", columnDefinition = "TEXT")
    private String returnReason;

    @Column(name = "supplier_name")
    private String supplierName;
    
    private String rockArea;


    @Column(name = "supplier_code")
    private String supplierCode;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private VendorReturnRequest.Priority priority = VendorReturnRequest.Priority.MEDIUM;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "tracking_name")
    private String trackingName;

    @Column(name = "pick_list_generated")
    private Boolean pickListGenerated = false;

    @Column(name = "pick_list_generated_at")
    private LocalDateTime pickListGeneratedAt;

    @Column(name = "picked_by")
    private Long pickedBy;

    @Column(name = "picked_at")
    private LocalDateTime pickedAt;

    @Column(name = "qc_verified_by")
    private Long qcVerifiedBy;

    @Column(name = "qc_verified_at")
    private LocalDateTime qcVerifiedAt;

    @Column(name = "packed_by")
    private Long packedBy;
    
    
    private String assignTo;


    @Column(name = "packed_at")
    private LocalDateTime packedAt;

    @Column(name = "dispatched_by")
    private Long dispatchedBy;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "dispatch_number")
    private String dispatchNumber;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id")
    private VendorReturnRequest returnRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToMany(mappedBy = "returnOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorReturnOrderLine> lines = new ArrayList<>();

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

    public enum OrderStatus {
        CREATED("Created"),
        PENDING_PICKING("Pending Picking"),
        PICKING("Picking"),
        PENDING_QC("Pending QC"),
        QC("QC In Progress"),
        QC_PASSED("QC Passed"),
        QC_FAILED("QC Failed"),
        PENDING_PACKING("Pending Packing"),
        PACKED("Packed"),
        DISPATCHED("Dispatched"),
        IN_TRANSIT("In Transit"),
        RECEIVED("Received"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        OrderStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void addLine(VendorReturnOrderLine line) {
        lines.add(line);
        line.setReturnOrder(this);
        updateTotals();
    }

    public void removeLine(VendorReturnOrderLine line) {
        lines.remove(line);
        line.setReturnOrder(null);
        updateTotals();
    }

    public void updateTotals() {
        this.totalQuantity = lines.stream()
                .mapToInt(VendorReturnOrderLine::getOrderQuantity)
                .sum();
        this.totalAmount = lines.stream()
                .map(VendorReturnOrderLine::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEditable() {
        return status == OrderStatus.CREATED || status == OrderStatus.PENDING_PICKING;
    }

    public boolean canPick() {
        return status == OrderStatus.PENDING_PICKING || status == OrderStatus.PICKING;
    }

    public boolean canQC() {
        return status == OrderStatus.PENDING_QC || status == OrderStatus.QC;
    }

    public boolean canPack() {
        return status == OrderStatus.QC_PASSED || status == OrderStatus.PENDING_PACKING;
    }

    public boolean canDispatch() {
        return status == OrderStatus.PACKED;
    }
}