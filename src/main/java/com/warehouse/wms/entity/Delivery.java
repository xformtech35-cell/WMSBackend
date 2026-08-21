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
@Table(name = "wms_delivery", indexes = {
    @Index(name = "idx_del_number", columnList = "delivery_number"),
    @Index(name = "idx_del_shipment_number", columnList = "shipment_number"),
    @Index(name = "idx_del_so_number", columnList = "so_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_number", unique = true, nullable = false, length = 50)
    private String deliveryNumber;

    @Column(name = "shipment_number", nullable = false, length = 50)
    private String shipmentNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "package_number", length = 50)
    private String packageNumber;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "delivery_date", nullable = false)
    private LocalDateTime deliveryDate;

    @Column(name = "received_by", length = 100)
    private String receivedBy;

    @Column(name = "delivered_quantity")
    private Integer deliveredQuantity = 0;

    @Column(name = "delivery_status", nullable = false, length = 30)
    private String deliveryStatus = "DELIVERED"; // DELIVERED, PARTIAL, REJECTED, RETURNED

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    @Column(name = "delivery_proof_url", length = 500)
    private String deliveryProofUrl;

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