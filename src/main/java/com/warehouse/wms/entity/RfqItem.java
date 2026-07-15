package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_rfq_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RfqItem {
    
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
    
    @Column(name = "hsn_code", length = 50)
    private String hsnCode;
    
    @Column(name = "gst_rate")
    private Double gstRate = 0.0;
    
    @Column(name = "cgst_rate")
    private Double cgstRate = 0.0;
    
    @Column(name = "sgst_rate")
    private Double sgstRate = 0.0;
    
    @Column(name = "igst_rate")
    private Double igstRate = 0.0;
    
    @Column(name = "estimated_unit_price")
    private Double estimatedUnitPrice = 0.0;
    
    @Column(name = "estimated_total")
    private Double estimatedTotal = 0.0;
    
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id")
    private Rfq rfq;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_item_id")
    private PurchaseRequestItem purchaseRequestItem;
}