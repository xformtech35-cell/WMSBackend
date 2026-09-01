package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wms_return_dispatch_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDispatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "dispatched_quantity", nullable = false)
    private Integer dispatchedQuantity;

    @Column(name = "packed_quantity")
    private Integer packedQuantity;

    @Column(name = "packaging_type")
    private String packagingType;

    @Column(name = "package_count")
    private Integer packageCount;

    @Column(name = "package_weight")
    private BigDecimal packageWeight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id")
    private ReturnDispatch dispatch;

    @Column(name = "vro_line_id")
    private Long vroLineId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}