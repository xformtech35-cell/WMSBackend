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
@Table(name = "wms_shipment_confirmation", indexes = {
    @Index(name = "idx_sc_shipment_number", columnList = "shipment_number"),
    @Index(name = "idx_sc_so_number", columnList = "so_number"),
    @Index(name = "idx_sc_tracking", columnList = "tracking_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_number", unique = true, nullable = false, length = 50)
    private String shipmentNumber;

    @Column(name = "dispatch_number", length = 50)
    private String dispatchNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "package_number", length = 50)
    private String packageNumber;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "transporter", length = 100)
    private String transporter;

    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "dispatch_date")
    private LocalDateTime dispatchDate;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "IN_TRANSIT"; // IN_TRANSIT, DELIVERED, FAILED

    @Column(name = "confirmed_by", length = 100)
    private String confirmedBy;

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