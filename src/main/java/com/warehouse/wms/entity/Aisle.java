// ====== FILE: src/main/java/com/warehouse/wms/entity/Aisle.java ======
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
@Table(name = "wms_aisles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Aisle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aisle_id", nullable = false, unique = true, length = 10)
    private String aisleId; // 01, 02, 03

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "width")
    private Double width; // in meters

    @Column(name = "length")
    private Double length; // in meters

    @Column(name = "total_racks")
    private Integer totalRacks = 0;

    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private String unit;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    
    
  

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @JsonIgnore
    @OneToMany(mappedBy = "aisle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Rack> racks = new ArrayList<>();

    public void addRack(Rack rack) {
        if (this.racks == null) {
            this.racks = new ArrayList<>();
        }
        this.racks.add(rack);
        rack.setAisle(this);
        this.totalRacks = this.racks.size();
    }

    public void removeRack(Rack rack) {
        if (this.racks != null) {
            this.racks.remove(rack);
            rack.setAisle(null);
            this.totalRacks = this.racks.size();
        }
    }
}