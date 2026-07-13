package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_purchase_request_items")  // Match the table name in logs
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private String uom;
    
    @Column(name = "requested_qty", nullable = false)
    private Integer requestedQty;
    
    @Column(name = "current_stock")
    private Integer currentStock;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "item_barcode")
    private String itemBarcode;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "pending_quantity")
    private Integer pendingQuantity = 0;
    
    @Column(name = "item_status")
    private String itemStatus = "PENDING";
    
    @OneToMany(mappedBy = "purchaseRequestItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemReceipt> receipts = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;
}