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
@Table(name = "wms_purchase_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pr_number", nullable = false, unique = true)
    private String prNumber;
    
    @Column(name = "pr_date", nullable = false)
    private LocalDate prDate;
    
    @Column(name = "requested_by", nullable = false)
    private String requestedBy;
    
    @Column(name = "department", nullable = false)
    private String department;
    
    @Column(name = "warehouse", nullable = false)
    private String warehouse;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;
    
    @Column(name = "required_date", nullable = false)
    private LocalDate requiredDate;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "Aproval_remarks", columnDefinition = "TEXT")
    private String aprovalRemarks;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.DRAFT;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;
    
    @Column(name = "total_gst")
    private Double totalGst = 0.0;
    
    @Column(name = "grand_total")
    private Double grandTotal = 0.0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @OneToMany(mappedBy = "purchaseRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PurchaseRequestItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addItem(PurchaseRequestItem item) {
        items.add(item);
        item.setPurchaseRequest(this);
    }
    
    public void removeItem(PurchaseRequestItem item) {
        items.remove(item);
        item.setPurchaseRequest(null);
    }
    
    public void calculateTotals() {
        this.totalAmount = items.stream()
            .mapToDouble(PurchaseRequestItem::getTotalPrice)
            .sum();
        
        this.totalGst = items.stream()
            .mapToDouble(PurchaseRequestItem::getGstAmount)
            .sum();
        
        this.grandTotal = this.totalAmount + this.totalGst;
    }
}