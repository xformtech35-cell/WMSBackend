package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wms_inbound")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inbound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "inbound_number", nullable = false, unique = true)
    private String inboundNumber;
    
    @Column(name = "inbound_date", nullable = false)
    private LocalDate inboundDate;
    
    @Column(name = "expected_arrival_date")
    private LocalDate expectedArrivalDate;
    
    // PO Details
    @Column(name = "po_number")
    private String poNumber;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @Column(name = "delivery_challan")
    private String deliveryChallan;
    
    @Column(name = "supplier_name")
    private String supplierName;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "tracking_name")
    private String trackingName;
    
    // Gate Entry
    @Column(name = "gate_entry_number")
    private String gateEntryNumber;
    
    @Column(name = "driver_name")
    private String driverName;
    
    @Column(name = "driver_contact")
    private String driverContact;
    
    @Column(name = "driver_id")
    private String driverId;
    
    @Column(name = "track_number")
    private String trackNumber;
    
    @Column(name = "gate_number")
    private String gateNumber;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "gate_entry_date_time")
    private LocalDateTime gateEntryDateTime;
    
    // Unloading
    @Column(name = "boxes_unloaded_quantity")
    private Integer boxesUnloadedQuantity;
    
    private Integer boxesInTruckQuantity;

    
    @Column(name = "unloaded_by")
    private String unloadedBy;
    
    @Column(name = "unloading_start_time")
    private LocalDateTime unloadingStartTime;
    
    @Column(name = "unloading_end_time")
    private LocalDateTime unloadingEndTime;
    
    // Goods Receiving
    @Column(name = "received_by")
    private Long receivedBy;
    
    @Column(name = "received_date")
    private LocalDateTime receivedDate;
    
    // Quality Inspection
    @Column(name = "inspected_by")
    private Long inspectedBy;
    
    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;
    
    @Column(name = "quality_status")
    private String qualityStatus; // GOOD, PARTIAL, REJECTED
    
    @Column(name = "quality_remarks", columnDefinition = "TEXT")
    private String qualityRemarks;
    
    // GRN
    @Column(name = "grn_number")
    private String grnNumber;
    
    @Column(name = "grn_date")
    private LocalDateTime grnDate;
    
    @Column(name = "grn_status")
    private String grnStatus; // PENDING, GENERATED
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InboundStatus status = InboundStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private InboundStage stage = InboundStage.PENDING_INBOUND;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @OneToMany(mappedBy = "inbound", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InboundLine> lines = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addLine(InboundLine line) {
        lines.add(line);
        line.setInbound(this);
    }
    
    public void removeLine(InboundLine line) {
        lines.remove(line);
        line.setInbound(null);
    }
}