// ====== FILE: src/main/java/com/warehouse/wms/entity/PutawayLine.java ======
package com.warehouse.wms.entity;

import com.warehouse.wms.constant.PutawayLineStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms_putaway_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PutawayLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "putaway_quantity")
    private Integer putawayQuantity = 0;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity = 0;

    // Suggested Location
    @Column(name = "suggested_warehouse", length = 20)
    private String suggestedWarehouse;

    @Column(name = "suggested_zone", length = 10)
    private String suggestedZone;

    @Column(name = "suggested_aisle", length = 10)
    private String suggestedAisle;

    @Column(name = "suggested_rack", length = 10)
    private String suggestedRack;

    @Column(name = "suggested_shelf", length = 10)
    private String suggestedShelf;

    @Column(name = "suggested_bin", length = 50)
    private String suggestedBin;

    // Actual Location
    @Column(name = "actual_warehouse", length = 20)
    private String actualWarehouse;

    @Column(name = "actual_zone", length = 10)
    private String actualZone;

    @Column(name = "actual_aisle", length = 10)
    private String actualAisle;

    @Column(name = "actual_rack", length = 10)
    private String actualRack;

    @Column(name = "actual_shelf", length = 10)
    private String actualShelf;

    @Column(name = "actual_bin", length = 50)
    private String actualBin;

    @Column(name = "bin_barcode", length = 100)
    private String binBarcode;

    // Batch/Serial
    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    // QR Code Reference
    @Column(name = "qr_code_id")
    private Long qrCodeId;

    @Column(name = "qr_code_value", length = 100)
    private String qrCodeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PutawayLineStatus status = PutawayLineStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // ====== RELATIONSHIPS ======
    // ONLY ONE of these should exist - choose ONE approach:

    // APPROACH 1: Using entity reference (Recommended)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_line_id")
    private InboundLine inboundLine;

    // ====== REMOVE THIS IF USING APPROACH 1 ======
    // @Column(name = "inbound_line_id")
    // private Long inboundLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "putaway_task_id")
    private PutawayTask putawayTask;
}