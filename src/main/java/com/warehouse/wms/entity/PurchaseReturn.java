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
@Table(name = "wms_purchase_return")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "return_number", nullable = false, unique = true)
    private String returnNumber;
    
    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;
    
    @Column(name = "po_number")
    private String poNumber;
    
    @Column(name = "grn_number")
    private String grnNumber;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @Column(name = "supplier_name")
    private String supplierName;
    
    @Column(name = "supplier_code")
    private String supplierCode;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "return_type")
    @Enumerated(EnumType.STRING)
    private ReturnType returnType;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReturnStatus status = ReturnStatus.PENDING;
    
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;
    
    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "rejected_by")
    private Long rejectedBy;
    
    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "shipped_by")
    private Long shippedBy;
    
    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "tracking_name")
    private String trackingName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id")
    private Inbound inbound;
    
    @OneToMany(mappedBy = "purchaseReturn", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PurchaseReturnLine> lines = new ArrayList<>();
    
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
    
    // Helper methods
    public void addLine(PurchaseReturnLine line) {
        lines.add(line);
        line.setPurchaseReturn(this);
        updateTotals();
    }
    
    public void removeLine(PurchaseReturnLine line) {
        lines.remove(line);
        line.setPurchaseReturn(null);
        updateTotals();
    }
    
    // ✅ Make this method public instead of private
    public void updateTotals() {
        this.totalQuantity = lines.stream()
                .mapToInt(PurchaseReturnLine::getReturnQuantity)
                .sum();
        this.totalAmount = lines.stream()
                .mapToDouble(line -> {
                    if (line.getTotalAmount() != null) {
                        return line.getTotalAmount();
                    }
                    return line.getReturnQuantity() * (line.getUnitPrice() != null ? line.getUnitPrice() : 0.0);
                })
                .sum();
    }
    
    // ✅ Also add a method to update totals from outside
    public void recalculateTotals() {
        updateTotals();
    }
    
    public enum ReturnType {
        DAMAGED("Damaged"),
        DEFECTIVE("Defective"),
        EXCESS("Excess Quantity"),
        WRONG_ITEM("Wrong Item"),
        QUALITY_ISSUE("Quality Issue"),
        EXPIRED("Expired"),
        OTHER("Other");
        
        private final String displayName;
        
        ReturnType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ReturnStatus {
        PENDING("Pending"),
        APPROVED("Approved"),
        REJECTED("Rejected"),
        SHIPPED("Shipped"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        ReturnStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}