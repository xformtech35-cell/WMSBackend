// ====== FILE: src/main/java/com/warehouse/wms/entity/Bin.java ======
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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wms_bins")
public class Bin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id")
    private Rack rack;

    // ✅ ADD LEVEL RELATIONSHIP
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;
    
    private String unit;


    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(nullable = false)
    private BigDecimal lengthCm;

    @Column(nullable = false)
    private BigDecimal widthCm;

    @Column(nullable = false)
    private BigDecimal heightCm;

    @Column(name = "volume_cm3")
    private BigDecimal volumeCm3;
    
    
    
    @Column(name = "barcode_data", length = 100)
    private String barcodeData; // Store the actual full barcode data (warehouseId-zoneId-aisleId-rackId-levelId-barcode)

    @Column(name = "barcode_image", columnDefinition = "TEXT")
    private String barcodeImage; // Base64 encoded barcode image

    @Column(name = "barcode_format", length = 20)
    private String barcodeFormat; // CODE128, CODE39, etc.

    @Column(name = "max_weight_g", nullable = false)
    private BigDecimal maxWeightG;

    @Column(name = "occupied_volume_cm3", columnDefinition = "DECIMAL(10, 2) DEFAULT 0")
    private BigDecimal occupiedVolumeCm3;

    @Column(name = "occupied_weight_g", columnDefinition = "DECIMAL(10, 2) DEFAULT 0")
    private BigDecimal occupiedWeightG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BinStatus status = BinStatus.AVAILABLE;

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

    public enum BinStatus {
        AVAILABLE, FULL, BLOCKED
    }

    // ✅ UPDATED Helper methods with Level
    public String getFullLocation() {
        if (level == null || level.getRack() == null || level.getRack().getAisle() == null || 
            level.getRack().getAisle().getZone() == null || 
            level.getRack().getAisle().getZone().getWarehouse() == null) {
            return null;
        }
        return String.format("%s-%s-%s-%s-%s-%s",
                level.getRack().getAisle().getZone().getWarehouse().getWarehouseId(),
                level.getRack().getAisle().getZone().getZoneId(),
                level.getRack().getAisle().getAisleId(),
                level.getRack().getRackId(),
                level.getLevelId(),
                barcode);
    }

    public BigDecimal getAvailableVolume() {
        if (volumeCm3 == null || occupiedVolumeCm3 == null) {
            return volumeCm3;
        }
        return volumeCm3.subtract(occupiedVolumeCm3);
    }

    public BigDecimal getAvailableWeight() {
        if (maxWeightG == null || occupiedWeightG == null) {
            return maxWeightG;
        }
        return maxWeightG.subtract(occupiedWeightG);
    }

    public boolean hasAvailableSpace(BigDecimal volume, BigDecimal weight) {
        if (getAvailableVolume() == null || getAvailableWeight() == null) {
            return false;
        }
        return getAvailableVolume().compareTo(volume) >= 0 && 
               getAvailableWeight().compareTo(weight) >= 0;
    }
}