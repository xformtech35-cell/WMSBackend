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
@Table(name = "wms_rfqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rfq {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rfq_number", nullable = false, unique = true)
    private String rfqNumber;
    
    @Column(name = "rfq_date", nullable = false)
    private LocalDate rfqDate;
    
    @Column(name = "closing_date")
    private LocalDate closingDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RfqStatus status = RfqStatus.DRAFT;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Column(name = "delivery_terms", columnDefinition = "TEXT")
    private String deliveryTerms;
    
    @Column(name = "payment_terms", columnDefinition = "TEXT")
    private String paymentTerms;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    
    @Column(name = "supplier_ids", columnDefinition = "TEXT")
    private String supplierIds;  // Will store like "[1, 2, 3, 4, 5]"
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<RfqItem> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorQuotation> vendorQuotations = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addItem(RfqItem item) {
        items.add(item);
        item.setRfq(this);
    }
    
    public void removeItem(RfqItem item) {
        items.remove(item);
        item.setRfq(null);
    }
    
    public void addVendorQuotation(VendorQuotation quotation) {
        vendorQuotations.add(quotation);
        quotation.setRfq(this);
    }
}