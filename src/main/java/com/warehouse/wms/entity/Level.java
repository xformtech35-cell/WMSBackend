// ====== FILE: src/main/java/com/warehouse/wms/entity/Level.java ======
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
@Table(name = "wms_levels", indexes = {
    @Index(name = "idx_level_id", columnList = "level_id"),
    @Index(name = "idx_rack_id", columnList = "rack_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id", nullable = false, unique = true, length = 20)
    private String levelId;  // L-01, L-02, L-03

    @Column(name = "name", nullable = false, length = 100)
    private String name;  // "Level 1", "Level 2", "Level 3"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level_number")
    private Integer levelNumber;  // 1, 2, 3 (for sorting)

    @Column(name = "height_cm")
    private Double heightCm;  // Height of the level

    @Column(name = "max_weight_kg")
    private Double maxWeightKg;  // Max weight capacity for this level

    @Column(name = "max_items")
    private Integer maxItems;  // Max number of items

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id")
    private Rack rack;

    @JsonIgnore
    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Bin> bins = new ArrayList<>();

    // Helper methods
    public void addBin(Bin bin) {
        if (this.bins == null) {
            this.bins = new ArrayList<>();
        }
        this.bins.add(bin);
        bin.setLevel(this);
    }

    public void removeBin(Bin bin) {
        if (this.bins != null) {
            this.bins.remove(bin);
            bin.setLevel(null);
        }
    }

    public String getFullLevelLocation() {
        if (rack == null || rack.getAisle() == null || rack.getAisle().getZone() == null ||
            rack.getAisle().getZone().getWarehouse() == null) {
            return null;
        }
        return String.format("%s-%s-%s-%s-%s",
                rack.getAisle().getZone().getWarehouse().getWarehouseId(),
                rack.getAisle().getZone().getZoneId(),
                rack.getAisle().getAisleId(),
                rack.getRackId(),
                levelId);
    }
}