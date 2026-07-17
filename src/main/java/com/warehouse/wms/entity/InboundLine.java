package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_inbound_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InboundLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "uom", nullable = false)
    private String uom;
    
    @Column(name = "ordered_quantity")
    private Integer orderedQuantity = 0;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "pending_quantity")
    private Integer pendingQuantity = 0;
    
    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;
    
    // Quality Inspection
    @Column(name = "accepted_quantity")
    private Integer acceptedQuantity = 0;
    
    @Column(name = "rejected_quantity")
    private Integer rejectedQuantity = 0;
    
    @Column(name = "defective_quantity")
    private Integer defectiveQuantity = 0;
    
    @Column(name = "quality_status")
    private String qualityStatus; // GOOD, PARTIAL, REJECTED
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id")
    private Inbound inbound;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_line_id")
    private PurchaseOrderLine purchaseOrderLine;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;
}