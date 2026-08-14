// ====== FILE: src/main/java/com/warehouse/wms/entity/Zone.java ======
package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_zones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id", nullable = false, unique = true, length = 10)
    private String zoneId; // A, B, C

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "zone_type", length = 20)
    private String zoneType; // PICKING, BULK, OVERFLOW, DANGEROUS
    
    @Column(name = "barcode_data", length = 50)
    private String barcodeData; // Store the actual barcode data (warehouseId-zoneId)

    @Column(name = "barcode_image", columnDefinition = "TEXT")
    private String barcodeImage; // Base64 encoded barcode image

    @Column(name = "barcode_format", length = 20)
    private String barcodeFormat; // CODE128, CODE39, etc.

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @Column(name = "total_aisles")
    private Integer totalAisles = 0;

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
    
    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @JsonIgnore
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Aisle> aisles = new ArrayList<>();

    public void addAisle(Aisle aisle) {
        if (this.aisles == null) {
            this.aisles = new ArrayList<>();
        }
        this.aisles.add(aisle);
        aisle.setZone(this);
        this.totalAisles = this.aisles.size();
    }

    public void removeAisle(Aisle aisle) {
        if (this.aisles != null) {
            this.aisles.remove(aisle);
            aisle.setZone(null);
            this.totalAisles = this.aisles.size();
        }
    }
    
    public String getFullZoneIdentifier() {
        if (warehouse != null && warehouse.getWarehouseId() != null) {
            return warehouse.getWarehouseId() + "-" + zoneId;
        }
        return zoneId;
    }
}