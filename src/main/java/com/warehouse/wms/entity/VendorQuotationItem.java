package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_vendor_quotation_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorQuotationItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "uom", nullable = false)
    private String uom;
    
    @Column(name = "quantity", nullable = false)
    private Double quantity;
    
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
    
    @Column(name = "gst_rate")
    private Double gstRate = 0.0;
    
    @Column(name = "cgst_rate")
    private Double cgstRate = 0.0;
    
    @Column(name = "sgst_rate")
    private Double sgstRate = 0.0;
    
    @Column(name = "igst_rate")
    private Double igstRate = 0.0;
    
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;
    
    @Column(name = "gst_amount")
    private Double gstAmount = 0.0;
    
    @Column(name = "total_with_gst")
    private Double totalWithGst = 0.0;
    
    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_quotation_id")
    private VendorQuotation vendorQuotation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_item_id")
    private RfqItem rfqItem;
    
    // Helper method
    public void calculatePrice() {
        if (unitPrice != null && quantity != null) {
            this.totalAmount = unitPrice * quantity;
            
            if (discountPercentage != null && discountPercentage > 0) {
                this.discountAmount = totalAmount * (discountPercentage / 100);
                this.totalAmount = this.totalAmount - this.discountAmount;
            }
            
            if (gstRate != null && gstRate > 0) {
                this.gstAmount = this.totalAmount * (gstRate / 100);
            } else {
                this.gstAmount = 0.0;
            }
            
            this.totalWithGst = this.totalAmount + this.gstAmount;
        }
    }
}