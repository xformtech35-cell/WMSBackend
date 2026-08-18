// ====== FILE: src/main/java/com/warehouse/wms/entity/StockTransferHistory.java ======
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
@Table(name = "wms_stock_transfer_history", indexes = {
    @Index(name = "idx_transfer_number", columnList = "transfer_number"),
    @Index(name = "idx_source_location", columnList = "source_location_path"),
    @Index(name = "idx_target_location", columnList = "target_location_path"),
    @Index(name = "idx_item_code_transfer", columnList = "item_code"),
    @Index(name = "idx_transfer_date", columnList = "transfer_date"),
    @Index(name = "idx_grn_number", columnList = "grn_number"),
    @Index(name = "idx_inventory_number", columnList = "inventory_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transfer_number", unique = true, nullable = false, length = 50)
    private String transferNumber;
    
    // Source Location Details
    @Column(name = "source_warehouse_id", length = 20)
    private String sourceWarehouseId;
    
    @Column(name = "source_zone_id", length = 10)
    private String sourceZoneId;
    
    @Column(name = "source_aisle_id", length = 10)
    private String sourceAisleId;
    
    @Column(name = "source_rack_id", length = 10)
    private String sourceRackId;
    
    @Column(name = "source_level_id", length = 20)
    private String sourceLevelId;
    
    @Column(name = "source_bin_id", length = 50)
    private String sourceBinId;
    
    @Column(name = "source_location_path", length = 200)
    private String sourceLocationPath;
    
    // Target Location Details
    @Column(name = "target_warehouse_id", length = 20)
    private String targetWarehouseId;
    
    @Column(name = "target_zone_id", length = 10)
    private String targetZoneId;
    
    @Column(name = "target_aisle_id", length = 10)
    private String targetAisleId;
    
    @Column(name = "target_rack_id", length = 10)
    private String targetRackId;
    
    @Column(name = "target_level_id", length = 20)
    private String targetLevelId;
    
    @Column(name = "target_bin_id", length = 50)
    private String targetBinId;
    
    @Column(name = "target_location_path", length = 200)
    private String targetLocationPath;
    
    // Item Details
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;
    
    @Column(name = "item_name", length = 200)
    private String itemName;
    
    @Column(name = "uom", length = 10)
    private String uom;
    
    @Column(name = "batch_number", length = 50)
    private String batchNumber;
    
    @Column(name = "inventory_number", length = 50)
    private String inventoryNumber;
    
    @Column(name = "quantity_transferred", nullable = false)
    private Integer quantityTransferred;
    
    @Column(name = "source_old_quantity")
    private Integer sourceOldQuantity;
    
    @Column(name = "source_new_quantity")
    private Integer sourceNewQuantity;
    
    @Column(name = "target_old_quantity")
    private Integer targetOldQuantity;
    
    @Column(name = "target_new_quantity")
    private Integer targetNewQuantity;
    
    // GRN Related Fields
    @Column(name = "grn_number", length = 50)
    private String grnNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status")
    private TransferStatus transferStatus;
    
    @Column(name = "transfer_reason", length = 500)
    private String transferReason;
    
    @Column(name = "transfer_date")
    private LocalDateTime transferDate;
    
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
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    public enum TransferStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        PARTIALLY_COMPLETED
    }
}