package com.warehouse.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_count_line")
public class CountLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "count_task_id", nullable = false)
    private CountTask countTask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "expected_qty", nullable = false)
    private Integer expectedQty;

    @Column(name = "counted_qty", nullable = false)
    private Integer countedQty;

    @Column(nullable = false)
    private Integer variance;

    @Column(name = "reason_code")
    private String reasonCode; // DAMAGE, THEFT, MISCOUNT, SYSTEM_ERROR, FOUND_STOCK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LineStatus status; // PENDING, APPROVED, REJECTED

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum LineStatus {
        PENDING, APPROVED, REJECTED
    }
}
