// ====== FILE: src/main/java/com/warehouse/wms/entity/Sku.java ======
package com.warehouse.wms.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skus", indexes = {
    @Index(name = "idx_sku_code", columnList = "sku_code")
})
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String skuCode;

    @Column(length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;



    @Column(name = "uom", length = 20)
    private String uom; // Unit of Measure

    @Column(name = "is_perishable", nullable = false)
    private Boolean isPerishable = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToOne(mappedBy = "sku", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private SkuDimension dimension;
    
    
 // ====== FILE: src/main/java/com/warehouse/wms/entity/Sku.java ======
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;  // ✅ Already BigDecimal
}