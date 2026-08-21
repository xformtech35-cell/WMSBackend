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
@Table(name = "wms_sales_order_item", indexes = {
    @Index(name = "idx_soi_so_number", columnList = "so_number"),
    @Index(name = "idx_soi_item_code", columnList = "item_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity = 0;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity = 0;

    @Column(name = "shipped_quantity")
    private Integer shippedQuantity = 0;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "source_location", length = 200)
    private String sourceLocation;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
}