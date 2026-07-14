package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_purchase_order_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private Sku skuId;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "hsn_code", length = 50)
    private String hsnCode;
    
    @Column(name = "uom", nullable = false)
    private String uom;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "gst_rate")
    private Double gstRate = 0.0;
    
    @Column(name = "sgst_rate")
    private Double sgstRate = 0.0;
    
    @Column(name = "cgst_rate")
    private Double cgstRate = 0.0;
    
    @Column(name = "igst_rate")
    private Double igstRate = 0.0;
    
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
    
    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    @Column(name = "total_price")
    private Double totalPrice = 0.0;
    
    @Column(name = "gst_amount")
    private Double gstAmount = 0.0;
    
    @Column(name = "total_with_gst")
    private Double totalWithGst = 0.0;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "pending_quantity")
    private Integer pendingQuantity = 0;
    
    @Column(name = "line_status")
    private String lineStatus = "PENDING";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    
    // Helper methods
    public void calculatePrice() {
        // Calculate total price
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice * quantity;
            
            // Calculate discount
            if (discountPercentage != null && discountPercentage > 0) {
                this.discountAmount = totalPrice * (discountPercentage / 100);
                this.totalPrice = this.totalPrice - this.discountAmount;
            }
            
            // Calculate GST
            if (gstRate != null && gstRate > 0) {
                this.gstAmount = this.totalPrice * (gstRate / 100);
            } else {
                this.gstAmount = 0.0;
            }
            
            this.totalWithGst = this.totalPrice + this.gstAmount;
        }
    }
}