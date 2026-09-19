package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_pick_confirmation_item", indexes = {
    @Index(name = "idx_pci_conf", columnList = "pick_confirmation_id"),
    @Index(name = "idx_pci_item", columnList = "item_code"),
    @Index(name = "idx_pci_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickConfirmationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pick_confirmation_id", nullable = false)
    private PickConfirmation pickConfirmation;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "required_quantity")
    private Integer requiredQuantity = 0;

    @Column(name = "picked_quantity", nullable = false)
    private Integer pickedQuantity = 0;

    @Column(name = "short_quantity")
    private Integer shortQuantity = 0;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "bin_id", length = 50)
    private String binId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "CONFIRMED"; // CONFIRMED, PARTIAL, REJECTED

    @Column(columnDefinition = "TEXT")
    private String remarks;
}