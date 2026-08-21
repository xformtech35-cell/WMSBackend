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
@Table(name = "wms_dispatch", indexes = {
    @Index(name = "idx_dsp_number", columnList = "dispatch_number"),
    @Index(name = "idx_dsp_shipment_number", columnList = "shipment_number"),
    @Index(name = "idx_dsp_so_number", columnList = "so_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispatch_number", unique = true, nullable = false, length = 50)
    private String dispatchNumber;

    @Column(name = "shipment_number", length = 50)
    private String shipmentNumber;

    @Column(name = "so_number", nullable = false, length = 50)
    private String soNumber;

    @Column(name = "package_number", nullable = false, length = 50)
    private String packageNumber;

    @Column(name = "customer_code", length = 50)
    private String customerCode;

    @Column(name = "customer_name", length = 200)
    private String customerName;

    @Column(name = "transporter", length = 100)
    private String transporter;

    @Column(name = "vehicle_number", length = 20)
    private String vehicleNumber;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "driver_mobile", length = 15)
    private String driverMobile;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "delivery_challan", length = 50)
    private String deliveryChallan;

    @Column(name = "dispatch_date", nullable = false)
    private LocalDateTime dispatchDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "DISPATCHED"; // DISPATCHED, IN_TRANSIT, DELIVERED

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

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