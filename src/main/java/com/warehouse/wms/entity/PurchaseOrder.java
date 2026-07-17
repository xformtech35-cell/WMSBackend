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
@Table(name = "wms_purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "po_number", nullable = false, unique = true)
    private String poNumber;
    
    @Column(name = "po_date", nullable = false)
    private LocalDate poDate;
    
    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;
    
    @Column(name = "subtotal")
    private Double subtotal = 0.0;
    
    @Column(name = "total_gst")
    private Double totalGst = 0.0;
    
    @Column(name = "grand_total")
    private Double grandTotal = 0.0;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    @Column(name = "shipping_charges")
    private Double shippingCharges = 0.0;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @Column(name = "supplier_name")
    private String supplierName;
    
    @Column(name = "supplier_email")
    private String supplierEmail;
    
    @Column(name = "supplier_phone")
    private String supplierPhone;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;
    
    @Column(name = "purchase_request_id")
    private Long purchaseRequestId; // Reference to source PR
    
    private String purchaseRequestNumber; // Reference to source PR

   
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PurchaseOrderLine> lines = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addLine(PurchaseOrderLine line) {
        lines.add(line);
        line.setPurchaseOrder(this);
    }
    
    public void removeLine(PurchaseOrderLine line) {
        lines.remove(line);
        line.setPurchaseOrder(null);
    }
    
    public void calculateTotals() {
        this.subtotal = lines.stream()
            .mapToDouble(PurchaseOrderLine::getTotalPrice)
            .sum();
        
        this.totalGst = lines.stream()
            .mapToDouble(PurchaseOrderLine::getGstAmount)
            .sum();
        
        this.grandTotal = this.subtotal + this.totalGst - this.discountAmount + this.shippingCharges;
    }
}