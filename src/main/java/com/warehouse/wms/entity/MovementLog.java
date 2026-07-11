package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MovementLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    private Inventory.InventoryState fromState;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Inventory.InventoryState toState;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "bin_id")
    private Bin bin;

    private Long userId; // Would reference a User entity

    private String action;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
