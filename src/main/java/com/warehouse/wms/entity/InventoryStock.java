// ====== FILE: src/main/java/com/warehouse/wms/entity/InventoryStock.java ======
package com.warehouse.wms.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.warehouse.wms.constant.InventoryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_inventory_stock", indexes = {
    @Index(name = "idx_item_code", columnList = "item_code"),
    @Index(name = "idx_bin_id", columnList = "bin_id"),
    @Index(name = "idx_grn_number", columnList = "grn_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_number", unique = true, length = 50)
    private String inventoryNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "in_transit_quantity")
    private Integer inTransitQuantity = 0;

    @Column(name = "warehouse_id", nullable = false, length = 20)
    private String warehouseId;

    @Column(name = "zone", length = 10)
    private String zone;

    @Column(name = "aisle", length = 10)
    private String aisle;

    @Column(name = "rack", length = 10)
    private String rack;

    @Column(name = "shelf", length = 10)
    private String shelf;
    
    @Column(name = "level", length = 10)
    private String level;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "bin_barcode", length = 100)
    private String binBarcode;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "mfg_date")
    private LocalDateTime mfgDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "grn_number", length = 50)
    private String grnNumber;

    @Column(name = "putaway_task_number", length = 50)
    private String putawayTaskNumber;

    @Column(name = "confirmation_number", length = 50)
    private String confirmationNumber;

    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Column(name = "qr_code_value", length = 100)
    private String qrCodeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InventoryStatus status = InventoryStatus.ACTIVE;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "is_allocated")
    private Boolean isAllocated = false;

    @Column(name = "is_frozen")
    private Boolean isFrozen = false;

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


    
    public void addQuantity(Integer qty) {
        if (qty == null || qty <= 0) return;
        this.quantity += qty;
        this.availableQuantity += qty;
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public void removeQuantity(Integer qty) {
        if (qty == null || qty <= 0) return;
        this.quantity -= qty;
        this.availableQuantity -= qty;
        if (this.availableQuantity < 0) this.availableQuantity = 0;
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public void reserveQuantity(Integer qty) {
        if (qty == null || qty <= 0 || qty > this.availableQuantity) return;
        this.reservedQuantity += qty;
        this.availableQuantity -= qty;
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public void unreserveQuantity(Integer qty) {
        if (qty == null || qty <= 0 || qty > this.reservedQuantity) return;
        this.reservedQuantity -= qty;
        this.availableQuantity += qty;
        this.lastUpdatedDate = LocalDateTime.now();
    }
}