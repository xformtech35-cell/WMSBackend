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
@Table(name = "wms_pick_task_item", indexes = {
        @Index(name = "idx_pti_task_id", columnList = "pick_task_id"),
        @Index(name = "idx_pti_item_code", columnList = "item_code"),
        @Index(name = "idx_pti_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTaskItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_task_id", nullable = false)
    private PickTask pickTask;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity = 0;

    @Column(name = "picked_quantity")
    private Integer pickedQuantity = 0;

    @Column(name = "short_quantity")
    private Integer shortQuantity = 0;

    @Column(name = "quantity_to_pick", nullable = false)
    private Integer quantityToPick = 0;

    @Column(name = "location_barcode", length = 100)
    private String locationBarcode;

    @Column(name = "item_barcode", length = 100)
    private String itemBarcode;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "source_location", length = 200)
    private String sourceLocation;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, PICKING, PICKED, SHORT, CANCELLED

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "is_scanned")
    private Boolean isScanned = false;

    @Column(name = "scan_time")
    private LocalDateTime scanTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", referencedColumnName = "id")
    private InventoryStock inventoryStock;

    @Column(name = "sales_order_line_id")
    private Long salesOrderLineId;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}