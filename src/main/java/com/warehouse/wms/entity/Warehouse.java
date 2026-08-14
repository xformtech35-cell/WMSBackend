// ====== FILE: src/main/java/com/warehouse/wms/entity/Warehouse.java ======
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
@Table(name = "wms_warehouses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "warehouse_id", nullable = false, unique = true, length = 20)
    private String warehouseId; // WH-01

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    private Integer maxCapacity;
    private Integer minCapacity;

    private String capacityUnit;
    
    
    @Column(name = "barcode_data", length = 50)
    private String barcodeData; // Store the actual barcode data (warehouseId)

    @Column(name = "barcode_image", columnDefinition = "TEXT")
    private String barcodeImage; // Base64 encoded barcode image

    @Column(name = "barcode_format", length = 20)
    private String barcodeFormat; // CODE128, CODE39, EAN13, etc.

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "total_zones")
    private Integer totalZones = 0;

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
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Zone> zones = new ArrayList<>();

    public void addZone(Zone zone) {
        if (this.zones == null) {
            this.zones = new ArrayList<>();
        }
        this.zones.add(zone);
        zone.setWarehouse(this);
        this.totalZones = this.zones.size();
    }

    public void removeZone(Zone zone) {
        if (this.zones != null) {
            this.zones.remove(zone);
            zone.setWarehouse(null);
            this.totalZones = this.zones.size();
        }
    }
}