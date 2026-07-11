package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_request_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code")
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_barcode")
    private String itemBarcode;
    
    // ✅ ADD THIS NEW FIELD
    @Column(name = "batch_no")
    private String batchNo;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    private String unit = "pcs";
    
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
    
    private Double total = 0.0;
    
    private String remarks;
    
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