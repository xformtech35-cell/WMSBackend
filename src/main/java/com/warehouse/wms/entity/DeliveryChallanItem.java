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
@Table(name = "wms_delivery_challan_item", indexes = {
    @Index(name = "idx_dci_challan_number", columnList = "challan_number"),
    @Index(name = "idx_dci_item_code", columnList = "item_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChallanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_number", nullable = false, length = 50)
    private String challanNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "ordered_quantity", nullable = false)
    private Integer orderedQuantity = 0;

    @Column(name = "dispatched_quantity", nullable = false)
    private Integer dispatchedQuantity = 0;

    @Column(name = "delivered_quantity")
    private Integer deliveredQuantity = 0;

    @Column(name = "short_quantity")
    private Integer shortQuantity = 0;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "unit_price")
    private Double unitPrice = 0.0;

    @Column(name = "total_price")
    private Double totalPrice = 0.0;

    @Column(name = "weight")
    private Double weight = 0.0;

    @Column(name = "volume")
    private Double volume = 0.0;

    @Column(name = "status", length = 30)
    private String status = "PENDING"; // PENDING, DISPATCHED, DELIVERED, RETURNED

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_challan_id")
    private DeliveryChallan deliveryChallan;
}