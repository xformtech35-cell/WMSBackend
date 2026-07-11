package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PutawayTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "suggested_bin_id")
    private Bin suggestedBin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PutawayTaskStatus status;

    @Column(nullable = false)
    private Integer priority;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    public enum PutawayTaskStatus {
        PENDING,
        COMPLETED,
        CANCELLED
    }
}
