package com.warehouse.wms.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_purchase_return_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReturnLine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "uom")
    private String uom;
    
    @Column(name = "return_quantity", nullable = false)
    private Integer returnQuantity = 0;
    
    @Column(name = "unit_price")
    private Double unitPrice = 0.0;
    
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;
    
    @Column(name = "original_quantity")
    private Integer originalQuantity = 0;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_return_id")
    private PurchaseReturn purchaseReturn;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_line_id")
    private InboundLine inboundLine;
}