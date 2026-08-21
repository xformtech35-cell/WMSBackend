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
@Table(name = "wms_pick_task", indexes = {
    @Index(name = "idx_pt_number", columnList = "pick_task_number"),
    @Index(name = "idx_pt_pl_number", columnList = "pick_list_number"),
    @Index(name = "idx_pt_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pick_task_number", unique = true, nullable = false, length = 50)
    private String pickTaskNumber;

    @Column(name = "pick_list_number", nullable = false, length = 50)
    private String pickListNumber;

    @Column(name = "so_number", length = 50)
    private String soNumber;

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

    @Column(name = "location_barcode", length = 100)
    private String locationBarcode;

    @Column(name = "item_barcode", length = 100)
    private String itemBarcode;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "picker_id", length = 100)
    private String pickerId;

    @Column(name = "picker_name", length = 100)
    private String pickerName;

    @Column(name = "scan_time")
    private LocalDateTime scanTime;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, SCANNED, CONFIRMED, CANCELLED

    @Column(name = "quantity_to_pick", nullable = false)  // ADD THIS FIELD
    private Integer quantityToPick = 0;
    
    @Column(name = "inventory_id")
    private Long inventoryId;  // Already nullable by default
    
    @Column(name = "sales_order_line_id")  // ADD THIS FIELD
    private Long salesOrderLineId;
    
    @Column(name = "is_scanned")
    private Boolean isScanned = false;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}