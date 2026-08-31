// ====== FILE: src/main/java/com/warehouse/wms/entity/Bin.java ======
package com.warehouse.wms.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;

    
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

    private BigDecimal utilizationPercentage;

    
    public enum BinStatus {
        AVAILABLE, FULL, BLOCKED
    }

    // ✅ IMPROVED getFullLocation() with better error handling
    public String getFullLocation() {
        try {
            if (level == null) {
                return null;
            }
            
            Rack rack = level.getRack();
            if (rack == null) {
                return null;
            }
            
            Aisle aisle = rack.getAisle();
            if (aisle == null) {
                return null;
            }
            
            Zone zone = aisle.getZone();
            if (zone == null) {
                return null;
            }
            
            Warehouse warehouse = zone.getWarehouse();
            if (warehouse == null) {
                return null;
            }
            
            return String.format("%s/%s/%s/%s/%s/%s",
                    warehouse.getWarehouseId(),
                    zone.getZoneId(),
                    aisle.getAisleId(),
                    rack.getRackId(),
                    level.getLevelId(),
                    barcode);
        } catch (Exception e) {
            return null;
        }
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