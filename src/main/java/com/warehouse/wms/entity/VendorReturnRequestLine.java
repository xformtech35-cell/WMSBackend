package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wms_vendor_return_request_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorReturnRequestLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "uom")
    private String uom;

    private String rejectedArea;

    
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity;

    @Column(name = "approved_quantity")
    private Integer approvedQuantity;

    @Column(name = "actual_returned_quantity")
    private Integer actualReturnedQuantity;

    @Column(name = "original_quantity")
    private Integer originalQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_request_id")
    private VendorReturnRequest returnRequest;

    @Column(name = "inbound_line_id")
    private Long inboundLineId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}