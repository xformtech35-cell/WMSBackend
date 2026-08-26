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
@Table(name = "wms_delivery_challan_package", indexes = {
    @Index(name = "idx_dcp_challan_number", columnList = "challan_number"),
    @Index(name = "idx_dcp_package_number", columnList = "package_number"),
    @Index(name = "idx_dcp_customer_code", columnList = "customer_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChallanPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challan_number", nullable = false, length = 50)
    private String challanNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "package_number", nullable = false, length = 50)
    private String packageNumber;

    @Column(name = "package_barcode", length = 100)
    private String packageBarcode;

    // Customer fields per package
    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "customer_gst", length = 50)
    private String customerGst;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "dispatch_date")
    private LocalDateTime dispatchDate;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    // Item fields
    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "uom", length = 10)
    private String uom;

    @Column(name = "ordered_quantity")
    private Integer orderedQuantity = 0;

    @Column(name = "dispatched_quantity")
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
    private String status = "PENDING";

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