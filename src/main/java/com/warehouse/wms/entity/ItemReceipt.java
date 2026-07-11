package com.warehouse.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.warehouse.wms.converter.StringListConverter;

@Entity
@Table(name = "wms_item_receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_request_item_id", nullable = false)
    private PurchaseRequestItem purchaseRequestItem;

    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(name = "defective_quantity")
    private Integer defectiveQuantity = 0;

    @Column(name = "quality_status", nullable = false)
    private String qualityStatus; // GOOD, PARTIAL, REJECTED

    private String remarks;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> images;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
