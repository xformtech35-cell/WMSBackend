// ====== FILE: src/main/java/com/warehouse/wms/entity/Trolley.java ======
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

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wms_trolleys", indexes = {
    @Index(name = "idx_trolley_identifier", columnList = "trolley_identifier"),
    @Index(name = "idx_trolley_status", columnList = "status")
})
public class Trolley {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trolley_identifier", unique = true, nullable = false, length = 50)
    private String trolleyIdentifier;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "trolley_type", length = 50)
    private String trolleyType; // MANUAL, ELECTRIC, HAND_PALLET, FORKLIFT

    @Column(name = "capacity")
    private Integer capacity = 0; // Max weight in kg

    @Column(name = "current_load")
    private Integer currentLoad = 0; // Current weight in kg

    @Column(name = "status", length = 50)
    private String status = "AVAILABLE"; // AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "maintenance_due_date")
    private LocalDateTime maintenanceDueDate;

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
    @OneToMany(mappedBy = "trolley", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<RackCompartment> compartments = new ArrayList<>();

    // Helper methods
    public void addCompartment(RackCompartment compartment) {
        if (this.compartments == null) {
            this.compartments = new ArrayList<>();
        }
        this.compartments.add(compartment);
        compartment.setTrolley(this);
    }

    public void removeCompartment(RackCompartment compartment) {
        if (this.compartments != null) {
            this.compartments.remove(compartment);
            compartment.setTrolley(null);
        }
    }

    public Boolean hasCapacity(Integer weight) {
        if (this.capacity == null || this.currentLoad == null) {
            return false;
        }
        return (this.capacity - this.currentLoad) >= weight;
    }

    public void addLoad(Integer weight) {
        if (this.currentLoad == null) this.currentLoad = 0;
        this.currentLoad += weight;
        this.lastUsedAt = LocalDateTime.now();
        // Update status based on load
        if (this.currentLoad >= this.capacity) {
            this.status = "IN_USE";
        }
    }

    public void removeLoad(Integer weight) {
        if (this.currentLoad == null) this.currentLoad = 0;
        this.currentLoad -= weight;
        if (this.currentLoad < 0) this.currentLoad = 0;
        if (this.currentLoad == 0) {
            this.status = "AVAILABLE";
        }
    }
}