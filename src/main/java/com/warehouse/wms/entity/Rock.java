// ====== FILE: src/main/java/com/warehouse/wms/entity/Rock.java ======
package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wms_rocks", indexes = {
    @Index(name = "idx_rock_id", columnList = "rock_id"),
    @Index(name = "idx_warehouse_id", columnList = "warehouse_id"),
    @Index(name = "idx_rock_type", columnList = "rock_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rock_id", nullable = false, unique = true, length = 50)
    private String rockId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "rock_type", length = 50)
    private String rockType;

    // ✅ Use BigDecimal instead of Double with precision and scale
    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "length_cm", precision = 10, scale = 2)
    private BigDecimal lengthCm;

    @Column(name = "width_cm", precision = 10, scale = 2)
    private BigDecimal widthCm;

    @Column(name = "height_cm", precision = 10, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "density_g_cm3", precision = 10, scale = 2)
    private BigDecimal densityGcm3;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "hardness")
    private Integer hardness; // Mohs scale 1-10

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "min_quantity")
    private Integer minQuantity = 0;

    @Column(name = "max_quantity")
    private Integer maxQuantity = 0;

    // ✅ Use BigDecimal for currency
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    // ✅ Helper methods with BigDecimal
    public BigDecimal getVolumeCm3() {
        if (lengthCm == null || widthCm == null || heightCm == null) {
            return null;
        }
        return lengthCm.multiply(widthCm).multiply(heightCm);
    }

    public BigDecimal getTotalWeight() {
        if (weightKg == null || quantity == null) {
            return null;
        }
        return weightKg.multiply(BigDecimal.valueOf(quantity));
    }
}