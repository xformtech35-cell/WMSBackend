// ====== FILE: src/main/java/com/warehouse/wms/entity/BinLocation.java ======
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
@Table(name = "wms_bin_locations", indexes = {
    @Index(name = "idx_bin_id", columnList = "bin_id"),
    @Index(name = "idx_bin_barcode", columnList = "bin_barcode"),
    @Index(name = "idx_warehouse_zone", columnList = "warehouse_id, zone")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bin_id", nullable = false, unique = true, length = 50)
    private String binId;

    @Column(name = "bin_barcode", unique = true, length = 100)
    private String binBarcode;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "zone", length = 10)
    private String zone;

    @Column(name = "aisle", length = 10)
    private String aisle;

    @Column(name = "rack", length = 10)
    private String rack;

    @Column(name = "shelf", length = 10)
    private String shelf;

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "position", length = 10)
    private String position;

    @Column(name = "capacity")
    private Integer capacity = 0;

    @Column(name = "available_capacity")
    private Integer availableCapacity = 0;

    @Column(name = "used_capacity")
    private Integer usedCapacity = 0;

    @Column(name = "min_threshold")
    private Integer minThreshold = 0;

    @Column(name = "max_threshold")
    private Integer maxThreshold = 0;

    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "is_occupied")
    private Boolean isOccupied = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_reserved")
    private Boolean isReserved = false;

    @Column(name = "reserved_for", length = 50)
    private String reservedFor;

    @Column(name = "location_type", length = 20)
    private String locationType;

    @Column(name = "zone_type", length = 20)
    private String zoneType;

    @Column(name = "movement_type", length = 20)
    private String movementType;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "distance_from_dispatch")
    private Integer distanceFromDispatch;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "last_putaway_at")
    private LocalDateTime lastPutawayAt;

    @Column(name = "last_pick_at")
    private LocalDateTime lastPickAt;

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

    public Integer getAvailableCapacity() {
        return this.capacity - (this.usedCapacity != null ? this.usedCapacity : 0);
    }

    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
        this.usedCapacity = this.capacity - availableCapacity;
    }

    public void addUsedCapacity(Integer quantity) {
        if (this.usedCapacity == null) this.usedCapacity = 0;
        this.usedCapacity += quantity;
        this.availableCapacity = this.capacity - this.usedCapacity;
    }

    public void removeUsedCapacity(Integer quantity) {
        if (this.usedCapacity == null) this.usedCapacity = 0;
        this.usedCapacity -= quantity;
        if (this.usedCapacity < 0) this.usedCapacity = 0;
        this.availableCapacity = this.capacity - this.usedCapacity;
    }

    public Boolean hasCapacity(Integer quantity) {
        return this.availableCapacity != null && this.availableCapacity >= quantity;
    }

    public String getFullLocation() {
        return String.format("%s-%s-%s-%s-%s-%s", 
            warehouseId, zone, aisle, rack, shelf, binId);
    }
}