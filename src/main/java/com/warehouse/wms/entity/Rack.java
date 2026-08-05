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
    private String rackId; // R-01, R-02, R-03

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "height")
    private Double height; // in meters

    @Column(name = "width")
    private Double width; // in meters
    
    private String unit;


    @Column(name = "depth")
    private Double depth; // in meters

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

    @JsonIgnore
    @OneToMany(mappedBy = "rack", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Bin> bins = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "rack", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RackCompartment> compartments = new ArrayList<>();

    public void addBin(Bin bin) {
        if (this.bins == null) {
            this.bins = new ArrayList<>();
        }
        this.bins.add(bin);
        bin.setRack(this);
    }

    public void removeBin(Bin bin) {
        if (this.bins != null) {
            this.bins.remove(bin);
            bin.setRack(null);
        }
    }
}