// ====== FILE: src/main/java/com/warehouse/wms/entity/QRCode.java ======
package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.warehouse.wms.constant.QRStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "wms_qr_codes", indexes = {
    @Index(name = "idx_qr_code", columnList = "qr_code"),
    @Index(name = "idx_barcode", columnList = "barcode"),
    @Index(name = "idx_grn_number", columnList = "grn_number"),
    @Index(name = "idx_putaway_task_id", columnList = "putaway_task_id"),
    @Index(name = "idx_bin_id", columnList = "bin_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "qr_id", nullable = false, unique = true, length = 50)
    private String qrId;
    
    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;
    
    @Column(name = "qr_image", columnDefinition = "LONGTEXT")
    private String qrImage; // Base64 encoded image
    
    @Column(name = "qr_data", columnDefinition = "LONGTEXT")
    private String qrData; // JSON data encoded
    
    @Column(name = "barcode", unique = true, length = 100)
    private String barcode;
    
    @Column(name = "barcode_image", columnDefinition = "LONGTEXT")
    private String barcodeImage; // Base64 encoded barcode image
    
    @Column(name = "qr_type", length = 20)
    private String qrType; // QR_CODE, CODE128, DATAMATRIX, GS1_128
    
    @Column(name = "label_level", length = 20)
    private String labelLevel; // UNIT, BOX, PALLET, BATCH, LOCATION
    
    @Column(name = "label_type", length = 20)
    private String labelType; // PUTAWAY, BIN, PRODUCT, SHIPMENT
    
    // Reference
    @Column(name = "grn_number", length = 50)
    private String grnNumber;
    
    @Column(name = "inbound_id")
    private Long inboundId;
    
    @Column(name = "inbound_line_id")
    private Long inboundLineId;
    
    @Column(name = "putaway_task_id")
    private Long putawayTaskId;
    
    @Column(name = "putaway_line_id")
    private Long putawayLineId;
    
    @Column(name = "item_code", length = 50)
    private String itemCode;
    
    @Column(name = "item_name", length = 200)
    private String itemName;
    
    @Column(name = "batch_number", length = 50)
    private String batchNumber;
    
    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;
    
    // Quantities
    @Column(name = "quantity")
    private Integer quantity = 0;
    
    @Column(name = "uom", length = 10)
    private String uom;
    
    // Dates
    @Column(name = "mfg_date")
    private LocalDateTime mfgDate;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // Location
    @Column(name = "warehouse_id", length = 20)
    private String warehouseId;
    
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
    
    @Column(name = "pallet_number", length = 50)
    private String palletNumber;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QRStatus status = QRStatus.GENERATED;
    
    @Column(name = "printed_by", length = 100)
    private String printedBy;
    
    @Column(name = "printed_at")
    private LocalDateTime printedAt;
    
    @Column(name = "print_copies")
    private Integer printCopies = 1;
    
    @Column(name = "scanned_by", length = 100)
    private String scannedBy;
    
    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;
    
    @Column(name = "scan_count")
    private Integer scanCount = 0;
    
    // Metadata
    @Column(name = "generated_by", length = 100)
    private String generatedBy;
    
    @Column(name = "template_name", length = 100)
    private String templateName;
    
    @Column(name = "label_format", length = 20)
    private String labelFormat; // PNG, PDF, ZPL, SVG
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    
    
}