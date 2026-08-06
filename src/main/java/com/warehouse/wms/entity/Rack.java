// ====== FILE: src/main/java/com/warehouse/wms/entity/Rack.java ======
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
@Table(name = "wms_racks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rack_id", nullable = false, unique = true, length = 10)
    private String rackId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "height")
    private Double height;

    @Column(name = "width")
    private Double width;
    
    private String unit;

    @Column(name = "depth")
    private Double depth;

    @Column(name = "total_shelves")
    private Integer totalShelves = 0;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aisle_id")
    private Aisle aisle;

    // ✅ ADD LEVELS (replaces direct bins)
    @JsonIgnore
    @OneToMany(mappedBy = "rack", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Level> levels = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "rack", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RackCompartment> compartments = new ArrayList<>();

    // Helper methods for Levels
    public void addLevel(Level level) {
        if (this.levels == null) {
            this.levels = new ArrayList<>();
        }
        this.levels.add(level);
        level.setRack(this);
        this.totalShelves = this.levels.size();
    }

    public void removeLevel(Level level) {
        if (this.levels != null) {
            this.levels.remove(level);
            level.setRack(null);
            this.totalShelves = this.levels.size();
        }
    }
}