package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "item_code", nullable = false, unique = true, length = 50)
    private String itemCode;
    
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "uom", nullable = false, length = 20)
    private String uom;
    
    // GST Fields
    @Column(name = "gst_rate")
    private Double gstRate;
    
    @Column(name = "gst_hsn_code", length = 20)
    private String gstHsnCode;
    
    @Column(name = "gst_sac_code", length = 20)
    private String gstSacCode;
    
    @Column(name = "is_gst_applicable")
    private Boolean isGstApplicable = true;
    
    @Column(name = "cgst_rate")
    private Double cgstRate;
    
    @Column(name = "sgst_rate")
    private Double sgstRate;
    
    @Column(name = "igst_rate")
    private Double igstRate;
    
    // Additional Fields
    @Column(name = "unit_price")
    private Double unitPrice;
    
    @Column(name = "current_stock")
    private Integer currentStock = 0;
    
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;
    
    @Column(name = "reorder_level")
    private Integer reorderLevel = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "category", length = 100)
    private String category;
    
    @Column(name = "brand", length = 100)
    private String brand;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}