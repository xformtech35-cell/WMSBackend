// ====== FILE: src/main/java/com/warehouse/wms/entity/PutawayConfirmation.java ======
package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_putaway_confirmations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "confirmation_number", nullable = false, unique = true, length = 50)
    private String confirmationNumber;

    @Column(name = "task_number", nullable = false, length = 50)
    private String taskNumber;

    @Column(name = "putaway_task_id")
    private Long putawayTaskId;

    @Column(name = "grn_number", length = 50)
    private String grnNumber;

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "total_quantity")
    private Integer totalQuantity = 0;

    @Column(name = "confirmed_quantity")
    private Integer confirmedQuantity = 0;

    @Column(name = "warehouse_id", length = 20)
    private String warehouseId;
    
    private String fullpath;


    @Column(name = "zone", length = 10)
    private String zone;

    @Column(name = "aisle", length = 10)
    private String aisle;

    @Column(name = "rack", length = 10)
    private String rack;

    @Column(name = "shelf", length = 10)
    private String shelf;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "bin_barcode", length = 100)
    private String binBarcode;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "inventory_update_id")
    private Long inventoryUpdateId;

    @Column(name = "inventory_updated")
    private Boolean inventoryUpdated = false;

    @Column(name = "inventory_updated_at")
    private LocalDateTime inventoryUpdatedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "putaway_task_id", insertable = false, updatable = false)
    private PutawayTask putawayTask;
}