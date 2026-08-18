// ====== FILE: src/main/java/com/warehouse/wms/entity/StockAvailability.java ======
package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_stock_availability", 
       indexes = {
           @Index(name = "idx_stock_warehouse_item", columnList = "warehouse_id,item_code"),
           @Index(name = "idx_stock_zone_item", columnList = "warehouse_id,zone_id,item_code"),
           @Index(name = "idx_stock_aisle_item", columnList = "warehouse_id,zone_id,aisle_id,item_code"),
           @Index(name = "idx_stock_rack_item", columnList = "warehouse_id,zone_id,aisle_id,rack_id,item_code"),
           @Index(name = "idx_stock_level_item", columnList = "warehouse_id,zone_id,aisle_id,rack_id,level_id,item_code"),
           @Index(name = "idx_stock_bin_item", columnList = "warehouse_id,zone_id,aisle_id,rack_id,level_id,bin_id,item_code"),
           @Index(name = "idx_stock_location_level", columnList = "location_level")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Location identifiers at each level
    @Column(name = "warehouse_id", length = 20, nullable = false)
    private String warehouseId;

    @Column(name = "zone_id", length = 10)
    private String zoneId;

    @Column(name = "aisle_id", length = 10)
    private String aisleId;

    @Column(name = "rack_id", length = 10)
    private String rackId;

    @Column(name = "level_id", length = 20)
    private String levelId;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "bin_barcode", length = 100)
    private String binBarcode;

    // Item details
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    // Stock quantities
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "in_transit_quantity", nullable = false)
    private Integer inTransitQuantity = 0;

    // Location level enum
    @Enumerated(EnumType.STRING)
    @Column(name = "location_level", nullable = false, length = 20)
    private LocationLevel locationLevel;

    // Batch tracking
    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    // Capacity information
    @Column(name = "max_capacity")
    private Integer maxCapacity;
    
    @Column(name = "min_capacity")
    private Integer minCapacity;
    
    
    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Column(name = "qr_code_value", length = 100)
    private String qrCodeValue;
 
    
    

    @Column(name = "utilization_percentage")
    private Double utilizationPercentage;

    @Column(name = "last_putaway_date")
    private LocalDateTime lastPutawayDate;

    @Column(name = "last_pick_date")
    private LocalDateTime lastPickDate;

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

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public enum LocationLevel {
        WAREHOUSE("WH"),
        ZONE("ZN"),
        AISLE("AL"),
        RACK("RK"),
        LEVEL("LV"),
        BIN("BN");

        private final String code;

        LocationLevel(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    // ====== Helper Methods ======

    public void addQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        this.totalQuantity += quantity;
        this.availableQuantity += quantity;
        this.lastPutawayDate = LocalDateTime.now();
        updateUtilization();
    }

    public void removeQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        this.totalQuantity -= quantity;
        this.availableQuantity -= quantity;
        if (this.availableQuantity < 0) this.availableQuantity = 0;
        if (this.totalQuantity < 0) this.totalQuantity = 0;
        this.lastPickDate = LocalDateTime.now();
        updateUtilization();
    }

    public void reserveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0 || quantity > this.availableQuantity) return;
        this.reservedQuantity += quantity;
        this.availableQuantity -= quantity;
        this.lastPickDate = LocalDateTime.now();
        updateUtilization();
    }

    public void unreserveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0 || quantity > this.reservedQuantity) return;
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
        updateUtilization();
    }

    public void addInTransit(Integer quantity) {
        if (quantity == null || quantity <= 0) return;
        this.inTransitQuantity += quantity;
    }

    public void removeInTransit(Integer quantity) {
        if (quantity == null || quantity <= 0 || quantity > this.inTransitQuantity) return;
        this.inTransitQuantity -= quantity;
    }

    private void updateUtilization() {
        if (maxCapacity != null && maxCapacity > 0) {
            this.utilizationPercentage = (double) totalQuantity / maxCapacity * 100;
        }
    }

    public boolean isFull() {
        return maxCapacity != null && totalQuantity >= maxCapacity;
    }

    public boolean isAvailable() {
        return availableQuantity != null && availableQuantity > 0;
    }

    public String getFullLocationPath() {
        StringBuilder path = new StringBuilder();
        if (warehouseId != null) path.append(warehouseId);
        if (zoneId != null) path.append("-").append(zoneId);
        if (aisleId != null) path.append("-").append(aisleId);
        if (rackId != null) path.append("-").append(rackId);
        if (levelId != null) path.append("-").append(levelId);
        if (binId != null) path.append("-").append(binId);
        return path.toString();
    }
    
    
 




}