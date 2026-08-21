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
@Table(name = "wms_shipping_label", indexes = {
    @Index(name = "idx_sl_number", columnList = "label_number"),
    @Index(name = "idx_sl_package_number", columnList = "package_number"),
    @Index(name = "idx_sl_so_number", columnList = "so_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingLabel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "label_number", unique = true, nullable = false, length = 50)
    private String labelNumber;

    @Column(name = "package_number", nullable = false, length = 50)
    private String packageNumber;

    @Column(name = "package_barcode", length = 100)
    private String packageBarcode;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "quantity")
    private Integer quantity = 0;

    @Column(name = "weight")
    private Double weight = 0.0;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "label_status", length = 30)
    private String labelStatus = "PRINTED"; // PRINTED, SCANNED, SHIPPED

    @Column(name = "printed_by", length = 100)
    private String printedBy;

    @Column(name = "printed_date")
    private LocalDateTime printedDate;

    @Column(name = "label_url", length = 500)
    private String labelUrl;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}