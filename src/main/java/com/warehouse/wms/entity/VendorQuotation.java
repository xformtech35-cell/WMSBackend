package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_vendor_quotations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "quotation_number", nullable = false, unique = true)
    private String quotationNumber;
    
    @Column(name = "quotation_date", nullable = false)
    private LocalDate quotationDate;
    
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;
    
    @Column(name = "valid_till")
    private LocalDate validTill;
    
    @Column(name = "sub_total")
    private Double subTotal = 0.0;
    
    @Column(name = "gst_total")
    private Double gstTotal = 0.0;
    
    @Column(name = "grand_total")
    private Double grandTotal = 0.0;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    @Column(name = "shipping_charges")
    private Double shippingCharges = 0.0;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuotationStatus status = QuotationStatus.PENDING;
    
    @Column(name = "rank")
    private Integer rank;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;
    
    @OneToMany(mappedBy = "vendorQuotation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VendorQuotationItem> items = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // Helper methods
    public void addItem(VendorQuotationItem item) {
        items.add(item);
        item.setVendorQuotation(this);
    }
    
    public void calculateTotals() {
        this.subTotal = items.stream()
            .mapToDouble(VendorQuotationItem::getTotalAmount)
            .sum();
        
        this.gstTotal = items.stream()
            .mapToDouble(VendorQuotationItem::getGstAmount)
            .sum();
        
        this.grandTotal = this.subTotal + this.gstTotal - this.discountAmount + this.shippingCharges;
    }
}