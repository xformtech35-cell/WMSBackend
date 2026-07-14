package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_purchase_request_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Direct fields (for quick reference)
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "uom", nullable = false)
    private String uom;
    
    @Column(name = "requested_qty", nullable = false)
    private Integer requestedQty;
    
    @Column(name = "current_stock")
    private Integer currentStock;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    // GST Fields from Item
    @Column(name = "gst_rate")
    private Double gstRate;
    
    @Column(name = "gst_hsn_code")
    private String gstHsnCode;
    
    @Column(name = "is_gst_applicable")
    private Boolean isGstApplicable = true;
    
    @Column(name = "cgst_rate")
    private Double cgstRate;
    
    @Column(name = "sgst_rate")
    private Double sgstRate;
    
    @Column(name = "igst_rate")
    private Double igstRate;
    
    // Price fields
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
    
    @Column(name = "total_price")
    private Double totalPrice = 0.0;
    
    @Column(name = "gst_amount")
    private Double gstAmount = 0.0;
    
    @Column(name = "total_with_gst")
    private Double totalWithGst = 0.0;
    
    // Stock fields
    @Column(name = "item_barcode")
    private String itemBarcode;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "pending_quantity")
    private Integer pendingQuantity = 0;
    
    @Column(name = "item_status")
    private String itemStatus = "PENDING";
    
    // Relationship with Item entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
    
    @OneToMany(mappedBy = "purchaseRequestItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemReceipt> receipts = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;
    
    // Helper methods
    public void calculatePrice() {
        if (unitPrice != null && requestedQty != null) {
            this.totalPrice = unitPrice * requestedQty;
            
            if (isGstApplicable != null && isGstApplicable && gstRate != null) {
                this.gstAmount = totalPrice * (gstRate / 100);
            } else {
                this.gstAmount = 0.0;
            }
            
            this.totalWithGst = this.totalPrice + this.gstAmount;
        }
    }
    
    public Double getCgstAmount() {
        if (cgstRate != null && totalPrice != null) {
            return totalPrice * (cgstRate / 100);
        }
        return 0.0;
    }
    
    public Double getSgstAmount() {
        if (sgstRate != null && totalPrice != null) {
            return totalPrice * (sgstRate / 100);
        }
        return 0.0;
    }
    
    public Double getIgstAmount() {
        if (igstRate != null && totalPrice != null) {
            return totalPrice * (igstRate / 100);
        }
        return 0.0;
    }
}