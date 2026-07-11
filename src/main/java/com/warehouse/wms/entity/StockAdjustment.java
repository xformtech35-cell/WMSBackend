package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_stock_adjustment")
public class StockAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @ManyToOne
    @JoinColumn(name = "bin_id")
    private Bin bin;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "quantity_adjusted", nullable = false)
    private Integer quantityAdjusted;

    private String reason;

    @ManyToOne
    @JoinColumn(name = "adjusted_by")
    private User adjustedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
