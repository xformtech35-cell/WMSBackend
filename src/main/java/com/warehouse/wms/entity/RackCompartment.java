// ====== FILE: src/main/java/com/warehouse/wms/entity/RackCompartment.java ======
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
@Table(name = "wms_rack_compartments", indexes = {
    @Index(name = "idx_compartment_id", columnList = "compartment_id"),
    @Index(name = "idx_rack_id", columnList = "rack_id"),
    @Index(name = "idx_trolley_id", columnList = "trolley_id"),
    @Index(name = "idx_sales_order_id", columnList = "sales_order_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RackCompartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "compartment_id", nullable = false, unique = true, length = 20)
    private String compartmentId;

    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "position", length = 10)
    private String position;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "capacity")
    private Integer capacity = 0;

    @Column(name = "used_capacity")
    private Integer usedCapacity = 0;

    @Column(name = "available_capacity")
    private Integer availableCapacity = 0;

  

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
    
    
 // ====== FILE: src/main/java/com/warehouse/wms/entity/RackCompartment.java ======
 // If you have Double fields, remove precision and scale
 @Column(name = "width")
 private Double width;  // ✅ Remove precision and scale

 @Column(name = "height")
 private Double height;  // ✅ Remove precision and scale

 @Column(name = "depth")
 private Double depth;  // ✅ Remove precision and scale

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rack_id")
    private Rack rack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trolley_id")
    private Trolley trolley;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    // Helper methods
    public Integer getAvailableCapacity() {
        return this.capacity - (this.usedCapacity != null ? this.usedCapacity : 0);
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
}