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
@Table(name = "wms_stock_reservation", indexes = {
    @Index(name = "idx_res_so_number", columnList = "so_number"),
    @Index(name = "idx_res_item_code", columnList = "item_code"),
    @Index(name = "idx_res_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long salesOrderItemId;


    @Column(name = "reservation_number", unique = true, nullable = false, length = 50)
    private String reservationNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity = 0;

    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;
    
    private Integer pysicalQuantity = 0;


    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "warehouse_id", length = 20)
    private String warehouseId;

    @Column(name = "zone_id", length = 20)
    private String zoneId;

    @Column(name = "aisle_id", length = 20)
    private String aisleId;

    @Column(name = "rack_id", length = 20)
    private String rackId;

    @Column(name = "level_id", length = 20)
    private String levelId;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "RESERVED"; // RESERVED, PICKED, CANCELLED

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

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